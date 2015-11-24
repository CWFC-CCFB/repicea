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

import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.stats.AbstractStatisticalExpression;


/**
 * The Likelihood class provides the basic services for all Likelihood classes
 * @author Mathieu Fortin - June 2011
 */
@SuppressWarnings("serial")
public abstract class Likelihood extends AbstractMathematicalFunctionWrapper {

	protected double observedValue;

	protected Likelihood(AbstractStatisticalExpression originalFunction) {
		super(originalFunction);
	}
	
	@Override
	public AbstractStatisticalExpression getOriginalFunction() {return (AbstractStatisticalExpression) super.getOriginalFunction();}
	

	protected void setObservedValue(double observedValue) {
		this.observedValue = observedValue;
	}

	/**
	 * This method returns the prediction for this observation.
	 * @return a double
	 */
	public abstract double getPrediction();
	
	
	/**
	 * This method sets the vector of explanatory variables. The method essentially
	 * relies on the setVariableValue() of the AbstractMathematicalFunction class.
	 * @param x a Matrix instance 
	 * @throws IllegalArgumentException if the parameter x is not a row vector
	 */
	public void setX(Matrix x) {
		getOriginalFunction().setX(x);
	}
	
	/**
	 * This method sets the vector of parameters. The method essentially relies on
	 * the setParameterValue() of the AbstractMathematicalFunction class.
	 * @param beta a Matrix instance
	 * @throws IllegalArgumentException if beta is not a column vector
	 */
	public void setBeta(Matrix beta) {
		getOriginalFunction().setBeta(beta);
	}
	
	/**
	 * This method returns the vector of parameters.
	 * @return a Matrix instance
	 */
	public Matrix getBeta() {
		return getOriginalFunction().getBeta();
	}

}
