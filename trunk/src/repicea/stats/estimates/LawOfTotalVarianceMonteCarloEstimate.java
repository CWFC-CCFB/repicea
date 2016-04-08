/*
 * This file is part of the repicea library.
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

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;

/**
 * The LawOfTotalVarianceMonteCarloEstimate is a Monte Carlo estimate for random variable.
 * The variance is provided by the variance of the mean estimate plus the expectation of their
 * variances.
 * @author Mathieu Fortin - January 2016
 */
@SuppressWarnings("serial")
public class LawOfTotalVarianceMonteCarloEstimate extends MonteCarloEstimate {

	
	private final List<SampleMeanEstimate> realizations;
	
	
	/**
	 * Constructor.	
	 */
	public LawOfTotalVarianceMonteCarloEstimate() {
		super();
		realizations = new ArrayList<SampleMeanEstimate>();
	}

	/**
	 * This method is a surrogate for addRealization(Matrix) method.
	 * @param estimate a SampleEstimate instance
	 */
	public void addRealization(SampleMeanEstimate estimate) {
		realizations.add(estimate);
		getDistribution().getRealizations().add(estimate.getMean());
	}
	
	@Override
	public void addRealization(Matrix mat) {
		addRealization(unformatObservation(mat));
	}
	
	
	@Override
	public Matrix getVariance() {
		MonteCarloEstimate meanOfVariances = new MonteCarloEstimate();
		MonteCarloEstimate varianceOfMeans = new MonteCarloEstimate();
		for (SampleMeanEstimate realization : realizations) {
			meanOfVariances.addRealization(realization.getVariance());
			varianceOfMeans.addRealization(realization.getMean());
		}
			
		return meanOfVariances.getMean().add(varianceOfMeans.getVariance());
	}

	private SampleMeanEstimate unformatObservation(Matrix formattedObservation) {
		SampleMeanEstimate estimate = new SampleMeanEstimate();
		for (int j = 0; j < formattedObservation.m_iCols; j++) {
			estimate.addObservation(formattedObservation.getSubMatrix(0, formattedObservation.m_iRows - 1, j, j));
		}
		return estimate;
	}

	@Override
	public Matrix getMean() {
		Matrix mean = null;
		for (SampleMeanEstimate estimate : realizations) {
			if (mean == null) {
				mean = estimate.getMean();
			} else {
				mean = mean.add(estimate.getMean());
			}
		}
		return mean.scalarMultiply(1d / getNumberOfRealizations());
	}

}
