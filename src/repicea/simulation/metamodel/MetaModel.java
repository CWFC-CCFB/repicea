/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */

package repicea.simulation.metamodel;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repicea.io.FormatField;
import repicea.io.Loadable;
import repicea.io.Saveable;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataBlock;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericHierarchicalStatisticalDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.Observation;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * A package class that handles the data set and fits the meta model. It implements
 * Runnable for an eventual multi-threaded implementation.
 * @author Mathieu Fortin - December 2020
 */
public class MetaModel implements Saveable {

	protected static boolean Verbose = false; 
	
	private class Bound {
		final double lower;
		final double upper;
		final double extent;
		
		Bound(double lower, double upper) {
			this.lower = lower;
			this.upper = upper;
			this.extent = this.upper - lower;
		}

		boolean checkValue(double value) {
			if (value < lower || value > upper) {
				return false;
			} else {
				return true;
			}
		}
		
		double getRandomValue() {
			return lower + extent * StatisticalUtility.getRandom().nextDouble(); 
		}
	}

	static class InnerModel {
		
		private Matrix parameters;
		final List<DataBlockWrapper> dataBlockWrappers;
		final Map<Object, Matrix> dummyMap;
		final List dummyOriginalValues;

		InnerModel(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
			dummyOriginalValues = structure.getPossibleValueForDummyVariable("OutputType", null);
			dummyMap = new HashMap<Object, Matrix>();
			for (Object obj : dummyOriginalValues) {
				Matrix oMat = new Matrix(1, dummyOriginalValues.size());
				oMat.setValueAt(0, dummyOriginalValues.indexOf(obj), 1d);
				dummyMap.put(obj, oMat);
			}

			Map<String, DataBlock> formattedMap = new LinkedHashMap<String, DataBlock>();
			Map<String, DataBlock> ageMap = structure.getHierarchicalStructure(); 
			for (String ageKey : ageMap.keySet()) {
				DataBlock db = ageMap.get(ageKey);
				for (String speciesGroupKey : db.keySet()) {
					DataBlock innerDb = db.get(speciesGroupKey);
					formattedMap.put(ageKey + "_" + speciesGroupKey, innerDb);
				}
			}

			dataBlockWrappers = new ArrayList<DataBlockWrapper>();
			for (String k : formattedMap.keySet()) {
				DataBlock db = formattedMap.get(k);
				List<Integer> indices = db.getIndices();
				dataBlockWrappers.add(new DataBlockWrapper(this, k, indices, structure, varCov));
			}
		}

		
		private static Matrix getGeneralParm(Matrix dummy, Matrix parameters, int i) {
			Matrix genParm = dummy.multiply(parameters.getSubMatrix(i, i+1, 0, 0));
			return genParm;
		}
		
		Matrix generatePredictions(DataBlockWrapper dbw, double randomEffect) {
			Matrix b1 = getGeneralParm(dbw.dummy, parameters, 0);
			Matrix b2 = getGeneralParm(dbw.dummy, parameters, 2);
			Matrix b3 = getGeneralParm(dbw.dummy, parameters, 4);
			Matrix mu = new Matrix(dbw.vecY.m_iRows, 1);
			for (int i = 0; i < mu.m_iRows; i++) {
				mu.setValueAt(i, 0, getPrediction(b1.getValueAt(i, 0), 
						b2.getValueAt(i, 0),
						b3.getValueAt(i, 0),
						dbw.ageYr.getValueAt(i, 0), randomEffect));
			}
			return mu;
		}
		
		Matrix getVarianceRandomEffect(DataBlockWrapper dbw) {
			return getGeneralParm(dbw.dummy.getSubMatrix(0, 0, 0, dbw.dummy.m_iCols - 1), parameters, 6);
		}
		
		static double getPrediction(double b1, double b2, double b3, double ageYr, double r1) {
			double pred = (b1 + r1) * Math.pow(1 - Math.exp(-b2 * ageYr), b3);
			return pred;
		}
		
		
		double getLogLikelihood(Matrix parameters) {
			setParameters(parameters);
			double logLikelihood = 0d;
			for (DataBlockWrapper dbw : dataBlockWrappers) {
				double marginalLogLikelihoodForThisBlock = dbw.getMarginalLogLikelihood();
				logLikelihood += marginalLogLikelihoodForThisBlock;
			}
			return logLikelihood;
		}
		
		void setParameters(Matrix parameters) {
			this.parameters = parameters;
		}
		
		
		Matrix getVectorOfPopulationAveragedPredictions() {
			int size = 0;
			for (DataBlockWrapper dbw : dataBlockWrappers) {
				size += dbw.indices.size();
			}
			Matrix predictions = new Matrix(size,1);
			for (DataBlockWrapper dbw : dataBlockWrappers) {
				Matrix y_i = generatePredictions(dbw, 0d);
				for (int i = 0; i < dbw.indices.size(); i++) {
					int index = dbw.indices.get(i);
					predictions.setValueAt(index, 0, y_i.getValueAt(i, 0));
				}
			}
			return predictions;
		}
		
	}
	
	private int nbBurnIn = 5000;
	private int nbRealizations = 500000 + nbBurnIn;
	private int nbInternalIter = 100000;
	private int oneEach = 50;
	private int nbInitialGrid = 10000;	

	private double coefVar;
	private boolean converged;
	
	private final Map<Integer, ScriptResult> scriptResults;
	private List<Bound> bounds;
	private InnerModel model;
	private Matrix finalParmEstimates;
	private Matrix finalVarCov;
	private String stratumGroup;
	private List<MetaModelGibbsSample> finalGibbsSample;
	private DataSet dataSet;

	/**
	 * Constructor. 
	 * @param stratumGroup a String representing the stratum group
	 */
	public MetaModel(String stratumGroup) {
		this.stratumGroup = stratumGroup;
		scriptResults = new ConcurrentHashMap<Integer, ScriptResult>();
		setDefaultSettings();
	}

	private void setDefaultSettings() {
		nbBurnIn = 5000;
		nbRealizations = 500000 + nbBurnIn;
		nbInternalIter = 100000;
		oneEach = 50;
		nbInitialGrid = 10000;	
	}
	
	
	/**
	 * Provide the stratum group for this mate-model.
	 * @return a String
	 */
	public String getStratumGroup() {
		return stratumGroup;
	}
	
	/**
	 * Return the state of the model. The model can be used if it has converged.
	 * @return
	 */
	public boolean hasConverged() {
		return converged;
	}
	
	void add(int initialAge, ScriptResult result) {
		boolean canBeAdded;
		if (scriptResults.isEmpty()) {
			canBeAdded = true;
		} else {
			ScriptResult previousResult = scriptResults.values().iterator().next();
			if (previousResult.isCompatible(result)) {
				canBeAdded = true;
			} else {
				canBeAdded = false;
			}
		}
		if (canBeAdded) {
			scriptResults.put(initialAge, result);
			converged = false;  // reset convergence to false since new results have been added
		} else {
			throw new InvalidParameterException("The result parameter is not compatible with previous results in the map!");
		}
	}

	protected HierarchicalStatisticalDataStructure getDataStructureReady() throws StatisticalDataException {
		DataSet overallDataset = null;
		for (int initAgeYr : scriptResults.keySet()) {
			ScriptResult r = scriptResults.get(initAgeYr);
			DataSet dataSet = r.getDataSet();
			if (overallDataset == null) {
				List<String> fieldNames = new ArrayList<String>();
				fieldNames.addAll(dataSet.getFieldNames());
				fieldNames.add("initialAgeYr");
				overallDataset = new DataSet(fieldNames);
			}
			for (Observation obs : dataSet.getObservations()) {
				List<Object> newObs = new ArrayList<Object>();
				newObs.addAll(Arrays.asList(obs.toArray()));
				newObs.add(initAgeYr);	// adding the initial age to the data set
				overallDataset.addObservation(newObs.toArray());
			}
		}
		overallDataset.indexFieldType();
		HierarchicalStatisticalDataStructure dataStruct = new GenericHierarchicalStatisticalDataStructure(overallDataset, false);	// no sorting
		dataStruct.setInterceptModel(false); // no intercept
		dataStruct.constructMatrices("Estimate ~ initialAgeYr + timeSinceInitialDateYr + OutputType + (1 | initialAgeYr/OutputType)");
		return dataStruct;
	}
	
	private Matrix getVarCovReady() {
		Matrix varCov = null;
		for (int initAgeYr : scriptResults.keySet()) {
			ScriptResult r = scriptResults.get(initAgeYr);
			Matrix varCovI = r.getTotalVariance();
			if (varCov == null) {
				varCov = varCovI;
			} else {
				varCov = varCov.matrixDiagBlock(varCovI);
			}
		}
		return varCov;
	}

	private InnerModel getInnerModel(HierarchicalStatisticalDataStructure structure) {
		Matrix varCov = getVarCovReady();
		InnerModel model = new InnerModel(structure, varCov);
		return model;
	}
	
	private MetaModelGibbsSample findFirstSetOfParameters(int nrow, int desiredSize) {
		long startTime = System.currentTimeMillis();
		Matrix parms = new Matrix(nrow, 1);
		double llk = Double.NEGATIVE_INFINITY;
		List<MetaModelGibbsSample> myFirstList = new ArrayList<MetaModelGibbsSample>();
		while (myFirstList.size() < desiredSize) {
			for (int i = 0; i < parms.m_iRows; i++) {
				parms.setValueAt(i, 0, bounds.get(i).getRandomValue());
			}
			llk = model.getLogLikelihood(parms);
			if (Math.exp(llk) > 0d) {
				myFirstList.add(new MetaModelGibbsSample(parms.getDeepClone(), llk));
				int nbSamples = myFirstList.size();
//				if (MetaModel.Verbose) {
					if (nbSamples%1000 == 0) {
						displayMessage("Initial sample list has " + myFirstList.size() + " sets.");
					}
//				}
			}
		}
		Collections.sort(myFirstList);
		MetaModelGibbsSample startingParms = myFirstList.get(myFirstList.size() - 1);
		displayMessage("Time to find a first set of plausible parameters = " + (System.currentTimeMillis() - startTime) + " ms");
		if (MetaModel.Verbose) {
			displayMessage("LLK = " + startingParms.llk + " - Parameters = " + startingParms.parms);
		}
		return startingParms;
	}


//	MF2020-12-21 A pure metropolis-hastings algorithm. 
	private boolean generateMetropolisSample(List<MetaModelGibbsSample> gibbsSample, GaussianDistribution gaussDist) {
		long startTime = System.currentTimeMillis();
		Matrix newParms = null;
		double llk = 0d;
		boolean completed = true;
		int trials = 0;
		int successes = 0;
		double acceptanceRatio; 
		for (int i = 0; i < nbRealizations - 1; i++) { // Metropolis-Hasting  -1 : the starting parameters are considered as the first realization
			gaussDist.setMean(gibbsSample.get(gibbsSample.size() - 1).parms);
			if (i > 0 && i < nbBurnIn && i%500 == 0) {
				acceptanceRatio = ((double) successes) / trials;
				if (MetaModel.Verbose) {
					displayMessage("After " + i + " realizations, the acceptance rate is " + acceptanceRatio);
				}
				if (acceptanceRatio > 0.4) {	// then we must increase the CoefVar
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(1.2*1.2));
				} else if (acceptanceRatio < 0.2) {
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(0.8*0.8));
				}
				successes = 0;
				trials = 0;
			}
//			if (MetaModel.Verbose) {
				if (i%10000 == 0) {
					displayMessage("Processing realization " + i + " / " + nbRealizations);
				}
//			}
			boolean accepted = false;
			int innerIter = 0;
			
			while (!accepted && innerIter < nbInternalIter) {
				newParms = gaussDist.getRandomRealization();
				if (checkBounds(newParms)) {
					llk = model.getLogLikelihood(newParms);
					double ratio = Math.exp(llk - gibbsSample.get(gibbsSample.size() - 1).llk);
					accepted = StatisticalUtility.getRandom().nextDouble() < ratio;
					trials++;
					if (accepted) {
						successes++;
					}
				}
				innerIter++;
			}
			if (innerIter >= nbInternalIter && !accepted) {
				displayMessage("Stopping after " + i + " realization");
				completed = false;
				break;
			} else {
				gibbsSample.add(new MetaModelGibbsSample(newParms, llk));  // new set of parameters is recorded
				if (MetaModel.Verbose) {
					if (gibbsSample.size()%100 == 0) {
						displayMessage(gibbsSample.get(gibbsSample.size() - 1));
					}
				}
			}
		}
		
		acceptanceRatio = ((double) successes) / trials;
		
		displayMessage("Time to obtain " + gibbsSample.size() + " samples = " + (System.currentTimeMillis() - startTime) + " ms");
		displayMessage("Acceptance ratio = " + acceptanceRatio);
		return completed;
	}


	/**
	 * Fit the meta-model.
	 * @return a boolean true if the model has converged or false otherwise
	 */
	public boolean fitModel() {
		coefVar = 0.01;
		boolean converged = false;
		dataSet = null;
		try {
			HierarchicalStatisticalDataStructure dataStructure = getDataStructureReady();
			model = getInnerModel(dataStructure);
			GaussianDistribution gaussDist = getStartingParmEst();
			List<MetaModelGibbsSample> gibbsSample = new ArrayList<MetaModelGibbsSample>();
			MetaModelGibbsSample firstSet = findFirstSetOfParameters(gaussDist.getMean().m_iRows, nbInitialGrid);
			gibbsSample.add(firstSet); // first valid sample
			boolean completed = generateMetropolisSample(gibbsSample, gaussDist);
			if (completed) {
				finalGibbsSample = retrieveFinalSample(gibbsSample);
				MonteCarloEstimate mcmcEstimate = new MonteCarloEstimate();
				for (MetaModelGibbsSample sample : finalGibbsSample) {
					mcmcEstimate.addRealization(sample.parms);
				}
				finalParmEstimates = mcmcEstimate.getMean();
				model.setParameters(finalParmEstimates);
				finalVarCov = mcmcEstimate.getVariance();
				Matrix finalPred = model.getVectorOfPopulationAveragedPredictions();
				Object[] finalPredArray = new Object[finalPred.m_iRows];
				for (int i = 0; i < finalPred.m_iRows; i++) {
					finalPredArray[i] = finalPred.getValueAt(i, 0);
				}
				dataSet = dataStructure.getDataSet();
				dataSet.addField("pred", finalPredArray);
				displayMessage("Final sample had " + finalGibbsSample.size() + " sets of parameters.");
				displayMessage("Final parameters = " + finalParmEstimates.toString());
//				displayMessage(finalVarCov);
				converged = true;
			}
 		} catch (StatisticalDataException e1) {
 			e1.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		} 
		this.converged = converged;
		return this.converged;
	}

	private void displayMessage(Object obj) {
		System.out.println("Meta-model " + stratumGroup + ": " + obj.toString());
	}
	
	private List<MetaModelGibbsSample> retrieveFinalSample(List<MetaModelGibbsSample> gibbsSample) {
		List<MetaModelGibbsSample> finalGibbsSample = new ArrayList<MetaModelGibbsSample>();
		displayMessage("Discarding " + this.nbBurnIn + " samples as burn in.");
		for (int i = nbBurnIn; i < gibbsSample.size(); i+= oneEach) {
			finalGibbsSample.add(gibbsSample.get(i));
		}
		displayMessage("Selecting one every " + this.oneEach + " samples as final selection.");
		return finalGibbsSample;
	}

	private GaussianDistribution getStartingParmEst() {
		Matrix parmEst = new Matrix(8,1);
		parmEst.setValueAt(0, 0, 100d);
		parmEst.setValueAt(1, 0, 100d);
		parmEst.setValueAt(2, 0, 0.02);
		parmEst.setValueAt(3, 0, 0.02);
		parmEst.setValueAt(4, 0, 2d);
		parmEst.setValueAt(5, 0, 2d);
		parmEst.setValueAt(6, 0, 200d);
		parmEst.setValueAt(7, 0, 200d);
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound(0,400));
		bounds.add(new Bound(0,400));
		bounds.add(new Bound(0.0001, 0.1));
		bounds.add(new Bound(0.0001, 0.1));
		bounds.add(new Bound(1,6));
		bounds.add(new Bound(1,6));
		bounds.add(new Bound(0,350));
		bounds.add(new Bound(0,350));

		return gd;
	}
	
	
	private boolean checkBounds(Matrix parms) {
		for (int i = 0; i < parms.m_iRows; i++) {
			if (!bounds.get(i).checkValue(parms.getValueAt(i, 0))) {
				return false;
			} 
		}
		return true;
	}

	public double getPrediction(int ageYr, int timeSinceInitialDateYr, String outputType) throws MetaModelException {
		if (converged) {
			if (!model.dummyOriginalValues.contains(outputType)) {
				throw new InvalidParameterException("This output type " + outputType + " is not recognized!");
			}
			Matrix dummy = model.dummyMap.get(outputType);
			// TODO optimize this
			Matrix b1 = InnerModel.getGeneralParm(dummy, finalParmEstimates, 0);
			Matrix b2 = InnerModel.getGeneralParm(dummy, finalParmEstimates, 2);
			Matrix b3 = InnerModel.getGeneralParm(dummy, finalParmEstimates, 4);
			double pred = InnerModel.getPrediction(b1.getValueAt(0, 0), 
					b2.getValueAt(0, 0), 
					b3.getValueAt(0, 0), 
					ageYr, 
					0d);
			return pred;
		} else {
			throw new MetaModelException("The meta-model has not converged or has not been fitted yet!");
		}
	}
	
	protected Matrix getFinalParameterEstimates() {
		return finalParmEstimates;
	}
  	
	/**
	 * Export the initial data set (before fitting the meta-model).
	 * @param filename
	 * @throws Exception
	 */
	public void exportInitialDataSet(String filename) throws Exception {
		getDataStructureReady().getDataSet().save(filename);
	}
	
	/**
	 * Export a final dataset, that is the initial data set plus the meta-model 
	 * predictions. This works only if the model has converged.
	 * @param filename
	 * @throws IOException
	 */
	public void exportFinalDataSet(String filename) throws IOException {
		if (converged) {
			dataSet.save(filename);
		}
	}

	/**
	 * Provide the list of possible output type for this meta-model.
	 * @return a List of String
	 */
	public List<String> getPossibleOutputTypes() {
		List<String> outputTypes = new ArrayList<String>();
		for (Object obj : model.dummyOriginalValues) {
			outputTypes.add(obj.toString());
		}
		return outputTypes;
	}

	
	
	/**
	 * Save a CSV file containing the final sequence produced by the Metropolis-Hastings algorithm, 
	 * that is without the burn-in period and with only every nth observation. The number of 
	 * burn-in samples to be dropped is set by the nbBurnIn member while every nth observation is 
	 * set by the oneEach member.
	 * @param filename
	 * @throws IOException
	 */
	public void exportMetropolisHastingsSample(String filename) throws IOException {
		if (converged) {
			CSVWriter writer = null;
			for (MetaModelGibbsSample sample : this.finalGibbsSample) {
				if (writer == null) {
					writer = new CSVWriter(new File(filename), false);
					List<FormatField> fieldNames = new ArrayList<FormatField>();
					fieldNames.add(new CSVField("LLK"));
					for (int j = 1; j <= sample.parms.m_iRows; j++) {
						fieldNames.add(new CSVField("p" + j));
					}
					writer.setFields(fieldNames);
				}
				Object[] record = new Object[sample.parms.m_iRows + 1];
				record[0] = sample.llk;
				for (int j = 1; j <= sample.parms.m_iRows; j++) {
					record[j] = sample.parms.getValueAt(j - 1, 0);
				}
				writer.addRecord(record);
			}
			writer.close();
		}
	}

	protected DataSet getFinalDataSet() {
		return dataSet;
	}

	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
	}

	/**
	 * Load a meta-model instance from file
	 * @param filename
	 * @return a MetaModel instance
	 * @throws IOException
	 */
	public static MetaModel Load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		MetaModel metaModel = (MetaModel) deserializer.readObject();
		if (metaModel.nbBurnIn == 0) { //saved under a former implementation where this variable was static
			metaModel.setDefaultSettings();
		}
		return metaModel;
	}
	
}
