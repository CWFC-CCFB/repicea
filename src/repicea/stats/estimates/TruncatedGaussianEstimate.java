/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.estimates;

import java.security.InvalidParameterException;

import repicea.math.Matrix;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.GaussianUtility;

/**
 * The TruncatedGaussianEstimate class allows to generate random deviates from a truncated Gaussian distribution. The bound of the 
 * distribution can set through the setLowerBound and setUpperBound methods. The random number generation is not analytical, ie. random
 * deviates are generated until they comply with the bounds. This way the calculation of the truncated pdf is avoided. However, it may
 * result in very long computations if the bounds are quite restrictive. 
 * @author Mathieu Fortin - August 2015
 */
@SuppressWarnings("serial")
public class TruncatedGaussianEstimate extends GaussianEstimate {

	protected static class Bound {
		
		private boolean isCompletelySet;
		
		private Matrix value;
		
		private Matrix pdfValue;
		
		private Matrix cdfValue;
		
		private boolean isUpperBound;
		
		private final GaussianDistribution originalDistribution;
		
		protected Bound(GaussianDistribution distribution, boolean isUpperBound) {
			this.originalDistribution = distribution;
		}
		
		protected void setBoundValue(Matrix value) {
			this.value = value;
			isCompletelySet = false;
		}
		
		protected synchronized Matrix getPdfValue() {
			if (!isCompletelySet) {
				update();
			}
			return pdfValue;
		}
		
		protected synchronized Matrix getCdfValue() {
			if (!isCompletelySet) {
				update();
			} 
			return cdfValue;
		}

		private void update() {
			if (value == null) {
				pdfValue = new Matrix(1,1);
				Matrix cdfValue = new Matrix(1,1);
				if (isUpperBound) {
					cdfValue.m_afData[0][0] = 1d;
				} else {
					cdfValue.m_afData[0][0] = 0d;
				}
				this.cdfValue = cdfValue;
			} else {
				Matrix pdfValue = new Matrix(1,1);
				pdfValue.m_afData[0][0] = GaussianUtility.getProbabilityDensity(value, originalDistribution);
				Matrix cdfValue = new Matrix(1,1);
				double standardizedValue = (value.m_afData[0][0] - originalDistribution.getMean().m_afData[0][0])/Math.sqrt(originalDistribution.getVariance().m_afData[0][0]);
				cdfValue.m_afData[0][0] = GaussianUtility.getCumulativeProbability(standardizedValue);
			}
			isCompletelySet = true;
		}
		
	}
	
	
	private final Bound lowerBound;
	private final Bound upperBound;

	/**
	 * Basic constructor with mu set to 0 and sigma2 set to 1.
	 */
	public TruncatedGaussianEstimate() {
		super();
		lowerBound = new Bound(getDistribution(), false);	// false: lower bound
		upperBound = new Bound(getDistribution(), true);	// true: upper bound
	}

	/**
	 * Constructor 2 with user specified mu and sigma2.
	 * @param mean a Matrix instance
	 * @param variance a Matrix instance
	 */
	public TruncatedGaussianEstimate(Matrix mu, Matrix sigma2) {
		super(mu, sigma2);
		lowerBound = new Bound(getDistribution(), false);
		upperBound = new Bound(getDistribution(), true);
	}
	
	/**
	 * This method sets the lower bound of the truncated distribution. Setting the lower bound to null simply removes the bound.
	 * @param lowerBoundValue a Matrix instance
	 */
	public void setLowerBound(Matrix lowerBoundValue) {
		if (upperBound.value != null && lowerBoundValue.subtract(upperBound.value).anyElementLargerThan(0)) {
			throw new InvalidParameterException("The lower bound is larger than the upper bound !");
		} else {
			this.lowerBound.value = lowerBoundValue;
		}
	}
	
	/**
	 * This method sets the upper bound of the truncated distribution. Setting the upper bound to null simply removes the bound.
	 * @param upperBoundValue a Matrix instance
	 */
	public void setUpperBound(Matrix upperBoundValue) {
		if (lowerBound.value != null && lowerBound.value.subtract(upperBoundValue).anyElementLargerThan(0)) {
			throw new InvalidParameterException("The upper bound is smaller than the lower bound !");
		} else {
			this.upperBound.value = upperBoundValue;
		}
	}
	

	@Override
	public Matrix getRandomDeviate() {
//		int i = 0;
//		boolean found = false;
//		Matrix deviate = null;
//		while (!found && i < 50) {
//			deviate = getDistribution().getRandomRealization();
//			if (complyWithLowerBound(deviate) && complyWithUpperBound(deviate)) {
//				found = true;
//			}
//			i++;
//		}
//		if (!found) {
//			throw new InvalidParameterException("Finding a deviate in this truncated distribution seems to be hardly achievable! Please check the bounds.");
//		} else {
//			return deviate;
//		}
		return null;
	}
	
	@Override
	public Matrix getMean() {
		
		return super.getMean();
	}
	
	

	
	

}
