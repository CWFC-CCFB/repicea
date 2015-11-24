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
public abstract class IndividualLikelihood extends AbstractMathematicalFunctionWrapper implements LikelihoodCompatible<Double> {

	protected double observedValue;

	protected IndividualLikelihood(AbstractStatisticalExpression originalFunction) {
		super(originalFunction);
	}
	
	@Override
	public AbstractStatisticalExpression getOriginalFunction() {return (AbstractStatisticalExpression) super.getOriginalFunction();}
	
	@Override
	public void setY(Double y) {
		this.observedValue = y;
	}

	/**
	 * This method returns the observed value.
	 * @return a double
	 */
	public double getObservedValue() {return observedValue;}
	 
	/**
	 * This method returns the prediction for this observation.
	 * @return a double
	 */
	public abstract double getPrediction();
	
	
	@Override
	public void setX(Matrix x) {
		getOriginalFunction().setX(x);
	}
	
	@Override
	public void setBeta(Matrix beta) {
		getOriginalFunction().setBeta(beta);
	}
	
	@Override
	public Matrix getBeta() {
		return getOriginalFunction().getBeta();
	}

}
