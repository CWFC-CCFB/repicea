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
import repicea.stats.sampling.PopulationUnit;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;

/**
 * This class implements an estimator of the mean of the population, which is the 
 * sample mean and the sample variance corrected by n/(n-1) being the estimator of the
 * variance.
 * @author Mathieu Fortin - April 2016
 */
@SuppressWarnings("serial")
public class PopulationMeanEstimate extends PointEstimate<PopulationUnitWithEqualInclusionProbability> {
		
	private final EmpiricalDistribution sample;

	/**
	 * Basic constructor without population size.
	 */
	public PopulationMeanEstimate() {
		super();
		sample = new EmpiricalDistribution();
	}

	/**
	 * Constructor with population size.
	 * @param populationSize the number of units in the population.
	 */
	public PopulationMeanEstimate(double populationSize) {
		super(populationSize);
		sample = new EmpiricalDistribution();
	}
	
	
	@Override
	public Matrix getMean() {
		return sample.getMean();
	}

	private int getSampleSize() {return sample.getNumberOfRealizations();}
	
	@Override
	public Matrix getVariance() {
		double smallAreaCorrectionFactor = 1d;
		if (isPopulationSizeKnown()) {
			smallAreaCorrectionFactor = 1d - getSampleSize()/getPopulationSize();
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
			super.addObservation(obs);
			sample.addRealization(obs.getData());
		}		
	}

	@Override
	protected PopulationMeanEstimate add(PointEstimate<?> pointEstimate) {
		if (isMergeableEstimate(pointEstimate)) {
			PopulationMeanEstimate newEstimate = new PopulationMeanEstimate();
			PopulationMeanEstimate meanEstimate = (PopulationMeanEstimate) pointEstimate;
			for (int i = 0; i < getObservations().size(); i++) {
				PopulationUnit thisUnit = getObservations().get(i);
				PopulationUnit thatUnit = meanEstimate.getObservations().get(i);
				newEstimate.addObservation(new PopulationUnitWithEqualInclusionProbability(thisUnit.getData().add(thatUnit.getData())));
			}
			return newEstimate;
		} else {
			throw new InvalidParameterException("Incompatible point estimates!");
		}
	}

	@Override
	protected PopulationMeanEstimate subtract(PointEstimate<?> pointEstimate) {
		if (isMergeableEstimate(pointEstimate)) {
			PopulationMeanEstimate newEstimate = new PopulationMeanEstimate();
			PopulationMeanEstimate meanEstimate = (PopulationMeanEstimate) pointEstimate;
			for (int i = 0; i < getObservations().size(); i++) {
				PopulationUnit thisUnit = getObservations().get(i);
				PopulationUnit thatUnit = meanEstimate.getObservations().get(i);
				newEstimate.addObservation(new PopulationUnitWithEqualInclusionProbability(thisUnit.getData().subtract(thatUnit.getData())));
			}
			return newEstimate;
		} else {
			throw new InvalidParameterException("Incompatible point estimates!");
		}
	}

	@Override
	protected PopulationMeanEstimate multiply(double scalar) {
		PopulationMeanEstimate newEstimate = new PopulationMeanEstimate();
		for (int i = 0; i < getObservations().size(); i++) {
			PopulationUnit thisUnit = getObservations().get(i);
			newEstimate.addObservation(new PopulationUnitWithEqualInclusionProbability(thisUnit.getData().scalarMultiply(scalar)));
		}
		return newEstimate;
	}

}
