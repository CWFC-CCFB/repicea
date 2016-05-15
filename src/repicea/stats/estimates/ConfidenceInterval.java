/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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

import repicea.math.Matrix;
import repicea.stats.distributions.BasicBound;

public class ConfidenceInterval {

	@SuppressWarnings("serial")
	class CIBound extends BasicBound {

		CIBound(boolean isUpperBound) {
			super(isUpperBound);
		}
		
		@Override
		protected void setBoundValue(Matrix value) {
			super.setBoundValue(value);
		}
		
		@Override
		protected Matrix getBoundValue() {
			return super.getBoundValue();
		}
	}
	
	private final CIBound lowerBound;
	private final CIBound upperBound;
	private final double probabilityLevel;
	
	protected ConfidenceInterval(Matrix lowerBoundValue, Matrix upperBoundValue, double probabilityLevel) {
		lowerBound = new CIBound(false);
		upperBound = new CIBound(true);
		lowerBound.setBoundValue(lowerBoundValue);
		upperBound.setBoundValue(upperBoundValue);
		this.probabilityLevel = probabilityLevel;
	}

	/**
	 * This method returns the confidence limit of this interval. 
	 * @return an array of two matrices. The first one is the lower bound and the second, the upper bound.
	 */
	public Matrix[] getConfidenceLimits() {
		Matrix[] limits = new Matrix[2];
		limits[0] = lowerBound.getBoundValue();
		limits[1] = upperBound.getBoundValue();
		return limits;
	}
	
	/**
	 * This method returns the probability level of the interval.
	 * @return a double
	 */
	public double getProbabilityLevel() {return probabilityLevel;}
	
}
