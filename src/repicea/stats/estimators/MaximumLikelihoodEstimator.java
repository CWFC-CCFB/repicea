/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.estimators;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.math.optimizer.AbstractOptimizer.LineSearchMethod;
import repicea.math.optimizer.AbstractOptimizer.OptimizationException;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.CompositeLogLikelihood;
import repicea.stats.model.StatisticalModel;
import repicea.util.REpiceaLogManager;

/**
 * Implements a maximum likelihood estimator based on 
 * the Newton-Raphson algorithm.
 * @author Mathieu Fortin - August 2011
 */
public class MaximumLikelihoodEstimator extends AbstractEstimator {
	
	public static String LOGGER_NAME = "MLEstimator";
	protected GaussianEstimate parameterEstimate;
	protected final repicea.math.optimizer.NewtonRaphsonOptimizer nro;

	/**
	 * Constructor. 
	 */
	public MaximumLikelihoodEstimator() {
		nro = new repicea.math.optimizer.NewtonRaphsonOptimizer();
	}
	
	
	@Override
	public boolean doEstimation(StatisticalModel<? extends StatisticalDataStructure> model) throws EstimatorException {
		nro.setConvergenceCriterion(model.getConvergenceCriterion());
		dataStruct = model.getDataStructure();
		
		CompositeLogLikelihood llk = model.getCompleteLogLikelihood();
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < model.getParameters().m_iRows; i++) {
			indices.add(i);
		}
		try {
			REpiceaLogManager.logMessage(LOGGER_NAME, Level.INFO, LOGGER_NAME, "Starting optimization");
			nro.optimize(llk, indices);
		} catch (OptimizationException e) {
			REpiceaLogManager.logMessage(LOGGER_NAME, Level.SEVERE, LOGGER_NAME, e.getMessage());
			parameterEstimate = null;
			return false;
		}
		if (nro.isConvergenceAchieved()) {
			Matrix varCov = nro.getHessianAtMaximum().getInverseMatrix().scalarMultiply(-1d);
			parameterEstimate = new GaussianEstimate(nro.getParametersAtMaximum(), varCov);
			return true;
		} else {
			parameterEstimate = null;
			return false;
		}
		
	}

	/**
	 * Returns the maximum log likelihood value after convergence.
	 * @return a double
	 */
	public double getMaximumLogLikelihood() {return nro.getOptimalValue();}


	@Override
	public boolean isConvergenceAchieved() {return nro.isConvergenceAchieved();}

	@Override
	public Estimate<?> getParameterEstimates() {
		return parameterEstimate;
	}
	
	@Override
	public String toString() {return "Maximum likelihood estimator";}

	/**
	 * Sets the line search method. <br>
	 * <br>
	 *  
	 * If the lsm parameter is null,
	 * the line search method is set to LineSearchMethod.TEN_EQUAL 
	 * by default.
	 * @param lsm a LineSearchMethod enum
	 */
	public void setLineSearchMethod(LineSearchMethod lineSearchMethod) {
		nro.setLineSearchMethod(lineSearchMethod);
	}
	
	@Override
	public DataSet getConvergenceStatusReport() {
		if (dataStruct == null) {
			throw new UnsupportedOperationException("The doEstimation method should be called first!");
		}
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(3);
		formatter.setMinimumFractionDigits(3);
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("Element");
		fieldNames.add("Value");
		DataSet dataSet = new DataSet(fieldNames);
		Object[] record = new Object[2];
		record[0] = "Converged";
		record[1] = isConvergenceAchieved();
		dataSet.addObservation(record);
		record[0] = "Maximum log-likelihood";
		record[1] = getMaximumLogLikelihood();
		dataSet.addObservation(record);
		record[0] = "AIC";
		record[1] = - 2 * getMaximumLogLikelihood() + 2 * nro.getParametersAtMaximum().getNumberOfElements();
		dataSet.addObservation(record);
		record[0] = "BIC";
		record[1] = - 2 * getMaximumLogLikelihood() + nro.getParametersAtMaximum().getNumberOfElements() * Math.log(dataStruct.getNumberOfObservations());
		dataSet.addObservation(record);
		return dataSet;
	}


	@Override
	public String getReport() {
		if (!isConvergenceAchieved()) {
			return "The log-likelihood function has not been or cannot be optimized.";
		} else {
			StringBuilder sb = new StringBuilder();
			DataSet convergenceDataset = getConvergenceStatusReport();
			sb.append(convergenceDataset.toString() + System.lineSeparator());
			DataSet parameterDataset = getParameterEstimatesReport();
			sb.append(parameterDataset.toString() + System.lineSeparator());
			return sb.toString();
		}

	}
}
