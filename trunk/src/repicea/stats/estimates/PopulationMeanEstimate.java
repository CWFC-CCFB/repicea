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

import repicea.math.Matrix;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;

/**
 * This class implements an estimator of the mean of the population, which is the 
 * sample mean and the sample variance corrected by n/(n-1) being the estimator of the
 * variance.
 * @author Mathieu Fortin - April 2016
 */
@SuppressWarnings("serial")
public class PopulationMeanEstimate extends PointEstimate<PopulationUnitWithEqualInclusionProbability> {
		
	private final double populationSize;
	private final EmpiricalDistribution sample;

	/**
	 * Basic constructor without population size.
	 */
	public PopulationMeanEstimate() {
		super();
		estimatorType = EstimatorType.LeastSquares;
		sample = new EmpiricalDistribution();
		populationSize = -1d;
	}

	/**
	 * Constructor with population size.
	 * @param populationSize the number of units in the population.
	 */
	public PopulationMeanEstimate(double populationSize) {
		super();
		estimatorType = EstimatorType.LeastSquares;
		sample = new EmpiricalDistribution();
		if (populationSize <= 0) {
			throw new InvalidParameterException("The population size must be greater than 0!");
		}
		this.populationSize = populationSize;
	}
	
	
	@Override
	public Matrix getMean() {
		return sample.getMean();
	}

	private int getSampleSize() {return sample.getNumberOfRealizations();}
	
	@Override
	public Matrix getVariance() {
		double smallAreaCorrectionFactor = 1d;
		if (populationSize != -1d) {
			smallAreaCorrectionFactor = 1d - getSampleSize()/populationSize;
		}
		return sample.getVariance().scalarMultiply(1d/getSampleSize() * smallAreaCorrectionFactor);
	}

	@Override
	public Matrix getRandomDeviate() {
		getDistribution().setMean(getMean());
		getDistribution().setMean(getVariance());
		return super.getRandomDeviate();
	}

	@Override
	public void addObservation(PopulationUnitWithEqualInclusionProbability obs) {
		if (obs != null) {
			sample.addRealization(obs.getData());
		}		
	}

	// TODO adapt other public methods
	
}
