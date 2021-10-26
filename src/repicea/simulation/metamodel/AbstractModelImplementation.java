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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.simulation.metamodel.MetaModel.ModelImplEnum;
import repicea.simulation.metamodel.MetaModel.SimulationParameters;
import repicea.stats.StatisticalUtility;
import repicea.stats.StatisticalUtility.TypeMatrixR;
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
 * A package class to handle the different types of meta-models (e.g. Chapman-Richards and others).
 * @author Mathieu Fortin - September 2021
 */
abstract class AbstractModelImplementation implements Runnable {

	@SuppressWarnings("serial")
	class DataBlockWrapper extends AbstractDataBlockWrapper {

		final Matrix varCovFullCorr;
		final Matrix distances;
		Matrix invVarCov;
		double lnConstant;

		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
			Matrix varCovTmp = overallVarCov.getSubMatrix(indices, indices);
			Matrix stdDiag = correctVarCov(varCovTmp).diagonalVector().elementWisePower(0.5);
			this.varCovFullCorr = stdDiag.multiply(stdDiag.transpose());
			distances = new Matrix(varCovFullCorr.m_iRows, 1, 1, 1);
		}

		@Override
		void updateCovMat(Matrix parameters) {
			double rhoParm = getCorrelationParameter();	
			Matrix corrMat = StatisticalUtility.constructRMatrix(distances, 1d, rhoParm, TypeMatrixR.POWER);
			Matrix varCov = varCovFullCorr.elementWiseMultiply(corrMat);

			Matrix invCorr = StatisticalUtility.getInverseCorrelationAR1Matrix(distances.m_iRows, rhoParm);
			Matrix invFull = varCovFullCorr.elementWisePower(-1d);
			invVarCov = invFull.elementWiseMultiply(invCorr);
			double determinant = varCov.getDeterminant();
			int k = this.vecY.m_iRows;
			this.lnConstant = -.5 * k * Math.log(2 * Math.PI) - Math.log(determinant) * .5;
		}

		@Override
		double getLogLikelihood() {
			Matrix pred = generatePredictions(this, getParameterValue(0));
			Matrix residuals = vecY.subtract(pred);
			Matrix rVr = residuals.transpose().multiply(invVarCov).multiply(residuals);
			double rVrValue = rVr.getSumOfElements();
			if (rVrValue < 0) {
				throw new UnsupportedOperationException("The sum of squared errors is negative!");
			} else {
				double llk = - 0.5 * rVrValue + lnConstant; 
				return llk;
			}
		}

		@Override
		double getMarginalLogLikelihood() {
			throw new UnsupportedOperationException("This model implementation " + getClass().getSimpleName() + " does not implement random effects!");
		}

	}


	private static final Map<Class<? extends AbstractModelImplementation>, ModelImplEnum> EnumMap = new HashMap<Class<? extends AbstractModelImplementation>, ModelImplEnum>();
	static {
		EnumMap.put(SimpleSlopeModelImplementation.class, ModelImplEnum.SimpleSlope);
		EnumMap.put(SimplifiedChapmanRichardsModelImplementation.class, ModelImplEnum.SimplifiedChapmanRichards);
		EnumMap.put(ChapmanRichardsModelImplementation.class, ModelImplEnum.ChapmanRichards);
		EnumMap.put(ChapmanRichardsModelWithRandomEffectImplementation.class, ModelImplEnum.ChapmanRichardsWithRandomEffect);
		EnumMap.put(ChapmanRichardsDerivativeModelImplementation.class, ModelImplEnum.ChapmanRichardsDerivative);
		EnumMap.put(ChapmanRichardsDerivativeModelWithRandomEffectImplementation.class, ModelImplEnum.ChapmanRichardsDerivativeWithRandomEffect);
	}
	
	protected final SimulationParameters simParms;
	protected final HierarchicalStatisticalDataStructure structure;
	protected final List<AbstractDataBlockWrapper> dataBlockWrappers;
	protected final String outputType;
	protected final String stratumGroup;
	
	protected ContinuousDistribution priors;
	private Matrix parameters;
	private Matrix parmsVarCov;
	protected List<Integer> fixedEffectsParameterIndices;
	protected double lnProbY;
	protected transient List<MetaModelMetropolisHastingsSample> finalMetropolisHastingsSampleSelection;
	private boolean converged;
	int indexCorrelationParameter;

	/**
	 * Internal constructor.
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 */
	AbstractModelImplementation(String outputType, MetaModel metaModel) throws StatisticalDataException {
		simParms = metaModel.simParms.clone();
		Map<Integer, ScriptResult> scriptResults = metaModel.scriptResults;
		String stratumGroup = metaModel.getStratumGroup();
		if (stratumGroup == null) {
			throw new InvalidParameterException("The argument stratumGroup must be non null!");
		}
		if (outputType == null) {
			throw new InvalidParameterException("The argument outputType must be non null!");
		}
		if (!MetaModel.getPossibleOutputTypes(scriptResults).contains(outputType)) {
			throw new InvalidParameterException("The outputType " + outputType + " is not part of the dataset!");
		}
		this.stratumGroup = stratumGroup;
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
			dataBlockWrappers.add(createWrapper(k, indices, structure, varCov));
		}
	}

	AbstractDataBlockWrapper createWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new DataBlockWrapper(k, indices, structure, varCov);
	}
	
	final Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect, boolean includePredVariance) {
		boolean canCalculateVariance = includePredVariance && getParmsVarCov() != null;
		Matrix mu;
		if (canCalculateVariance) {
			mu = new Matrix(dbw.vecY.m_iRows, 2);
		} else {
			mu = new Matrix(dbw.vecY.m_iRows, 1);
		}
		
		for (int i = 0; i < mu.m_iRows; i++) {
			mu.setValueAt(i, 0, getPrediction(dbw.ageYr.getValueAt(i, 0), dbw.timeSinceBeginning.getValueAt(i, 0), randomEffect));
			if (canCalculateVariance) {
				double predVar = getPredictionVariance(dbw.ageYr.getValueAt(i, 0), dbw.timeSinceBeginning.getValueAt(i, 0), randomEffect);
				mu.setValueAt(i, 1, predVar);
			}
		}
		return mu;
	}

	final double getCorrelationParameter() {
		return getParameters().getValueAt(indexCorrelationParameter, 0);
	}

	final ModelImplEnum getModelImplementation() {
		return EnumMap.get(getClass());
	}
	
	double getLogLikelihood(Matrix parameters) {
		setParameters(parameters);
		double logLikelihood = 0d;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			double logLikelihoodForThisBlock = dbw.getLogLikelihood();
			logLikelihood += logLikelihoodForThisBlock;
		}
		return logLikelihood;
	}
	
	/**
	 * Get the observations of a particular output type ready for the meta-model fitting. 
	 * @return a HierarchicalStatisticalDataStructure instance
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 * @throws StatisticalDataException
	 */
	private HierarchicalStatisticalDataStructure getDataStructureReady(String outputType, Map<Integer, ScriptResult> scriptResults) throws StatisticalDataException {
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

	String getSelectedOutputType() {
		return outputType;
	}
	
	private MetaModelMetropolisHastingsSample findFirstSetOfParameters(int desiredSize, boolean isForIntegral) {
		long startTime = System.currentTimeMillis();
		double llk = Double.NEGATIVE_INFINITY;
		List<MetaModelMetropolisHastingsSample> myFirstList = new ArrayList<MetaModelMetropolisHastingsSample>();
		while (myFirstList.size() < desiredSize) {
			Matrix parms = priors.getRandomRealization();
			llk = isForIntegral ? getLogLikelihood(parms) : getLogLikelihood(parms) + Math.log(priors.getProbabilityDensity(parms)); // if isForIntegral then there is no need for the density of the parameters since the random realizations account for the distribution of the prior 
			if (Math.exp(llk) > 0d) {
				myFirstList.add(new MetaModelMetropolisHastingsSample(parms.getDeepClone(), llk));
				if (myFirstList.size()%1000 == 0) {
					MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Initial sample list has " + myFirstList.size() + " sets.");
				}
			}
		}
 		Collections.sort(myFirstList);
		MetaModelMetropolisHastingsSample startingParms = myFirstList.get(myFirstList.size() - 1);
		MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Time to find a first set of plausible parameters = " + (System.currentTimeMillis() - startTime) + " ms");
		MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "LLK = " + startingParms.llk + " - Parameters = " + startingParms.parms);
		return startingParms;
	}

	private String getLogMessagePrefix() {
		return stratumGroup + " Implementation " + getModelImplementation().name();
	}


	/**
	 * Implement the Metropolis-Hastings algorithm.
	 * @param nbRealizations number of samples in the chain before removing burn in and selected one sample every x samples
	 * @param nbBurnIn number of samples to discard at the beginning of the chain
	 * @param nbInternalIter maximum number of realizations to find the next acceptable sample of the chain
	 * @param metropolisHastingsSample A list of MetaModelMetropolisHastingsSample instance that represents the chain
	 * @param gaussDist the sampling distribution
	 * @return a boolean
	 */
	private boolean generateMetropolisSample(List<MetaModelMetropolisHastingsSample> metropolisHastingsSample, GaussianDistribution gaussDist) {
		long startTime = System.currentTimeMillis();
		Matrix newParms = null;
		double llk = 0d;
		boolean completed = true;
		int trials = 0;
		int successes = 0;
		double acceptanceRatio; 
		for (int i = 0; i < simParms.nbRealizations - 1; i++) { // Metropolis-Hasting  -1 : the starting parameters are considered as the first realization
			gaussDist.setMean(metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).parms);
			if (i > 0 && i < simParms.nbBurnIn && i%500 == 0) {
				acceptanceRatio = ((double) successes) / trials;
				MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "After " + i + " realizations, the acceptance rate is " + acceptanceRatio);
				if (acceptanceRatio > 0.4) {	// then we must increase the CoefVar
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(1.2*1.2));
				} else if (acceptanceRatio < 0.2) {
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(0.8*0.8));
				}
				successes = 0;
				trials = 0;
			}
			if (i%100000 == 0) {
				MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Processing realization " + i + " / " + simParms.nbRealizations);
			}
			boolean accepted = false;
			int innerIter = 0;
			
			while (!accepted && innerIter < simParms.nbInternalIter) {
				newParms = gaussDist.getRandomRealization();
				double parmsPriorDensity = priors.getProbabilityDensity(newParms);
				if (parmsPriorDensity > 0d) {
					llk = getLogLikelihood(newParms) + Math.log(parmsPriorDensity);
					double ratio = Math.exp(llk - metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).llk);
					accepted = StatisticalUtility.getRandom().nextDouble() < ratio;
					trials++;
					if (accepted) {
						successes++;
					}
				}
				innerIter++;
			}
			if (innerIter >= simParms.nbInternalIter && !accepted) {
				MetaModelManager.logMessage(Level.SEVERE,  getLogMessagePrefix(), "Stopping after " + i + " realization");
				completed = false;
				break;
			} else {
				metropolisHastingsSample.add(new MetaModelMetropolisHastingsSample(newParms, llk));  // new set of parameters is recorded
				if (metropolisHastingsSample.size()%100 == 0) {
					MetaModelManager.logMessage(Level.FINEST, getLogMessagePrefix(), metropolisHastingsSample.get(metropolisHastingsSample.size() - 1));
				}
			}
		}
		
		if (completed) {
			acceptanceRatio = ((double) successes) / trials;
			MetaModelManager.logMessage(Level.INFO, getLogMessagePrefix(), "Time to obtain " + metropolisHastingsSample.size() + " samples = " + (System.currentTimeMillis() - startTime) + " ms");
			MetaModelManager.logMessage(Level.INFO, getLogMessagePrefix(), "Acceptance ratio = " + acceptanceRatio);
		} 
		return completed;
	}

	private List<MetaModelMetropolisHastingsSample> retrieveFinalSample(List<MetaModelMetropolisHastingsSample> metropolisHastingsSample) {
		List<MetaModelMetropolisHastingsSample> finalMetropolisHastingsGibbsSample = new ArrayList<MetaModelMetropolisHastingsSample>();
		MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Discarding " + simParms.nbBurnIn + " samples as burn in.");
		for (int i = simParms.nbBurnIn; i < metropolisHastingsSample.size(); i+= simParms.oneEach) {
			finalMetropolisHastingsGibbsSample.add(metropolisHastingsSample.get(i));
		}
		MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Selecting one every " + simParms.oneEach + " samples as final selection.");
		return finalMetropolisHastingsGibbsSample;
	}
	
	void fitModel() {
		double coefVar = 0.01;
		try {
			GaussianDistribution gaussDist = getStartingParmEst(coefVar);
			List<MetaModelMetropolisHastingsSample> mhSample = new ArrayList<MetaModelMetropolisHastingsSample>();
			MetaModelMetropolisHastingsSample firstSet = findFirstSetOfParameters(simParms.nbInitialGrid, false);	// false: not for integration
			mhSample.add(firstSet); // first valid sample
			boolean completed = generateMetropolisSample(mhSample, gaussDist);
			if (completed) {
				finalMetropolisHastingsSampleSelection = retrieveFinalSample(mhSample);
				MonteCarloEstimate mcmcEstimate = new MonteCarloEstimate();
				for (MetaModelMetropolisHastingsSample sample : finalMetropolisHastingsSampleSelection) {
					mcmcEstimate.addRealization(sample.parms);
				}

				Matrix finalParmEstimates = mcmcEstimate.getMean();
				Matrix finalVarCov = mcmcEstimate.getVariance();
				lnProbY = getLnProbY(finalParmEstimates, finalMetropolisHastingsSampleSelection, gaussDist);
				setParameters(finalParmEstimates);
				setParmsVarCov(finalVarCov);

				Matrix finalPred = getVectorOfPopulationAveragedPredictionsAndVariances();
				Object[] finalPredArray = new Object[finalPred.m_iRows];
				Object[] finalPredVarArray = new Object[finalPred.m_iRows];
				Object[] implementationArray = new Object[finalPred.m_iRows];
				for (int i = 0; i < finalPred.m_iRows; i++) {
					finalPredArray[i] = finalPred.getValueAt(i, 0);
					finalPredVarArray[i] = finalPred.getValueAt(i, 1);
					implementationArray[i] = getModelImplementation().name();
				}

				structure.getDataSet().addField("modelImplementation", implementationArray);
				structure.getDataSet().addField("pred", finalPredArray);
				structure.getDataSet().addField("predVar", finalPredVarArray);

				MetaModelManager.logMessage(Level.FINE, getLogMessagePrefix(), "Final sample had " + finalMetropolisHastingsSampleSelection.size() + " sets of parameters.");
				converged = true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			converged = false;
		} 
	}
	

	private double getLnProbY(Matrix point, 
			List<MetaModelMetropolisHastingsSample> posteriorSamples, 
			GaussianDistribution samplingDist) {
		double parmsPriorDensity = priors.getProbabilityDensity(point);
		double llkOfThisPoint = getLogLikelihood(point) + Math.log(parmsPriorDensity);
		double sumIntegrand = 0;
		double densityFromSamplingDist = 0;
		for (MetaModelMetropolisHastingsSample s : posteriorSamples) {
			samplingDist.setMean(s.parms);
			double ratio = Math.exp(llkOfThisPoint - s.llk);
			if (ratio > 1d) {
				ratio = 1;
			}
			densityFromSamplingDist = samplingDist.getProbabilityDensity(point); 
			sumIntegrand += ratio * densityFromSamplingDist;
		}
		sumIntegrand /= posteriorSamples.size();
		
		samplingDist.setMean(point);
		double sumRatio = 0d;
		int nbRealizations = posteriorSamples.size();
		for (int j = 0; j < nbRealizations; j++) {
			Matrix newParms = samplingDist.getRandomRealization();
			parmsPriorDensity = priors.getProbabilityDensity(newParms);
			double ratio;
			if (parmsPriorDensity > 0d) {
				double llk = getLogLikelihood(newParms) + Math.log(parmsPriorDensity);
				ratio = Math.exp(llk - llkOfThisPoint);
				if (ratio > 1d) {
					ratio = 1d;
				}
			} else {
				ratio = 0d;
			}
			sumRatio += ratio;
		}
		sumRatio /= nbRealizations;
		double pi_theta_y = sumIntegrand / sumRatio;
		double log_m_hat = llkOfThisPoint - Math.log(pi_theta_y);
		return log_m_hat;
	}

	boolean hasConverged() {return converged;}
	
	@Override
	public final void run() {
		fitModel();
	}
	
	void printSummary() {
		if (hasConverged()) {
			System.out.println("Model implementation: " + getModelImplementation().name());
//			System.out.println("Final log-likelihood = " + getLogLikelihood(getParameters()));
			System.out.println("Final marginal log-likelihood = " + lnProbY);
			System.out.println("Final parameters = ");
			System.out.println(getParameters().toString());
			System.out.println("Final standardError = ");
			Matrix diagStd = getParmsVarCov().diagonalVector().elementWisePower(0.5);
			System.out.println(diagStd.toString());
			System.out.println("Correlation matrix = ");
			Matrix corrMat = getParmsVarCov().elementWiseDivide(diagStd.multiply(diagStd.transpose()));
			System.out.println(corrMat);
		} else {
			System.out.println("The model has not converged!");
		}
	}

}