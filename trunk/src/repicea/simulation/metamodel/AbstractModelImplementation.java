/*
 * This file is part of the repicea library.
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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.metamodel.MetaModel.ModelImplEnum;
import repicea.stats.data.DataBlock;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericHierarchicalStatisticalDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.Observation;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.distributions.ContinuousDistribution;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * A package class to handle the different type of meta-models (e.g. Richards-Chapman and others).
 * @author Mathieu Fortin - September 2021
 */
abstract class AbstractModelImplementation {

	protected ContinuousDistribution priors;
	protected final HierarchicalStatisticalDataStructure structure;
	private Matrix parameters;
	private Matrix parmsVarCov;
	protected final List<AbstractDataBlockWrapper> dataBlockWrappers;
	protected List<Integer> fixedEffectsParameterIndices;
	protected double lnProbY;
	protected final String outputType;

	/**
	 * Internal constructor.
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 */
	AbstractModelImplementation(String outputType, Map<Integer, ScriptResult> scriptResults) throws StatisticalDataException {
		this.structure = getDataStructureReady(outputType, scriptResults);
		Matrix varCov = getVarCovReady(outputType, scriptResults);

		this.outputType = outputType;
		Map<String, DataBlock> formattedMap = new LinkedHashMap<String, DataBlock>();
		Map<String, DataBlock> ageMap = structure.getHierarchicalStructure(); 
		for (String ageKey : ageMap.keySet()) {
			DataBlock db = ageMap.get(ageKey);
			for (String speciesGroupKey : db.keySet()) {
				DataBlock innerDb = db.get(speciesGroupKey);
				formattedMap.put(ageKey + "_" + speciesGroupKey, innerDb);
			}
		}

		dataBlockWrappers = new ArrayList<AbstractDataBlockWrapper>();
		for (String k : formattedMap.keySet()) {
			DataBlock db = formattedMap.get(k);
			List<Integer> indices = db.getIndices();
			dataBlockWrappers.add(createDataBlockWrapper(k, indices, structure, varCov));
		}
	}

	/**
	 * Get the observations of a particular output type ready for the meta-model fitting. 
	 * @return a HierarchicalStatisticalDataStructure instance
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 * @throws StatisticalDataException
	 */
	protected HierarchicalStatisticalDataStructure getDataStructureReady(String outputType, Map<Integer, ScriptResult> scriptResults) throws StatisticalDataException {
		if (outputType == null) {
			throw new InvalidParameterException("The argument outputType must be non null!");
		}
		if (!MetaModel.getPossibleOutputTypes(scriptResults).contains(outputType)) {
			throw new InvalidParameterException("The outputType " + outputType + " is not part of the dataset!");
		}
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
//				if (obsArray[outputTypeFieldNameIndex].equals(selectedOutputType)) {
				if (obsArray[outputTypeFieldNameIndex].equals(outputType)) {
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
	
	/**
	 * Format the variance-covariance matrix of the residual error term. 
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 * @return
	 */
	private Matrix getVarCovReady(String outputType, Map<Integer, ScriptResult> scriptResults) {
		Matrix varCov = null;
		for (int initAgeYr : scriptResults.keySet()) {
			ScriptResult r = scriptResults.get(initAgeYr);
			Matrix varCovI = r.getTotalVariance(outputType);
			if (varCov == null) {
				varCov = varCovI;
			} else {
				varCov = varCov.matrixDiagBlock(varCovI);
			}
		}
		return varCov;
	}

	
	abstract AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov);

	abstract Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect, boolean includePredVariance);

	final Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect) {
		return generatePredictions(dbw, randomEffect, false);
	}
	
	abstract double getPrediction(double ageYr, double timeSinceBeginning, double r1);
	
	abstract Matrix getFirstDerivative(double ageYr, double timeSinceBeginning, double r1);

	final double getPredictionVariance(double ageYr, double timeSinceBeginning, double r1) {
		if (parmsVarCov == null) {
			throw new InvalidParameterException("The variance-covariance matrix of the parameter estimates has not been set!");
		}
		Matrix firstDerivatives = getFirstDerivative(ageYr, timeSinceBeginning, r1);
		Matrix variance = firstDerivatives.transpose().multiply(parmsVarCov.getSubMatrix(fixedEffectsParameterIndices, fixedEffectsParameterIndices)).multiply(firstDerivatives);
		return variance.getValueAt(0, 0);
	}
	
	/**
	 * Return the loglikelihood for the model implementation. This likelihood is used in 
	 * the Metropolis-Hastings algorithm.
	 * @param parameters
	 * @return
	 */
	abstract double getLogLikelihood(Matrix parameters);

	void setParameters(Matrix parameters) {
		this.parameters = parameters;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			dbw.updateCovMat(this.parameters);
		}

	}
	
	Matrix getParameters() {
		return parameters;
	}
	
	Matrix getParmsVarCov() {
		return parmsVarCov;
	}
	
	void setParmsVarCov(Matrix m) {
		parmsVarCov = m;
	}

	Matrix getVectorOfPopulationAveragedPredictionsAndVariances() {
		int size = 0;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			size += dbw.indices.size();
		}
		Matrix predictions = new Matrix(size,2);
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			Matrix y_i = generatePredictions(dbw, 0d, true);
			for (int i = 0; i < dbw.indices.size(); i++) {
				int index = dbw.indices.get(i);
				predictions.setValueAt(index, 0, y_i.getValueAt(i, 0));
				predictions.setValueAt(index, 1, y_i.getValueAt(i, 1));
			}
		}
		return predictions;
	}

	abstract GaussianDistribution getStartingParmEst(double coefVar);

	double getParmsPriorDensity(Matrix parms) {
		return priors.getProbabilityDensity(parms);
	}

	String getSelectedOutputType() {
		return outputType;
	}
	
	
	
//	/**
//	 * Fit the meta-model.
//	 * @param outputType the output type the model will be fitted to (e.g. volumeAlive_Coniferous)
//	 * @return a boolean true if the model has converged or false otherwise
//	 */
//	boolean fitModel(String outputType) {
//		if (outputType == null) {
//			throw new InvalidParameterException("The arguments outputType and e must be non null!");
//		} 
////		selectedOutputType = outputType;
//
//		double coefVar = 0.01;
//		try {
////			HierarchicalStatisticalDataStructure dataStructure = getDataStructureReady();
////			model = getInnerModel(dataStructure);
//			GaussianDistribution gaussDist = model.getStartingParmEst(coefVar);
//			List<MetaModelMetropolisHastingsSample> gibbsSample = new ArrayList<MetaModelMetropolisHastingsSample>();
//			MetaModelMetropolisHastingsSample firstSet = findFirstSetOfParameters(gaussDist.getMean().m_iRows, nbInitialGrid);
//			gibbsSample.add(firstSet); // first valid sample
//			boolean completed = generateMetropolisSample(gibbsSample, gaussDist);
//			if (completed) {
//				finalMetropolisHastingsSampleSelection = retrieveFinalSample(gibbsSample);
//				MonteCarloEstimate mcmcEstimate = new MonteCarloEstimate();
//				for (MetaModelMetropolisHastingsSample sample : finalMetropolisHastingsSampleSelection) {
//					mcmcEstimate.addRealization(sample.parms);
//				}
//				
//				Matrix finalParmEstimates = mcmcEstimate.getMean();
//				Matrix finalVarCov = mcmcEstimate.getVariance();
//				double lnProbY = getLnProbY(finalParmEstimates, 
//						finalMetropolisHastingsSampleSelection, 
//						gaussDist);
//				model.lnProbY = lnProbY;
////				finalLLK = model.getLogLikelihood(finalParmEstimates);
//				model.setParameters(finalParmEstimates);
//				model.setParmsVarCov(finalVarCov);
//				
//				Matrix finalPred = model.getVectorOfPopulationAveragedPredictionsAndVariances();
//				Object[] finalPredArray = new Object[finalPred.m_iRows];
//				Object[] finalPredVarArray = new Object[finalPred.m_iRows];
//				for (int i = 0; i < finalPred.m_iRows; i++) {
//					finalPredArray[i] = finalPred.getValueAt(i, 0);
//					finalPredVarArray[i] = finalPred.getValueAt(i, 1);
//				}
//				
//				model.structure.getDataSet().addField("pred", finalPredArray);
//				model.structure.getDataSet().addField("predVar", finalPredVarArray);
//
//				displayMessage("Final sample had " + finalMetropolisHastingsSampleSelection.size() + " sets of parameters.");
//				converged = true;
//				printSummary();				
//			}
// 		} catch (Exception e1) {
// 			e1.printStackTrace();
// 			converged = false;
// 			selectedOutputType = null;
//		} 
//		return converged;
//	}

	
}
