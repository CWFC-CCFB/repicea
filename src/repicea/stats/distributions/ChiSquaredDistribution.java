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
package repicea.stats.distributions;

import java.security.InvalidParameterException;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.stats.CentralMomentsSettable;
import repicea.stats.Distribution;
import repicea.stats.distributions.ChiSquaredDistribution.FunctionParameter;

/**
 * The ChiSquareDistribution class represents a univariate Chi Square distribution with a given degrees of freedom.
 * @author Mathieu Fortin - November 2012
 */
public final class ChiSquaredDistribution extends AbstractMathematicalFunction<FunctionParameter, Double, Integer, Double> implements Distribution<Double>, CentralMomentsSettable<Double> {

	private static final long serialVersionUID = 20121114L;

	protected static enum FunctionParameter {Mu, Sigma2}

	private int degreesOfFreedom;
	

	/**
	 * Common constructor.
	 * @param degreesOfFreedom the degrees of freedom
	 */
	public ChiSquaredDistribution(int degreesOfFreedom) {
		if (degreesOfFreedom < 1) {
			throw new InvalidParameterException("The number of degrees of freedom must be equal to or larger than 1!");
		}
		this.degreesOfFreedom = degreesOfFreedom;
	}

	/**
	 * Constructor with mean only. The variance is calculated as 2 / (2 + v) * meanEstimate ^ 2 with
	 * v being the degrees of freedom.
	 * @param degreesOfFreedom the degrees of freedom
	 * @param estimate the estimated mean
	 */
	public ChiSquaredDistribution(int degreesOfFreedom, double estimate) {
		this(degreesOfFreedom);
		if (estimate < 0) {
			throw new InvalidParameterException("The variance estimate must be larger than 0!");
		}
		double var = 2d / (2 + degreesOfFreedom) * estimate * estimate;
		setMean(estimate);
		setVariance(var);
	}


	/**
	 * Constructor with mean and variance.
	 * @param degreesOfFreedom the degrees of freedom
	 * @param estimate the estimate of the mean
	 * @param var the variance
	 */
	public ChiSquaredDistribution(int degreesOfFreedom, double estimate, double var) {
		this(degreesOfFreedom);
		if (estimate < 0) {
			throw new InvalidParameterException("The variance estimate must be larger than 0!");
		}
		if (var < 0) {
			throw new InvalidParameterException("The variance must be larger than 0!");
		}
		setMean(estimate);
		setVariance(var);
	}

	/**
	 * This method returns the degrees of freedom.
	 * @return an integer
	 */
	public int getDegreesOfFreedom() {
		return degreesOfFreedom;
	}
	
	@Override
	public Double getMean() {
		return getParameterValue(FunctionParameter.Mu);
	}

	@Override
	public Double getVariance() {
		return getParameterValue(FunctionParameter.Sigma2);
	}


	@Override
	public boolean isParametric() {return true;}

	@Override
	public boolean isMultivariate() {return false;}

	@Override
	public Type getType() {return Type.CHI_SQUARE;}

	@Override
	public Double getRandomRealization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getValue() {
		// TODO Auto-generated method stub
		return 0d;
	}

	@Override
	public Matrix getGradient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getHessian() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setMean(Double mean) {
		setParameterValue(FunctionParameter.Mu, mean);
	}

	@Override
	public void setVariance(Double variance) {
		setParameterValue(FunctionParameter.Sigma2, variance);
	}

}
