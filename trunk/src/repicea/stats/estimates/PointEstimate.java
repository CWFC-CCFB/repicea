/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge-Epicea
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
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.sampling.PopulationUnit;

@SuppressWarnings("serial")
public abstract class PointEstimate<O extends PopulationUnit> extends Estimate<GaussianDistribution> {

	private final List<O> observations;
	protected int nRows;
	protected int nCols;
	private final double populationSize;

	
	/**
	 * Basic constructor without population size.
	 */
	protected PointEstimate() {
		super(new GaussianDistribution(new Matrix(new double[]{0}), new Matrix(new double[]{1})));
		this.observations = new CopyOnWriteArrayList<O>();
		populationSize = -1d;
		estimatorType = EstimatorType.LeastSquares;
	}

	/**
	 * Constructor with population size.
	 * @param populationSize the number of units in the population.
	 */
	protected PointEstimate(double populationSize) {
		super(new GaussianDistribution(new Matrix(new double[]{0}), new Matrix(new double[]{1})));
		if (populationSize <= 0) {
			throw new InvalidParameterException("The population size must be greater than 0!");
		}
		this.observations = new CopyOnWriteArrayList<O>();
		this.populationSize = populationSize;
		estimatorType = EstimatorType.LeastSquares;
	}
	

	/**
	 * This method adds an observation to the sample.
	 * @param obs a PopulationUnitObservation instance
	 */
	public void addObservation(O obs) {
		if (obs != null) {
			if (nCols == 0) {
				nCols = obs.getData().m_iCols;
			}
			if (nRows == 0) {
				nRows = obs.getData().m_iRows;
			}
			if (obs.getData().m_iCols != nCols || obs.getData().m_iRows != nRows) {
				throw new InvalidParameterException("The observation is incompatible with what was already observed!");
			} else {
				observations.add(obs);
			}
		}
	}

	@Override
	protected boolean isMergeableEstimate(Estimate<?> estimate) {
		if (estimate.getClass().equals(getClass())) {
			PointEstimate pe = (PointEstimate) estimate;
			if (observations.size() == pe.observations.size()) {
				if (nRows == pe.nRows) {
					if (nCols == pe.nCols) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected List<O> getObservations() {return observations;}

	public boolean isPopulationSizeKnown() {return populationSize != -1;}
	
	public double getPopulationSize() {return populationSize;}
	
	protected Matrix getQuantileForProbability(double probability) {
		Matrix stdDev = getVariance().diagonalVector().elementWisePower(.5); 
		double quantile = GaussianUtility.getQuantile(probability);
		return getMean().add(stdDev.scalarMultiply(quantile));
	}
	
	@Override
	public ConfidenceInterval getConfidenceIntervalBounds(double oneMinusAlpha) {
		Matrix lowerBoundValue = getQuantileForProbability(.5 * (1d - oneMinusAlpha));
		Matrix upperBoundValue = getQuantileForProbability(1d - .5 * (1d - oneMinusAlpha));
		return new ConfidenceInterval(lowerBoundValue, upperBoundValue, oneMinusAlpha);
	}

	
	protected abstract PointEstimate<?> add(PointEstimate<?> pointEstimate);

	protected abstract PointEstimate<?> subtract(PointEstimate<?> pointEstimate);

	protected abstract PointEstimate<?> multiply(double scalar);


}