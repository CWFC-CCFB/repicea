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

import repicea.math.Matrix;
import repicea.stats.estimators.Estimator;

/**
 * This interface defines the services provided by a statistical model.
 * @author Mathieu Fortin - October 2011
 */
public interface StatisticalModel { //<P extends StatisticalDataStructure> {

//	/**
//	 * This method returns the model log-likelihood function.
//	 * @return a LogLikelihood instance
//	 */
//	public CompositeLogLikelihood getCompleteLogLikelihood();
	public void setParameters(Matrix beta);
	public Matrix getParameters();
	
	/**
	 * This method returns the results of the fit on screen.
	 */
	public void getSummary();
	
	/**
	 * This method computes the parameter estimates.
	 */
	public void doEstimation();
	
	/**
	 * This method returns the model definition as entered by the user.
	 * @return a String
	 */
	public String getModelDefinition();
	
	
	/**
	 * This method returns the value of the convergence criterion.
	 * @return a double
	 */
	public double getConvergenceCriterion();
	
	/**
	 * This method returns the optimizer of the log-likelihood function.
	 * @return an Optimizer instance
	 */
	public Estimator getEstimator();
	
}
