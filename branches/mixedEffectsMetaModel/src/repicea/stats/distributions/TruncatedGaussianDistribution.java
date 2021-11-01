/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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

import java.io.Serializable;
import java.security.InvalidParameterException;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.utility.GaussianUtility;

/**
 * A class for truncated gaussian distribution.
 * @author Mathieu Fortin
 */
public class TruncatedGaussianDistribution extends StandardGaussianDistribution implements BoundedDistribution {

	private static final long serialVersionUID = -8980153249116737564L;

	protected class Bound extends BasicBound implements Serializable {
		
		private static final long serialVersionUID = -7435897095155443037L;

		private boolean isCompletelySet;
		
		private double pdfValue;
		
		private double cdfValue;
		
		private double standardizedValue;
		
		protected Bound(boolean isUpperBound) {
			super(isUpperBound);
		}

		@Override
		protected void setBoundValue(Matrix value) {
			super.setBoundValue(value);
			isCompletelySet = false;
		}
		
		protected synchronized double getPdfValue() {
			if (!isCompletelySet) {
				update();
			}
			return pdfValue;
		}
		
		protected synchronized double getCdfValue() {
			if (!isCompletelySet) {
				update();
			} 
			return cdfValue;
		}
		
		private double getBoundValueDouble() {
			return getBoundValue().getValueAt(0, 0);
		}

		private void update() {
			if (getBoundValue() == null) {
				pdfValue = 0d;
				if (isUpperBound()) {
					cdfValue = 1d;
				} else {
					cdfValue = 0d;
				}
			} else {
				standardizedValue = (getBoundValueDouble() - TruncatedGaussianDistribution.this.getMu().getValueAt(0, 0)) / Math.sqrt(TruncatedGaussianDistribution.this.getSigma2().getValueAt(0, 0));
				pdfValue = GaussianUtility.getProbabilityDensity(standardizedValue);
				cdfValue = GaussianUtility.getCumulativeProbability(standardizedValue);
			}
			isCompletelySet = true;
		}
		
	}
	
	private final Bound lowerBound;
	private final Bound upperBound;

	/**
	 * Constructor 1. Truncated standard Gaussian distribution.
	 */
	public TruncatedGaussianDistribution() {
		this(0d,1d);
//		setMean(new Matrix(1,1));
//		Matrix sigma2 = new Matrix(1,1);
//		sigma2.setValueAt(0, 0, 1d);
//		setVariance(sigma2);
//		lowerBound = new Bound(false);	// false: lower bound
//		upperBound = new Bound(true);	// true: upper bound
	}

	
//	/**
//	 * Constructor 2. Truncated Gaussian distribution with mu different from 0 or sigma2 different from 1.
//	 * @param mu a Matrix instance
//	 * @param sigma2 a Matrix instance
//	 */
//	public TruncatedGaussianDistribution(Matrix mu, Matrix sigma2) {
//		super();
//		setMean(mu);
//		setVariance(sigma2);
//		lowerBound = new Bound(false);	// false: lower bound
//		upperBound = new Bound(true);	// true: upper bound
//	}

	/**
	 * Constructor 2. Truncated Gaussian distribution with mu different from 0 or sigma2 different from 1.
	 * @param mu the mean of the original distribution
	 * @param sigma2 the variance of the original distribution
	 */
	public TruncatedGaussianDistribution(double mu, double sigma2) {
		super();
		Matrix mean = new Matrix(1,1);
		mean.setValueAt(0, 0, mu);
		setMean(mean);
		Matrix variance = new Matrix(1,1);
		variance.setValueAt(0, 0, sigma2);
		setVariance(variance);
		lowerBound = new Bound(false);	// false: lower bound
		upperBound = new Bound(true);	// true: upper bound
	}

	
	
	@Override
	public Matrix getMean() {
		double z = upperBound.getCdfValue() - lowerBound.getCdfValue();
		double diff = (lowerBound.getPdfValue() - upperBound.getPdfValue()) / z * getStandardDeviation().getValueAt(0, 0);
		Matrix mean = this.getMu().scalarAdd(diff);
		return mean;
	}

	@Override
	public Matrix getVariance() {
		double zFactor = 1d / (upperBound.getCdfValue() - lowerBound.getCdfValue());
		double mult1;
		if (lowerBound.getBoundValue() != null) {
			mult1 = lowerBound.getPdfValue() * lowerBound.standardizedValue;
		} else {
			mult1 = lowerBound.getPdfValue();
		}
		double mult2;
		if (upperBound.getBoundValue() != null) {
			mult2 = upperBound.getPdfValue() * upperBound.standardizedValue;
		} else {
			mult2 = upperBound.getPdfValue();
		}
		double num1 = mult1 - mult2;
		double num2 = lowerBound.getPdfValue() - upperBound.getPdfValue();
		return getSigma2().scalarMultiply(1 + num1 * zFactor - (num2 * zFactor) * (num2 * zFactor));
	}


	@Override
	public Matrix getRandomRealization() {
		double random = StatisticalUtility.getRandom().nextDouble();
		double diff = (upperBound.getCdfValue() - lowerBound.getCdfValue()) * random + lowerBound.getCdfValue();
		Matrix deviate = new Matrix(1,1);
		deviate.setValueAt(0, 0, GaussianUtility.getQuantile(diff));
		deviate = deviate.multiply(getStandardDeviation()).add(getMu());
		return deviate;
	}

	@Override
	public void setLowerBoundValue(Matrix lowerBoundValue) {
		checkMatrixSize(lowerBoundValue);
		lowerBound.setBoundValue(lowerBoundValue);
	}

	private void checkMatrixSize(Matrix m) {
		if (m.getNumberOfElements() != 1) {
			throw new InvalidParameterException("The TruncatedGaussianDistribution is univariate! The bound should be a 1x1 Matrix instance!");
		}
	}
	
	@Override
	public void setUpperBoundValue(Matrix upperBoundValue) {
		checkMatrixSize(upperBoundValue);
		upperBound.setBoundValue(upperBoundValue);
	}

}
