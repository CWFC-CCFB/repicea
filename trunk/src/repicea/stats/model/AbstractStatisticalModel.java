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
package repicea.stats.model;

import java.text.NumberFormat;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimates.Estimate;
import repicea.stats.optimizers.MaximumLikelihoodOptimizer;
import repicea.stats.optimizers.Optimizer;
import repicea.stats.optimizers.Optimizer.OptimizationException;

/**
 * The AbstractStatisticalModel class implements the StatisticalModel interface. It contains the
 * basic features for a StatisticalModel, namely a data structure, a log-likelihood function and an
 * optimizer.
 * @author Mathieu Fortin - August 2011
 * @param <D> a StatisticalDataStructure-derived class
 */
public abstract class AbstractStatisticalModel<D extends StatisticalDataStructure> implements StatisticalModel<D> {

	
	
	protected static abstract class ModelLogLikelihood implements LogLikelihood{}
	
	private Optimizer optimizer;
	private D dataStructure;
	
	private double convergenceCriterion;
	private Object optimizerParameters; 
	
	/**
	 * The complete model likelihood.
	 */
	protected ModelLogLikelihood overallLLK;
	private String modelDefinition;

	/**
	 * Default constructor.
	 */
	protected AbstractStatisticalModel(DataSet dataSet) {
		dataStructure = getDataStructureFromDataSet(dataSet);
		setConvergenceCriterion(1E-8);			// default value
	}
	
	
	/**
	 * This method returns the appropriate StatisticalDataStructure from the dataSet. It
	 * is the first instruction to be called in the constructor.
	 * @param dataSet a DataSet instance
	 * @return a StatisticalDataStructure derived instance
	 */
	protected abstract D getDataStructureFromDataSet(DataSet dataSet);


	@Override
	public LogLikelihood getLogLikelihood() {return overallLLK;}
	
	/**
	 * This method sets the log-likelihood function of the model. It is to be defined in the derived class, since the 
	 * log-likelihood function depends on the different features of the model.
	 */
	protected abstract void setOverallLLK();
	
	/**
	 * This method sets the optimizer for the model.
	 * @param optimizer an Optimizer instance
	 */
	public void setOptimizer(Optimizer optimizer) {this.optimizer = optimizer;}
	
	@Override
	public Optimizer getOptimizer() {
		if (optimizer == null) {
			setOptimizer(instantiateDefaultOptimizer());
		}
		return optimizer;
	}


	@Override
	public D getDataStructure() {return dataStructure;}
	
	/**
	 * This method defines the default optimizer which is to be specific to the derived classes.
	 * @return an Optimizer instance 
	 */
	protected abstract Optimizer instantiateDefaultOptimizer();

	/**
	 * This method sets the convergence criterion.
	 * @param convergenceCriterion a double
	 */
	public void setConvergenceCriterion(double convergenceCriterion) {this.convergenceCriterion = convergenceCriterion;}

	@Override
	public double getConvergenceCriterion() {return convergenceCriterion;}
	
	/**
	 * This method sets the parameter for the optimizer.
	 * @param optimizerParameters
	 */
	public void setOptimizerParameters(Object optimizerParameters) {this.optimizerParameters = optimizerParameters;}
	
	protected Object getOptimizerParameters() {return optimizerParameters;}
	
	@Override
	public void optimize() {
		System.out.println("Optimization using the " + getOptimizer().toString() + " algorithm.");
		try {
			if (getOptimizer().optimize(this)) {
				System.out.println("Convergence achieved!");
			} else {
				System.out.println("Unable to reach convergence.");
			}
		} catch (OptimizationException e) {
			System.out.println("An error occured while optimizing the log likelihood function.");
			e.printStackTrace();
		}
	}
	
	public void getSummary() {
		if (!getOptimizer().isConvergenceAchieved()) {
			System.out.println("The log-likelihood function has not been or cannot be optimized.");
		} else {
			System.out.println(toString());
			System.out.println("Model definition : " + getModelDefinition());
			if (optimizer instanceof MaximumLikelihoodOptimizer) {
				double maximumLogLikelihood = ((MaximumLikelihoodOptimizer) optimizer).getMaximumLogLikelihood();
				double AIC = - 2 * maximumLogLikelihood + 2 * getParameters().getNumberOfElements(); 
				double BIC = - 2 * maximumLogLikelihood + getParameters().getNumberOfElements() * Math.log(dataStructure.getNumberOfObservations());
				NumberFormat formatter = NumberFormat.getInstance();
				formatter.setMaximumFractionDigits(2);
				formatter.setMinimumFractionDigits(2);
				System.out.println("Log-likelihood : " + formatter.format(maximumLogLikelihood));
				System.out.println("AIC            : " + formatter.format(AIC));
				System.out.println("BIC            : " + formatter.format(BIC));
			}
			
			Estimate<?> parameterEstimates = optimizer.getParameters();
			
			Matrix report;
			boolean varianceAvailable = false;
			if (parameterEstimates.getVariance() != null) {
				Matrix std = parameterEstimates.getVariance().diagonalVector().elementwisePower(0.5);
				report = parameterEstimates.getMean().matrixStack(std, false);
				varianceAvailable = true;
			} else {
				report = parameterEstimates.getMean();
			}
			
			NumberFormat formatter = NumberFormat.getInstance();
			formatter.setMaximumFractionDigits(6);
			formatter.setMinimumFractionDigits(6);

			System.out.println("Parameter estimates");
			String output;
			for (int i = 0; i < report.m_iRows; i++) {
				output = "Parameter " + i + "; Estimate : " + formatter.format(report.m_afData[i][0]);
				if (varianceAvailable) {
					output = output.concat("; Standard error : " + formatter.format(report.m_afData[i][1]));
				}
				System.out.println(output);
			}
		}
	}

	@Override
	public String getModelDefinition() {return modelDefinition;}

	/**
	 * This method sets the model definition and computes the appropriate matrix from the data.
	 * @param modelDefinition a String that defines the model
	 * @throws StatisticalDataException
	 */
	protected void setModelDefinition(String modelDefinition) throws StatisticalDataException {
		this.modelDefinition = modelDefinition;
		getDataStructure().constructMatrices(modelDefinition);
	}
	

}
