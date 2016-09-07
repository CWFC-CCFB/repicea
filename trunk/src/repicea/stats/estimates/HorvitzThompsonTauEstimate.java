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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.UnknownDistribution;

/**
 * This class implements the classical Horvitz-Thompson estimator of the total (tau) in a
 * context of random sampling WITHOUT replacement. 
 * @author Mathieu Fortin - September 2016
 */
@SuppressWarnings("serial")
public class HorvitzThompsonTauEstimate extends Estimate<UnknownDistribution> {

	
	static class Observation {
		final Matrix observation;
		final double inclusionProbability;
		
		Observation(Matrix observation, double inclusionProbability) {
			if (inclusionProbability <= 0 || inclusionProbability >= 1d) {
				throw new InvalidParameterException("The inclusion probability must be larger than 0 and smaller than 1");
			}
			this.observation = observation;
			this.inclusionProbability = inclusionProbability;
		}
	}
	
	private final double populationSize;
	private final List<Observation> observations;
	private int nRows;
	private int nCols;
	
	protected HorvitzThompsonTauEstimate(double populationSize) {
		super(new UnknownDistribution());
		this.populationSize = populationSize;
		observations = new ArrayList<Observation>();
	}

	/**
	 * This methods add an observation to the sample. IMPORTANT: the inclusion probability
	 * is the individual inclusion probability of this unit. For instance, that would be the 
	 * area of the plot divided by the total area, and not the plot area multiplied by the sample
	 * size. This product is actually handled internally.
	 * @param observation a Matrix that contains the observation
	 * @param inclusionProbablity the INDIVIDUAL inclusion probability of the sample unit.
	 */
	public void addObservation(Matrix observation, double inclusionProbablity) {
		if (observation == null) {
			throw new InvalidParameterException("The observation matrix cannot be null!");
		}
		observations.add(new Observation(observation, inclusionProbablity));
		if (nCols == 0) {
			nCols = observation.m_iCols;
		}
		if (nRows == 0) {
			nRows = observation.m_iRows;
		}
	}

	@Override
	public Matrix getMean() {
		return getTotal().scalarMultiply(1d/populationSize);
	}

	@Override
	public Matrix getVariance() {
		return null;
	}
	
	/**
	 * This method returns the value of the Horvitz-Thompson estimator (tau hat).
	 * @return a Matrix instance
	 */
	public Matrix getTotal() {
		Matrix total = new Matrix(nRows, nCols);
		int sampleSize = observations.size();
		for (Observation observation : observations) {
			total = total.add(observation.observation.scalarMultiply(1d/(sampleSize * observation.inclusionProbability)));
		}
		return total;
	}

	/**
	 * This method returns the variance of the tau estimate.
	 * @return a Matrix
	 */
	public Matrix getVarianceTotal() {
		int n = observations.size();
		Observation obs_i;
		Observation obs_j;
		double pi_i;
		double pi_j;
		double pi_ij;
		Matrix varianceContribution;
		Matrix variance = null;
		for (int i = 0; i < observations.size(); i++) {
			for (int j = i; j < observations.size(); j++) {
				obs_i = observations.get(i);
				obs_j = observations.get(j);
				pi_i = n * obs_i.inclusionProbability;
				pi_j = n * obs_j.inclusionProbability;
				if (i == j) {
					varianceContribution = obs_i.observation.multiply(obs_i.observation.transpose()).scalarMultiply((1 - pi_i)/(pi_i*pi_i));
				} else {
					pi_ij = pi_i * (n-1) * obs_j.inclusionProbability / (1 - obs_i.inclusionProbability);
					double factor = (pi_ij - pi_i * pi_j)/(pi_i * pi_j * pi_ij);
					varianceContribution = obs_i.observation.multiply(obs_j.observation.transpose()).scalarMultiply(2 * factor);
				}
				if (variance == null) {
					variance = varianceContribution;
				} else {
					variance = variance.add(varianceContribution);
				}
			}
		}
		return variance;
	}
	
}
