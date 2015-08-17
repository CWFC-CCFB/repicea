/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.CentralMomentsSettable;
import repicea.stats.Distribution;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.NonparametricDistribution;

/**
 * The HybridEstimate can be either a Likelihood based estimate or a MonteCarlo estimate depending on the parameters given in
 * the constructor.
 * @author Mathieu Fortin - June 2014
 */
@SuppressWarnings({"serial" })
public class HybridEstimate extends Estimate<Distribution> implements CentralMomentsSettable {

	private final Distribution alternateDistribution;
	
	/**
	 * Constructor 1 for default likelihood based estimate.
	 */
	public HybridEstimate() {
		this(0, false);
	}

	/**
	 * Constructor 2.
	 * @param numberOfRealizations the number of realizations (an integer)
	 * @param isMonteCarlo a boolean 
	 */
	public HybridEstimate(int numberOfRealizations, boolean isMonteCarlo) {
		super(new NonparametricDistribution());
		alternateDistribution = new GaussianDistribution(null, null);
		if (isMonteCarlo) {
			estimatorType = EstimatorType.MonteCarlo;
		} else {
			estimatorType = EstimatorType.LikelihoodBased;
		}
	}

	@Override
	public Distribution getDistribution() {
		if (estimatorType == EstimatorType.MonteCarlo) {
			return super.getDistribution();
		} else {
			return alternateDistribution;
		}
	}
	
	protected int getNumberOfRealizations() {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			return ((NonparametricDistribution) getDistribution()).getNumberOfRealizations();
		} else {
			return 0;		// TODO check if this works
		}
	}

	protected List<Matrix> getRealizations() {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			return ((NonparametricDistribution) getDistribution()).getRealizations();
		} else {
			return null;		// TODO check if this works
		}
	}

	@Override
	public void setMean(Matrix mean) {
		if (getEstimatorType() == EstimatorType.LikelihoodBased) {
			((GaussianDistribution) getDistribution()).setMean(mean);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to likelihood based!");
		}
	}

	@Override
	public void setVariance(Matrix variance) {
		if (getEstimatorType() == EstimatorType.LikelihoodBased) {
			((GaussianDistribution) getDistribution()).setVariance(variance);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to likelihood based!");
		}
	}

	/**
	 * This method records the realization of a particular Monte Carlo iteration
	 * @param realization the realization as a Matrix instance
	 */
	public void addRealization(Matrix realization) {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			((NonparametricDistribution) getDistribution()).addRealization(realization);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to Monte Carlo!");
		}
	}		
	
}
