/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.model.glm.measerr;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.stats.data.DataSet;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.stats.estimators.AbstractEstimator;
import repicea.stats.estimators.AbstractEstimator.EstimatorCompatibleModel;
import repicea.stats.model.glm.measerr.SIMEXModel.InternalGLM;
import repicea.util.REpiceaLogManager;

class SIMEXEstimator extends AbstractEstimator<EstimatorCompatibleModel> {

	public static String LOGGER_NAME = "SIMEXEstimator";
	
	class InternalWorker extends Thread {
		
		final InternalGLM model;
		final Map<Double, MonteCarloEstimate> estimateMap;
		final Map<Double, MonteCarloEstimate> varianceMap;
		EstimatorException estimatorException;
		
		InternalWorker(int id, InternalGLM model, Map<Double, MonteCarloEstimate> estimateMap, Map<Double, MonteCarloEstimate> varianceMap) {
			super("InternalWorker no " + id);
			this.model = model;
			this.estimateMap = estimateMap;
			this.varianceMap = varianceMap;
			start();
		}

		@Override
		public void run() {
			try {
				while (!this.isInterrupted()) {
					double factor = queue.take();
					if (Double.isNaN(factor))
						break;
					model.getCompleteLogLikelihood().generateMeasurementError(factor);
					model.doEstimation();
					if (model.getEstimator().isConvergenceAchieved()) {
						addRealizationToEstimate(estimateMap, varianceMap, factor, model);
					}
				}
			} catch (Exception e) {
				estimatorException = new EstimatorException(e.getMessage());
				SIMEXEstimator.this.interruptTasks();
			}
		}
	}


	private final BlockingQueue<Double> queue;
	private GaussianEstimate estimate;
	private boolean convergenceAchieved;
	private List<InternalWorker> threads;
	
	protected SIMEXEstimator(EstimatorCompatibleModel model) {
		super(model);
		queue = new LinkedBlockingQueue<Double>();
	}

	void interruptTasks() {
		if (threads != null) {
			for (InternalWorker t : threads) {
				t.interrupt();
			}
		}
	}

	protected SIMEXModel getModel() {return (SIMEXModel) model;}
	
	synchronized void addRealizationToEstimate(Map<Double, MonteCarloEstimate> estimateMap, 
			Map<Double, MonteCarloEstimate> varianceMap,
			double factor, 
			InternalGLM model) {
		if (!estimateMap.containsKey(factor)) {
			estimateMap.put(factor, new MonteCarloEstimate());
		}
		estimateMap.get(factor).addRealization(model.getParameters());
		if (!varianceMap.containsKey(factor)) {
			varianceMap.put(factor, new MonteCarloEstimate());
		}
		varianceMap.get(factor).addRealization(model.getEstimator().getParameterEstimates().getVariance());
	}

	
	@Override
	public boolean doEstimation() throws EstimatorException {
		convergenceAchieved = false;
		estimate = null;
		Map<Double, MonteCarloEstimate> varianceMap = new HashMap<Double, MonteCarloEstimate>();
		Map<Double, MonteCarloEstimate> estimateMap = new HashMap<Double, MonteCarloEstimate>();
		threads = new ArrayList<InternalWorker>();
		for (int id = 1; id <= getModel().nbThreads; id++) {
			threads.add(new InternalWorker(id, getModel().originalGLM.clone(), estimateMap, varianceMap));
		}
		for (Double factor : getModel().factors) {
			for (int b = 0; b < getModel().nbBootstrapRealizations; b++) {
				queue.add(factor);	// just one realization for the 0 factor
				if (factor == 0) {
					break;
				}
			}
		}
		for (@SuppressWarnings("unused") InternalWorker t : threads) 
			queue.add(Double.NaN);
		for (InternalWorker t : threads)
			try {
				t.join();
			} catch (InterruptedException e) {}

		for (InternalWorker t : threads) {
			if (t.estimatorException != null) {
				REpiceaLogManager.logMessage(LOGGER_NAME, Level.SEVERE, LOGGER_NAME, t.estimatorException.getMessage());
				return false;
			}
		}

		Matrix epsilon = new Matrix(getModel().factors);
		// A quadratic extrapolation x = [1, epsilon, epsilon2]
		Matrix x = new Matrix(epsilon.m_iRows, 1, 1, 0).matrixStack(epsilon, false).matrixStack(epsilon.elementWisePower(2), false);
		Matrix invXtX_Xt = x.transpose().multiply(x).getInverseMatrix().multiply(x.transpose());
		// Extrapolation is then at X = [1, -1, 1]
		Matrix extrapolation = new Matrix(1,3,1,0);
		extrapolation.setValueAt(0, 1, -1);
		extrapolation.setValueAt(0, 2, 1);
		Matrix parameters = null;
		Matrix variances = null;
		for (Double d : getModel().factors) {
			Matrix theseParms = estimateMap.get(d).getMean().transpose();
			Matrix theseVCov;
			SymmetricMatrix varianceMapResult = SymmetricMatrix.convertToSymmetricIfPossible(varianceMap.get(d).getMean());
			if (d == 0) {
				theseVCov = varianceMapResult.symSquare().transpose(); 
			} else {
				Matrix s2_delta = estimateMap.get(d).getVariance();
				theseVCov = ((SymmetricMatrix) varianceMapResult.subtract(s2_delta)).symSquare().transpose(); 
			}
			if (parameters == null) {
				parameters = theseParms;
				variances = theseVCov;
			} else { 
				parameters = parameters.matrixStack(theseParms, true);
				variances = variances.matrixStack(theseVCov, true);
			}
		}
		//			System.out.println("Parameters = " + parameters.toString());
		Matrix simexParms = new Matrix(parameters.m_iCols, 1);
		for (int j = 0; j < parameters.m_iCols; j++) {
			Matrix y = parameters.getSubMatrix(0, parameters.m_iRows - 1, j, j);
			Matrix beta = invXtX_Xt.multiply(y);
			Matrix simexValue = extrapolation.multiply(beta);
			simexParms.setValueAt(j, 0, simexValue.getValueAt(0, 0));
		}
		Matrix simexVCov = new Matrix(variances.m_iCols, 1);
		for (int j = 0; j < variances.m_iCols; j++) {
			Matrix y = variances.getSubMatrix(0, variances.m_iRows - 1, j, j);
			Matrix beta = invXtX_Xt.multiply(y);
			Matrix simexValue = extrapolation.multiply(beta);
			simexVCov.setValueAt(j, 0, simexValue.getValueAt(0, 0));
		}
		SymmetricMatrix simexVCovSymm = simexVCov.squareSym();
		estimate = new GaussianEstimate(simexParms, simexVCovSymm);
		convergenceAchieved = true;
		return true;
	}

	@Override
	public boolean isConvergenceAchieved() {return convergenceAchieved;}

	@Override
	public Estimate<?> getParameterEstimates() {return estimate;}

	@Override
	public DataSet getConvergenceStatusReport() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("Element");
		fieldNames.add("Value");
		DataSet dataSet = new DataSet(fieldNames);
		Object[] record = new Object[2];
		record[0] = "Converged";
		record[1] = isConvergenceAchieved();
		dataSet.addObservation(record);
		return dataSet;
	}
	
	@Override
	public String getReport() {
		if (!isConvergenceAchieved()) {
			return "Convergence could not be achieved!";
		} else {
			StringBuilder sb = new StringBuilder();
			DataSet convergenceDataset = getConvergenceStatusReport();
			DecimalFormat decFormat = new DecimalFormat();
			decFormat.setMaximumFractionDigits(4);
			decFormat.setMinimumFractionDigits(4);
			convergenceDataset.setFormatter(1, decFormat);
			sb.append(convergenceDataset.toString() + System.lineSeparator());
			DataSet parameterDataset = getParameterEstimatesReport();
			decFormat = new DecimalFormat();
			decFormat.setMaximumFractionDigits(6);
			decFormat.setMinimumFractionDigits(6);
			parameterDataset.setFormatter(1, decFormat);
			parameterDataset.setFormatter(2, decFormat);
			parameterDataset.setFormatter(4, decFormat);
			decFormat = new DecimalFormat();
			decFormat.setMaximumFractionDigits(3);
			decFormat.setMinimumFractionDigits(3);
			parameterDataset.setFormatter(3, decFormat);
			sb.append(parameterDataset.toString() + System.lineSeparator());
			return sb.toString();
		}

	}

	
}

