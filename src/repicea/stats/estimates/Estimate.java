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

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.RandomVariable;

/**
 * The Estimate class is the basic class for all estimates.
 * @author Mathieu Fortin - March 2012
 * @param <D> a Distribution derived instance which represents the assumed distribution for the estimate
 */
public abstract class Estimate<D extends Distribution> extends RandomVariable<D> {
	
	private static final long serialVersionUID = 20120825L;
	
	protected EstimatorType estimatorType;
	
	/**
	 * The type of estimator.
	 * @author Mathieu Fortin - March 2012
	 */
	public static enum EstimatorType {MonteCarlo, LeastSquares, LikelihoodBased, MomentBased, Unknown}

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
	public Matrix getRandomDeviate() {
		return getDistribution().getRandomRealization();
	}

	
	/**
	 * This method returns an difference estimate.
	 * @param estimate2 an Estimate to be subtracted from this estimate.
	 * @return an Estimate
	 */
	public Estimate<?> getDifferenceEstimate(Estimate<?> estimate2) {
		if (this instanceof MonteCarloEstimate && ((MonteCarloEstimate) this).isCompatible(estimate2)) {
			try {
				return ((MonteCarloEstimate) this).subtract((MonteCarloEstimate) estimate2);
			} catch (Exception e) {}
		}
		Matrix diff = getMean().subtract(estimate2.getMean());
		Matrix variance = getVariance().add(estimate2.getVariance());
		return new GaussianEstimate(diff, variance);
	}
	
	/**
	 * This method returns a sum of two estimates.
	 * @param estimate2 an Estimate to be added to this estimate.
	 * @return an Estimate
	 */
	public Estimate<?> getSumEstimate(Estimate<?> estimate2) {
		if (this instanceof MonteCarloEstimate && ((MonteCarloEstimate) this).isCompatible(estimate2)) {
			try {
				return ((MonteCarloEstimate) this).add((MonteCarloEstimate) estimate2);
			} catch (Exception e) {}
		}
		Matrix diff = getMean().add(estimate2.getMean());
		Matrix variance = getVariance().add(estimate2.getVariance());
		return new GaussianEstimate(diff, variance);
	}

	
	/**
	 * This method returns an difference estimate.
	 * @param estimate2 an Estimate to be subtracted from this estimate.
	 * @return an Estimate
	 */
	public Estimate<?> getProductEstimate(double scalar) {
		if (this instanceof MonteCarloEstimate) {
			try {
				return ((MonteCarloEstimate) this).multiply(scalar);
			} catch (Exception e) {}
		}
		Matrix diff = getMean().scalarMultiply(scalar);
		Matrix variance = getVariance().scalarMultiply(scalar * scalar);
		return new GaussianEstimate(diff, variance);
	}

	/**
	 * This method returns the probability of getting a lower valueand upper bound of a confidence intervals at probability
	 * level 1 - alpha
	 * @param oneMinusAlpha is 1 minus the probability of Type I error
	 * @return a ConfidenceInterval instance 
	 */
	public abstract ConfidenceInterval getConfidenceIntervalBounds(double oneMinusAlpha);
	
}
