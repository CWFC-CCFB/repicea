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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import repicea.math.Matrix;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

/**
 * This class implements the classical Horvitz-Thompson estimator of the total (tau) in a
 * context of random sampling WITHOUT replacement. 
 * @author Mathieu Fortin - September 2016
 */
@SuppressWarnings("serial")
public class PopulationTotalEstimate extends PointEstimate<PopulationUnitWithUnequalInclusionProbability> {

//	protected final double populationSize;
	private final List<PopulationUnitWithUnequalInclusionProbability> observations;
	private int nRows;
	private int nCols;
	
	/**
	 * Constructor.
	 * @param the population size in terms of sampling units or eventually ha if the 
	 * response variable is expressed at this scale
	 */
//	public PopulationTotalEstimate(double populationSize) {
	public PopulationTotalEstimate() {
		super();
//		this.populationSize = populationSize;
		observations = new CopyOnWriteArrayList<PopulationUnitWithUnequalInclusionProbability>();
	}

	/**
	 * This methods add an observation to the sample. 
	 * @param observation a Matrix that contains the observation
	 * @param inclusionProbability the INDIVIDUAL inclusion probability of the sample unit.
	 */
	@Override
	public void addObservation(PopulationUnitWithUnequalInclusionProbability observation) {
		if (nCols == 0) {
			nCols = observation.getData().m_iCols;
		}
		if (nRows == 0) {
			nRows = observation.getData().m_iRows;
		}
		if (observation.getData().m_iCols != nCols || observation.getData().m_iRows != nRows) {
			throw new InvalidParameterException("The observation is incompatible with what was already observed!");
		} else {
			observations.add(observation);
		}
	}

//	protected double getPopulationSize() {return populationSize;}
	

	
	/**
	 * This method returns the value of the Horvitz-Thompson estimator (tau hat).
	 * @return a Matrix instance
	 */
	@Override
	public Matrix getMean() {
		Matrix total = new Matrix(nRows, nCols);
		int sampleSize = observations.size();
		for (PopulationUnitWithUnequalInclusionProbability observation : observations) {
			total = total.add(observation.getData().scalarMultiply(1d/(sampleSize * observation.getInclusionProbability())));
		}
		return total;
	}

	/**
	 * This method returns the variance of the tau estimate.
	 * @return a Matrix
	 */
	@Override
	public Matrix getVariance() {
		int n = observations.size();
		PopulationUnitWithUnequalInclusionProbability obs_i;
		PopulationUnitWithUnequalInclusionProbability obs_j;
		double pi_i;
		double pi_j;
		double pi_ij;
		Matrix varianceContribution;
		Matrix variance = null;
		for (int i = 0; i < observations.size(); i++) {
			for (int j = i; j < observations.size(); j++) {
				obs_i = observations.get(i);
				obs_j = observations.get(j);
				pi_i = n * obs_i.getInclusionProbability();
				pi_j = n * obs_j.getInclusionProbability();
				if (i == j) {
					varianceContribution = obs_i.getData().multiply(obs_i.getData().transpose()).scalarMultiply((1 - pi_i)/(pi_i*pi_i));
				} else {
					pi_ij = pi_i * (n-1) * obs_j.getInclusionProbability() / (1 - obs_i.getInclusionProbability());
					double factor = (pi_ij - pi_i * pi_j)/(pi_i * pi_j * pi_ij);
					varianceContribution = obs_i.getData().multiply(obs_j.getData().transpose()).scalarMultiply(2 * factor);
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

	protected boolean isCompatible(PopulationTotalEstimate estimate) {
		if (observations.size() == estimate.observations.size()) {
			if (nRows == estimate.nRows) {
				if (nCols == estimate.nCols) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected List<PopulationUnitWithUnequalInclusionProbability> getObservations() {return observations;}
	
}
