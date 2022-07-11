/*
 * This file is part of the repicea library.
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

import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimators.Estimator;
import repicea.stats.estimators.Estimator.EstimatorException;

/**
 * The AbstractStatisticalModel class implements the StatisticalModel interface. It contains the
 * basic features for a StatisticalModel, namely a data structure, a log-likelihood function and an
 * optimizer.
 * @author Mathieu Fortin - August 2011
 * @param <D> a StatisticalDataStructure-derived class
 */
public abstract class AbstractStatisticalModel<D extends StatisticalDataStructure> implements StatisticalModel<D> {

	private Estimator estimator;
	private D dataStructure;
	
	private double convergenceCriterion;
	private Object optimizerParameters; 
	
	/**
	 * The complete model likelihood.
	 */
	protected CompositeLogLikelihood completeLLK;
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
	public CompositeLogLikelihood getCompleteLogLikelihood() {return completeLLK;}
	
	/**
	 * This method sets the log-likelihood function of the model. It is to be defined in the derived class, since the 
	 * log-likelihood function depends on the different features of the model.
	 */
	protected abstract void setCompleteLLK();
	
	/**
	 * This method sets the optimizer for the model.
	 * @param optimizer an Optimizer instance
	 */
	public void setOptimizer(Estimator optimizer) {this.estimator = optimizer;}
	
	@Override
	public Estimator getEstimator() {
		if (estimator == null) {
			setOptimizer(instantiateDefaultEstimator());
		}
		return estimator;
	}


	@Override
	public D getDataStructure() {return dataStructure;}
	
	/**
	 * This method defines the default optimizer which is to be specific to the derived classes.
	 * @return an Optimizer instance 
	 */
	protected abstract Estimator instantiateDefaultEstimator();

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
	public void doEstimation() {
		System.out.println("Optimization using " + getEstimator().toString() + ".");
		try {
			if (getEstimator().doEstimation(this)) {
				System.out.println("Convergence achieved!");
			} else {
				System.out.println("Unable to reach convergence.");
			}
		} catch (EstimatorException e) {
			System.out.println("An error occured while optimizing the log likelihood function.");
			e.printStackTrace();
		}
	}

	@Override
	public void getSummary() {
		System.out.println(toString());
		System.out.println("Model definition : " + getModelDefinition() + System.lineSeparator());
		System.out.println(estimator.getReport());
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
