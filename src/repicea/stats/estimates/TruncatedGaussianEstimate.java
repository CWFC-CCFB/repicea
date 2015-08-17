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

import repicea.math.Matrix;
import repicea.stats.distributions.TruncatedGaussianDistribution;

/**
 * The TruncatedGaussianEstimate class allows to generate random deviates from a truncated Gaussian distribution. The bound of the 
 * distribution can set through the setLowerBound and setUpperBound methods. 
 * @author Mathieu Fortin - August 2015
 */
@SuppressWarnings("serial")
public class TruncatedGaussianEstimate extends Estimate<TruncatedGaussianDistribution> {


	/**
	 * Basic constructor with mu set to 0 and sigma2 set to 1.
	 */
	public TruncatedGaussianEstimate() {
		super(new TruncatedGaussianDistribution());
	}

	/**
	 * Constructor 2 with user specified mu and sigma2.
	 * @param mu a Matrix instance
	 * @param sigma2 a Matrix instance
	 */
	public TruncatedGaussianEstimate(Matrix mu, Matrix sigma2) {
		super(new TruncatedGaussianDistribution(mu, sigma2));
	}
	
	/**
	 * This method sets the lower bound of the truncated distribution. Setting the lower bound to null simply removes the bound.
	 * @param lowerBoundValue a Matrix instance
	 */
	public void setLowerBound(Matrix lowerBoundValue) {
		getDistribution().setLowerBoundValue(lowerBoundValue);
	}
	
	/**
	 * This method sets the upper bound of the truncated distribution. Setting the upper bound to null simply removes the bound.
	 * @param upperBoundValue a Matrix instance
	 */
	public void setUpperBound(Matrix upperBoundValue) {
		getDistribution().setUpperBoundValue(upperBoundValue);
	}
	

}
