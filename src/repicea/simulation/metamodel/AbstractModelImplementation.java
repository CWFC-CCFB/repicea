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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.metamodel.MetaModel.ModelImplEnum;
import repicea.stats.StatisticalUtility;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.data.DataBlock;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericHierarchicalStatisticalDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.Observation;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.mcmc.MetropolisHastingsAlgorithm;
import repicea.stats.mcmc.MetropolisHastingsCompatibleModel;

/**
 * A package class to handle the different types of meta-models (e.g. Chapman-Richards and others).
 * @author Mathieu Fortin - September 2021
 */
abstract class AbstractModelImplementation implements MetropolisHastingsCompatibleModel, Runnable {

	/**
	 * A nested class to handle blocks of repeated measurements.
	 * @author Mathieu Fortin - November 2021
	 */
	@SuppressWarnings("serial")
	class DataBlockWrapper extends AbstractDataBlockWrapper {

		final Matrix varCovFullCorr;
		final Matrix distances;
		Matrix invVarCov;
		double lnConstant;

		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				Matrix vectorY,
				Matrix matrixX,
				Matrix overallVarCov) {
			super(blockId, indices, vectorY, matrixX, overallVarCov);
			Matrix varCovTmp = overallVarCov.getSubMatrix(indices, indices);
			Matrix stdDiag = correctVarCov(varCovTmp).diagonalVector().elementWisePower(0.5);
			this.varCovFullCorr = stdDiag.multiply(stdDiag.transpose());
			distances = new Matrix(varCovFullCorr.m_iRows, 1, 1, 1);
		}

		@Override
		void updateCovMat(Matrix parameters) {
			double rhoParm = parameters.getValueAt(indexCorrelationParameter, 0);	
//			Matrix corrMat = StatisticalUtility.constructRMatrix(distances, 1d, rhoParm, TypeMatrixR.POWER);
			SymmetricMatrix corrMat = StatisticalUtility.constructRMatrix(Arrays.asList(new Double[] {1d, rhoParm}), TypeMatrixR.POWER, distances);
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
	
	protected final List<AbstractDataBlockWrapper> dataBlockWrappers;
	protected final String outputType;
	protected final String stratumGroup;
	protected final MetropolisHastingsAlgorithm mh;
	private Matrix parameters;
	private Matrix parmsVarCov;
	protected List<Integer> fixedEffectsParameterIndices;
	protected int indexCorrelationParameter;
	private DataSet finalDataSet;
	protected final boolean isVarianceErrorTermAvailable;
	
	/**
	 * Internal constructor.
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 */
	AbstractModelImplementation(String outputType, MetaModel metaModel) throws StatisticalDataException {
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
		HierarchicalStatisticalDataStructure structure = getDataStructureReady(outputType, scriptResults);
		Matrix varCov = getVarCovReady(outputType, scriptResults);
		isVarianceErrorTermAvailable = metaModel.isVarianceAvailable();

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
		Matrix vectorY = structure.constructVectorY();
		Matrix matrixX = structure.constructMatrixX();
		for (String k : formattedMap.keySet()) {
			DataBlock db = formattedMap.get(k);
			List<Integer> indices = db.getIndices();
			dataBlockWrappers.add(createWrapper(k, indices, vectorY, matrixX, varCov));
		}
		
		finalDataSet = structure.getDataSet();
		mh = new MetropolisHastingsAlgorithm(this, MetaModelManager.LoggerName, getLogMessagePrefix());
		mh.setSimulationParameters(metaModel.mhSimParms);
	}

	protected AbstractDataBlockWrapper createWrapper(String k, List<Integer> indices, Matrix vectorY, Matrix matrixX, Matrix varCov) {
		return new DataBlockWrapper(k, indices, vectorY, matrixX, varCov);
	}
	
	private Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect, boolean includePredVariance) {
		boolean canCalculateVariance = includePredVariance && mh.getParameterCovarianceMatrix() != null;
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

	protected final ModelImplEnum getModelImplementation() {
		return EnumMap.get(getClass());
	}

	@Override
	public final double getLogLikelihood(Matrix parameters) {
		setParameters(parameters);
		double logLikelihood = 0d;
		for (int i = 0; i < dataBlockWrappers.size(); i++) {
			double logLikelihoodForThisBlock = getLogLikelihoodForThisBlock(parameters, i);
			logLikelihood += logLikelihoodForThisBlock;
		}
		return logLikelihood;
	}
	
	
	protected double getLogLikelihoodForThisBlock(Matrix parameters, int i) {
		AbstractDataBlockWrapper dbw = dataBlockWrappers.get(i);
		return dbw.getLogLikelihood();
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
		dataStruct.setModelDefinition("Estimate ~ initialAgeYr + timeSinceInitialDateYr + (1 | initialAgeYr/OutputType)");
//		dataStruct.constructMatrices();
		return dataStruct;
	}
	
	/**
	 * Format the variance-covariance matrix of the residual error term. <br>
	 * <br>
	 * If the model does not provide the variance associated with the predictions,
	 * this method returns null.
	 * @param outputType the desired outputType to be modelled
	 * @param scriptResults a Map containing the ScriptResult instances of the growth simulation
	 * @return
	 */
	private Matrix getVarCovReady(String outputType, Map<Integer, ScriptResult> scriptResults) {
		Matrix varCov = null;
		for (int initAgeYr : scriptResults.keySet()) {
			ScriptResult r = scriptResults.get(initAgeYr);
			Matrix varCovI = r.computeVarCovErrorTerm(outputType);
			if (varCov == null) {
				varCov = varCovI;
			} else {
				varCov = varCov.matrixDiagBlock(varCovI);
			}
		}
		return varCov;
	}


	private Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect) {
		return generatePredictions(dbw, randomEffect, false);
	}
	
	final double getPrediction(double ageYr, double timeSinceBeginning, double r1) {
		return this.getPrediction(ageYr, timeSinceBeginning, r1, null);
	}
	
	abstract double getPrediction(double ageYr, double timeSinceBeginning, double r1, Matrix parameters);
	
	abstract Matrix getFirstDerivative(double ageYr, double timeSinceBeginning, double r1);

	final double getPredictionVariance(double ageYr, double timeSinceBeginning, double r1) {
		if (mh.getParameterCovarianceMatrix() == null) {
			throw new InvalidParameterException("The variance-covariance matrix of the parameter estimates has not been set!");
		}
		Matrix firstDerivatives = getFirstDerivative(ageYr, timeSinceBeginning, r1);
		Matrix variance = firstDerivatives.transpose().multiply(mh.getParameterCovarianceMatrix().getSubMatrix(fixedEffectsParameterIndices, fixedEffectsParameterIndices)).multiply(firstDerivatives);
		return variance.getValueAt(0, 0);
	}
	

	protected void setParameters(Matrix parameters) {
		this.parameters = parameters;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			dbw.updateCovMat(parameters);
		}

	}
	
	Matrix getParameters() {
		return parameters;
	}
	
	Matrix getParmsVarCov() {
		return parmsVarCov;
	}
	
	private void setParmsVarCov(Matrix m) {
		parmsVarCov = m;
	}

	private Matrix getVectorOfPopulationAveragedPredictionsAndVariances() {
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

	@Override
	public abstract GaussianDistribution getStartingParmEst(double coefVar);

	String getSelectedOutputType() {
		return outputType;
	}
	

	private String getLogMessagePrefix() {
		return stratumGroup + " Implementation " + getModelImplementation().name();
	}

	void fitModel() {
		mh.doEstimation();
		if (mh.isConvergenceAchieved()) {
			setParameters(mh.getFinalParameterEstimates());
			setParmsVarCov(mh.getParameterCovarianceMatrix());
			
			Matrix finalPred = getVectorOfPopulationAveragedPredictionsAndVariances();
			Object[] finalPredArray = new Object[finalPred.m_iRows];
			Object[] finalPredVarArray = new Object[finalPred.m_iRows];
			Object[] implementationArray = new Object[finalPred.m_iRows];
			for (int i = 0; i < finalPred.m_iRows; i++) {
				finalPredArray[i] = finalPred.getValueAt(i, 0);
				finalPredVarArray[i] = finalPred.getValueAt(i, 1);
				implementationArray[i] = getModelImplementation().name();
			}

			finalDataSet.addField("modelImplementation", implementationArray);
			finalDataSet.addField("pred", finalPredArray);
			finalDataSet.addField("predVar", finalPredVarArray);
		}
	}
	

	boolean hasConverged() {return mh.isConvergenceAchieved();}
	
	@Override
	public final void run() {
		fitModel();
	}
	
	String getSummary() {
		return mh.getReport();
//		if (hasConverged()) {
//			DataSet d = new DataSet(Arrays.asList(new String[] {"Parameter", "Value", "StdErr"}));
//			d.addObservation(new Object[]{"Model implementation", getModelImplementation().name(), ""});
//			d.addObservation(new Object[] {"Log pseudomarginal likelihood", mh.getLogPseudomarginalLikelihood(), ""});
//			for (int i = 0; i < parameters.m_iRows; i++) {
//				d.addObservation(new Object[] {"Beta" + i, parameters.getValueAt(i, 0), Math.sqrt(parmsVarCov.getValueAt(i, i))});
//			}
//			return d;
//		} else {
//			System.out.println("The model has not converged!");
//			return null;
//		}
	}
	
	DataSet getFinalDataSet() {
		return finalDataSet;
	}
	
	@Override
	public final int getNbSubjects() {
		return dataBlockWrappers.size();
	}
	
	@Override
	public final double getLikelihoodOfThisSubject(Matrix m, int i) {
		setParameters(m);
		return Math.exp(getLogLikelihoodForThisBlock(m, i));
	}
	
	@Override
	public int getNumberOfObservations() {return finalDataSet.getNumberOfObservations();}

	
}
