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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repicea.io.FormatField;
import repicea.io.Saveable;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.StatisticalUtility;
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

	static {
		repicea.serial.xml.XmlSerializerChangeMonitor.registerClassNameChange("repicea.simulation.metamodel.MetaModel$InnerModel", 
				"repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation");
		repicea.serial.xml.XmlSerializerChangeMonitor.registerClassNameChange("repicea.simulation.metamodel.DataBlockWrapper", 
				"repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation$DataBlockWrapper");
	}
	
	protected static boolean Verbose = false; 
	
	public static enum ModelImplEnum {
		ChapmanRichards,
		ChapmanRichardsWithRandomEffect,
		ChapmanRichardsDerivative,
		ChapmanRichardsDerivativeWithRandomEffect;
	}
	
	private int nbBurnIn = 5000;
	private int nbRealizations = 500000 + nbBurnIn;
	private int nbInternalIter = 100000;
	private int oneEach = 50;
	private int nbInitialGrid = 10000;	

	private double coefVar;
	private boolean converged;
	
	private final Map<Integer, ScriptResult> scriptResults;
	private AbstractModelImplementation model;
//	private Matrix finalParmEstimates;
//	private Matrix finalVarCov;
	private double finalLLK;
	private final String stratumGroup;
	private String selectedOutputType;
	private transient List<MetaModelMetropolisHastingsSample> finalMetropolisHastingsSampleSelection;
	private DataSet dataSet;
	private ModelImplEnum modelImplEnum; 

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
		if (modelImplEnum == null) {
			modelImplEnum = ModelImplEnum.ChapmanRichardsWithRandomEffect;
		}
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

	/**
	 * Get the observation of a particular output type ready for the meta-model fitting. 
	 * @return a HierarchicalStatisticalDataStructure instance
	 * @throws StatisticalDataException
	 */
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
			int outputTypeFieldNameIndex = overallDataset.getFieldNames().indexOf(ScriptResult.OutputTypeFieldName);
			for (Observation obs : dataSet.getObservations()) {
				List<Object> newObs = new ArrayList<Object>();
				Object[] obsArray = obs.toArray();
				if (obsArray[outputTypeFieldNameIndex].equals(selectedOutputType)) {
					newObs.addAll(Arrays.asList(obsArray));
					newObs.add(initAgeYr);	// adding the initial age to the data set
					overallDataset.addObservation(newObs.toArray());
				}
			}
		}
		overallDataset.indexFieldType();
		HierarchicalStatisticalDataStructure dataStruct = new GenericHierarchicalStatisticalDataStructure(overallDataset, false);	// no sorting
		dataStruct.setInterceptModel(false); // no intercept
		dataStruct.constructMatrices("Estimate ~ initialAgeYr + timeSinceInitialDateYr + (1 | initialAgeYr/OutputType)");
		return dataStruct;
	}
	
	private Matrix getVarCovReady() {
		Matrix varCov = null;
		for (int initAgeYr : scriptResults.keySet()) {
			ScriptResult r = scriptResults.get(initAgeYr);
			Matrix varCovI = r.getTotalVariance(selectedOutputType);
			if (varCov == null) {
				varCov = varCovI;
			} else {
				varCov = varCov.matrixDiagBlock(varCovI);
			}
		}
		return varCov;
	}

	private AbstractModelImplementation getInnerModel(HierarchicalStatisticalDataStructure structure) {
		Matrix varCov = getVarCovReady();
		AbstractModelImplementation model;
		switch(modelImplEnum) {
		case ChapmanRichards:
			model = new ChapmanRichardsModelImplementation(structure, varCov);
			break;
		case ChapmanRichardsWithRandomEffect:
			model = new ChapmanRichardsModelWithRandomEffectImplementation(structure, varCov);
			break;
		case ChapmanRichardsDerivative:
			model = new ChapmanRichardsDerivativeModelImplementation(structure, varCov);
			break;
		case ChapmanRichardsDerivativeWithRandomEffect:
			model = new ChapmanRichardsDerivativeModelWithRandomEffectImplementation(structure, varCov);
			break;
		default:
			throw new InvalidParameterException("This ModelImplEnum " + modelImplEnum.name() + " has not been implemented yet!");
		}
		return model;
	}
	
	private MetaModelMetropolisHastingsSample findFirstSetOfParameters(int nrow, int desiredSize) {
		long startTime = System.currentTimeMillis();
		double llk = Double.NEGATIVE_INFINITY;
		List<MetaModelMetropolisHastingsSample> myFirstList = new ArrayList<MetaModelMetropolisHastingsSample>();
		while (myFirstList.size() < desiredSize) {
			Matrix parms = model.getRandomValueBetweenBounds();
			llk = model.getLogLikelihood(parms);
			if (Math.exp(llk) > 0d) {
				myFirstList.add(new MetaModelMetropolisHastingsSample(parms.getDeepClone(), llk));
				int nbSamples = myFirstList.size();
				if (nbSamples%1000 == 0) {
					displayMessage("Initial sample list has " + myFirstList.size() + " sets.");
				}
			}
		}
		Collections.sort(myFirstList);
		MetaModelMetropolisHastingsSample startingParms = myFirstList.get(myFirstList.size() - 1);
		displayMessage("Time to find a first set of plausible parameters = " + (System.currentTimeMillis() - startTime) + " ms");
		if (MetaModel.Verbose) {
			displayMessage("LLK = " + startingParms.llk + " - Parameters = " + startingParms.parms);
		}
		return startingParms;
	}

	/**
	 * Implement a pure Metropolis-Hastings algorithm.
	 * @param metropolisHastingsSample
	 * @param gaussDist
	 * @return
	 */
	private boolean generateMetropolisSample(List<MetaModelMetropolisHastingsSample> metropolisHastingsSample, GaussianDistribution gaussDist) {
		long startTime = System.currentTimeMillis();
		Matrix newParms = null;
		double llk = 0d;
		boolean completed = true;
		int trials = 0;
		int successes = 0;
		double acceptanceRatio; 
		for (int i = 0; i < nbRealizations - 1; i++) { // Metropolis-Hasting  -1 : the starting parameters are considered as the first realization
			gaussDist.setMean(metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).parms);
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
			if (i%10000 == 0) {
				displayMessage("Processing realization " + i + " / " + nbRealizations);
			}
			boolean accepted = false;
			int innerIter = 0;
			
			while (!accepted && innerIter < nbInternalIter) {
				newParms = gaussDist.getRandomRealization();
				if (model.checkBounds(newParms)) {
					llk = model.getLogLikelihood(newParms);
					double ratio = Math.exp(llk - metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).llk);
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
				metropolisHastingsSample.add(new MetaModelMetropolisHastingsSample(newParms, llk));  // new set of parameters is recorded
				if (MetaModel.Verbose) {
					if (metropolisHastingsSample.size()%100 == 0) {
						displayMessage(metropolisHastingsSample.get(metropolisHastingsSample.size() - 1));
					}
				}
			}
		}
		
		acceptanceRatio = ((double) successes) / trials;
		
		displayMessage("Time to obtain " + metropolisHastingsSample.size() + " samples = " + (System.currentTimeMillis() - startTime) + " ms");
		displayMessage("Acceptance ratio = " + acceptanceRatio);
		return completed;
	}

	/**
	 * Return the possible output types given what they are in the ScriptResult
	 * instances.
	 * @return a List of String
	 * @throws MetaModelException
	 */
	public List<String> getPossibleOutputTypes() {
		List<String> possibleOutputTypes = new ArrayList<String>();
		if (!scriptResults.isEmpty()) {
			ScriptResult scriptRes = scriptResults.values().iterator().next();
			possibleOutputTypes.addAll(scriptRes.getOutputTypes());
		}
		return possibleOutputTypes;
	}
	
	/**
	 * Fit the meta-model.
	 * @param outputType the output type the model will be fitted to (e.g. volumeAlive_Coniferous)
	 * @param e a ModelImplEnum enum 
	 * @return a boolean true if the model has converged or false otherwise
	 */
	public boolean fitModel(String outputType, ModelImplEnum e) {
		if (e == null) {
			throw new InvalidParameterException("Argument e cannot be null!");
		} 
		this.modelImplEnum = e;

		if (!getPossibleOutputTypes().contains(outputType)) {
			throw new InvalidParameterException("This output type is not recognized: " + outputType);
		}
		selectedOutputType = outputType;
		coefVar = 0.01;
		boolean converged = false;
		dataSet = null;
		try {
			HierarchicalStatisticalDataStructure dataStructure = getDataStructureReady();
			model = getInnerModel(dataStructure);
			GaussianDistribution gaussDist = model.getStartingParmEst(coefVar);
			List<MetaModelMetropolisHastingsSample> gibbsSample = new ArrayList<MetaModelMetropolisHastingsSample>();
			MetaModelMetropolisHastingsSample firstSet = findFirstSetOfParameters(gaussDist.getMean().m_iRows, nbInitialGrid);
			gibbsSample.add(firstSet); // first valid sample
			boolean completed = generateMetropolisSample(gibbsSample, gaussDist);
			if (completed) {
				finalMetropolisHastingsSampleSelection = retrieveFinalSample(gibbsSample);
				MonteCarloEstimate mcmcEstimate = new MonteCarloEstimate();
				for (MetaModelMetropolisHastingsSample sample : finalMetropolisHastingsSampleSelection) {
					mcmcEstimate.addRealization(sample.parms);
				}
				
//				finalParmEstimates = mcmcEstimate.getMean();
//				finalLLK = model.getLogLikelihood(finalParmEstimates);
//				model.setParameters(finalParmEstimates);
//				finalVarCov = mcmcEstimate.getVariance();

				Matrix finalParmEstimates = mcmcEstimate.getMean();
				Matrix finalVarCov = mcmcEstimate.getVariance();
				finalLLK = model.getLogLikelihood(finalParmEstimates);
				model.setParameters(finalParmEstimates);
				model.setParmsVarCov(finalVarCov);
				
				Matrix finalPred = model.getVectorOfPopulationAveragedPredictionsAndVariances();
				Object[] finalPredArray = new Object[finalPred.m_iRows];
				Object[] finalPredVarArray = new Object[finalPred.m_iRows];
				for (int i = 0; i < finalPred.m_iRows; i++) {
					finalPredArray[i] = finalPred.getValueAt(i, 0);
					finalPredVarArray[i] = finalPred.getValueAt(i, 1);
				}
				
				dataSet = dataStructure.getDataSet();
				dataSet.addField("pred", finalPredArray);
				dataSet.addField("predVar", finalPredVarArray);

				displayMessage("Final sample had " + finalMetropolisHastingsSampleSelection.size() + " sets of parameters.");
				converged = true;
				printSummary();				
			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			converged = false;
 			selectedOutputType = null;
		} 
		this.converged = converged;
		return this.converged;
	}

	private void displayMessage(Object obj) {
		System.out.println("Meta-model " + stratumGroup + ": " + obj.toString());
	}
	
	private List<MetaModelMetropolisHastingsSample> retrieveFinalSample(List<MetaModelMetropolisHastingsSample> gibbsSample) {
		List<MetaModelMetropolisHastingsSample> finalGibbsSample = new ArrayList<MetaModelMetropolisHastingsSample>();
		displayMessage("Discarding " + this.nbBurnIn + " samples as burn in.");
		for (int i = nbBurnIn; i < gibbsSample.size(); i+= oneEach) {
			finalGibbsSample.add(gibbsSample.get(i));
		}
		displayMessage("Selecting one every " + this.oneEach + " samples as final selection.");
		return finalGibbsSample;
	}

	
	

	public double getPrediction(int ageYr, int timeSinceInitialDateYr) throws MetaModelException {
		if (converged) {
			double pred = model.getPrediction(ageYr, timeSinceInitialDateYr, 0d);
			return pred;
		} else {
			throw new MetaModelException("The meta-model has not converged or has not been fitted yet!");
		}
	}
	
	protected Matrix getFinalParameterEstimates() {
		return model.getParameters();
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
	 * Provide the selected output type that was set in the call to method
	 * fitModel.
	 * @return a String
	 */
	public String getSelectedOutputType() {return selectedOutputType;}

	
	
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
			for (MetaModelMetropolisHastingsSample sample : finalMetropolisHastingsSampleSelection) {
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
		Object obj = deserializer.readObject();
		MetaModel metaModel = (MetaModel) obj;
		if (metaModel.nbBurnIn == 0 || metaModel.modelImplEnum == null) { //saved under a former implementation where this variable was static
			metaModel.setDefaultSettings();
		}
		return metaModel;
	}

	public void printSummary() {
		if (converged) {
			System.out.println("Final log-likelihood = " + finalLLK);
			System.out.println("Final parameters = ");
			System.out.println(getFinalParameterEstimates().toString());
			System.out.println("Final standardError = ");
			Matrix diagStd = model.getParmsVarCov().diagonalVector().elementWisePower(0.5);
			System.out.println(diagStd.toString());
			System.out.println("Correlation matrix = ");
			Matrix corrMat = model.getParmsVarCov().elementWiseDivide(diagStd.multiply(diagStd.transpose()));
			System.out.println(corrMat);
		} else {
			System.out.println("The model has not converged!");
		}
	}

	MetaModelMetaData getMetaData() {
		//MetaModelMetaData.Growth growth = new MetaModelMetaData.Growth(); 
		return null;
	}
}
