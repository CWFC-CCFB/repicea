/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
package repicea.stats.mcmc;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.GaussianDistribution;

/**
 * The MetropolisHastingsSampler class is a Gaussian distribution 
 * that samples the parameter space in order to create the Monte Carlo
 * Markov Chain.
 * @author Mathieu Fortin - November 2021
 */
@SuppressWarnings("serial")
public class MetropolisHastingsSampler extends GaussianDistribution {

	private final List<Integer> truePriorParmsIndices;
	private final GaussianDistribution innerGaussianDistribution;
	
	/**
	 * Constructor. 
	 * @param mu the mean of the gaussian distribution
	 * @param sigma2 the variance of the gaussian distribution
	 * @param truePriorParmsIndices the list of non random effect parameters. If there is no random effect, this argument 
	 * be set to null.
	 */
	public MetropolisHastingsSampler(Matrix mu, Matrix sigma2, List<Integer> truePriorParmsIndices) {
		super(mu, sigma2);
		this.innerGaussianDistribution = new GaussianDistribution(0,1);
		if (truePriorParmsIndices != null) {
			this.truePriorParmsIndices = new ArrayList<Integer>();
			this.truePriorParmsIndices.addAll(truePriorParmsIndices);
		} else {
			this.truePriorParmsIndices = null;
		}
	}

	
	double getMarginalProbabilityDensity(Matrix point) {
		if (truePriorParmsIndices == null) {		// there is no random effects
			return getProbabilityDensity(point);
		} else {
			innerGaussianDistribution.setMean(getMean().getSubMatrix(truePriorParmsIndices, null));
			innerGaussianDistribution.setVariance(getVariance().getSubMatrix(truePriorParmsIndices, truePriorParmsIndices));
			return innerGaussianDistribution.getProbabilityDensity(point.getSubMatrix(truePriorParmsIndices, null));
		}
	}

	
	
	
}
