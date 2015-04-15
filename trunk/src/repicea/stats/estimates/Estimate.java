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
package repicea.stats.estimates;

import repicea.stats.Distribution;
import repicea.stats.RandomVariable;

/**
 * The Estimate class is the basic class for all estimates.
 * @author Mathieu Fortin - March 2012
 * @param <D> a Distribution derived instance which represents the assumed distribution for the estimate
 */
public abstract class Estimate<N extends Number, D extends Distribution<N>> extends RandomVariable<N,D> {
	
	private static final long serialVersionUID = 20120825L;
	
	protected EstimatorType estimatorType;
	
	/**
	 * The type of estimator.
	 * @author Mathieu Fortin - March 2012
	 */
	public static enum EstimatorType {MonteCarlo, LikelihoodBased, MomentBased, Unknown}

	protected Estimate(D distribution) {
		super(distribution);
	}
	
	
	/**
	 * This method returns the type of the estimator.
	 * @return an EstimatorType instance
	 */
	public EstimatorType getEstimatorType() {return estimatorType;}
	

	/**
	 * This method returns a random deviate from this estimate. This method
	 * is useful for Monte Carlo simulations.
	 * @return a deviate from the underlying distribution as a Matrix instance
	 */
	public N getRandomDeviate() {
		return getDistribution().getRandomRealization();
	}

}
