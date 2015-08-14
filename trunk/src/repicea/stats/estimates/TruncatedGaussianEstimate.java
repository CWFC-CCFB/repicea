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

/**
 * The TruncatedGaussianEstimate class allows to generate random deviates from a truncated Gaussian distribution. The bound of the 
 * distribution can set through the setLowerBound and setUpperBound methods. The random number generation is not analytical, ie. random
 * deviates are generated until they comply with the bounds. This way the calculation of the truncated pdf is avoided. However, it may
 * result in very long computations if the bounds are quite restrictive. 
 * @author Mathieu Fortin - August 2015
 */
@SuppressWarnings("serial")
public class TruncatedGaussianEstimate extends GaussianEstimate {

	private Matrix lowerBound;
	private Matrix upperBound;
	
	
	/**
	 * This method sets the lower bound of the truncated distribution. Setting the lower bound to null simply removes the bound.
	 * @param lowerBound a Matrix instance
	 */
	public void setLowerBound(Matrix lowerBound) {
		if (upperBound != null && upperBound.subtract(lowerBound).anyElementLargerThan(0)) {
			throw new InvalidParameterException("The lower bound is larger than the upper bound !");
		} else {
			this.lowerBound = lowerBound;
		}
	}
	
	/**
	 * This method sets the upper bound of the truncated distribution. Setting the upper bound to null simply removes the bound.
	 * @param upperBound a Matrix instance
	 */
	public void setUpperBound(Matrix upperBound) {
		if (lowerBound != null && upperBound.subtract(lowerBound).anyElementLargerThan(0)) {
			throw new InvalidParameterException("The upper bound is smaller than the lower bound !");
		} else {
			this.upperBound = upperBound;
		}
	}
	
	private boolean complyWithLowerBound(Matrix deviate) {
		if (lowerBound == null) {
			return true;
		} else {
			return !lowerBound.subtract(deviate).anyElementLargerThan(0d);
		}
	}

	private boolean complyWithUpperBound(Matrix deviate) {
		if (upperBound == null) {
			return true;
		} else {
			return !deviate.subtract(upperBound).anyElementLargerThan(0d);
		}
	}

	@Override
	public Matrix getRandomDeviate() {
		int i = 0;
		boolean found = false;
		Matrix deviate = null;
		while (!found && i < 50) {
			deviate = getDistribution().getRandomRealization();
			if (complyWithLowerBound(deviate) && complyWithUpperBound(deviate)) {
				found = true;
			}
			i++;
		}
		if (!found) {
			throw new InvalidParameterException("Finding a deviate in this truncated distribution seems to be hardly achievable! Please check the bounds.");
		} else {
			return deviate;
		}
	}

	

}
