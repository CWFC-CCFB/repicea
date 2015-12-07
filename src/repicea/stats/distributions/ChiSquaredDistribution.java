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

import repicea.math.Matrix;
import repicea.stats.CentralMomentsSettable;
import repicea.stats.Distribution;

/**
 * The ChiSquareDistribution class represents a univariate Chi Square distribution with a given degrees of freedom.
 * @author Mathieu Fortin - November 2012
 */
public final class ChiSquaredDistribution implements Distribution, CentralMomentsSettable {

	private static final long serialVersionUID = 20121114L;

	private int degreesOfFreedom;

	private Matrix mean;
	private Matrix variance;

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
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = estimate;
		setMean(mean);
		Matrix variance = new Matrix(1,1);
		variance.m_afData[0][0] = var;
		setVariance(variance);
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
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = estimate;
		setMean(mean);
		Matrix variance = new Matrix(1,1);
		variance.m_afData[0][0] = var;
		setVariance(variance);
	}

	/**
	 * This method returns the degrees of freedom.
	 * @return an integer
	 */
	public int getDegreesOfFreedom() {
		return degreesOfFreedom;
	}
	
	@Override
	public Matrix getMean() {
		return mean;
	}

	@Override
	public Matrix getVariance() {
		return variance;
	}


	@Override
	public boolean isParametric() {return true;}

	@Override
	public boolean isMultivariate() {return false;}

	@Override
	public Type getType() {return Type.CHI_SQUARE;}

	@Override
	public Matrix getRandomRealization() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setMean(Matrix mean) {
		this.mean = mean;
	}

	@Override
	public void setVariance(Matrix variance) {
		this.variance = variance;
	}

}
