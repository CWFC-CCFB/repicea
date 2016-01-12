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

import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.NonparametricDistribution;

/**
 * The LawOfTotalVarianceMonteCarloEstimate is a Monte Carlo estimate for random variable.
 * The variance is provided by the variance of the mean estimate plus the expectation of their
 * variances.
 * @author Mathieu Fortin - January 2016
 */
@SuppressWarnings("serial")
public class LawOfTotalVarianceMonteCarloEstimate extends Estimate<NonparametricDistribution> {

	private final NonparametricDistribution varianceDistribution;
		
	protected LawOfTotalVarianceMonteCarloEstimate() {
		super(new NonparametricDistribution());
		varianceDistribution = new NonparametricDistribution();
		estimatorType = EstimatorType.MonteCarlo;
	}

	public int getNumberOfRealizations() {return getDistribution().getNumberOfRealizations();}
	
	public void addRealization(Matrix value, Matrix variance) {
		getDistribution().addRealization(value);
		varianceDistribution.addRealization(variance);
	}
	
	public List<Matrix> getRealizationsOfTheMean() {return getDistribution().getRealizations();}
	
	public List<Matrix> getRealizationsOfTheVariance() {return varianceDistribution.getRealizations();}

	@Override
	public Matrix getVariance() {
		return super.getVariance().add(varianceDistribution.getMean());
	}
	
}
