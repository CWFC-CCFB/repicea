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
package repicea.stats.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;

/**
 * The NonparametricDistribution is useful for Monte Carlo simulations. Its first two central moments
 * are derived from an array of matrices that represents the observations.
 * @author Mathieu Fortin - August 2012
 */
public class NonparametricDistribution<N extends Number> implements Distribution<N>, Serializable {

	private static final long serialVersionUID = 20120826L;
	
	private final List<N> observations;

	/**
	 * Constructor.
	 */
	public NonparametricDistribution() {
		observations = new ArrayList<N>();
	}
	
	/**
	 * This method returns the number of observation in this nonparametric distribution.
	 * @return an integer
	 */
	public int getNumberOfRealizations() {return observations.size();}
	
	/**
	 * This method sets a given observation of the nonparametric distribution.
	 * @param value the value of the observation
	 */
	public void addRealization(N value) {observations.add(value);}
	
	/**
	 * This method returns the array that contains all the observations of this distribution.
	 * @return an array of Matrix instances
	 */
	public List<N> getRealizations() {return observations;}
	
	@SuppressWarnings("unchecked")
	@Override
	public N getMean() {
		if (observations == null || observations.isEmpty()) {
			return null;
		} else {
			if (observations.get(0) instanceof Matrix) {
				Matrix sum = null;
				for (N mat : observations) {
					if (sum == null) {
						sum = ((Matrix) mat).getDeepClone();
					} else {
						sum = sum.add((Matrix) mat);
					}
				}
				return (N) sum.scalarMultiply(1d / observations.size());
			} else {
				double sum = 0;
				for (N mat : observations) {
					sum += mat.doubleValue();
				}
				return (N) ((Double) (sum / observations.size()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public N getVariance() {
		if (getMean() instanceof Matrix) {
			Matrix mean = (Matrix) getMean();
			Matrix sse = null;
			Matrix error;
			for (N mat : observations) {
				error = ((Matrix) mat).subtract(mean);
				if (sse == null) {
					sse = error.multiply(error.transpose());
				} else {
					sse = sse.add(error.multiply(error.transpose()));
				}
			}
			return (N) sse.scalarMultiply(1d / (observations.size()-1));
		} else {
			double mean = (Double) getMean();
			double sse = 0;
			double error;
			for (N mat : observations) {
				error = ((Double) mat) - mean;
				sse += error * error;
			}
			return (N) ((Double) (sse / (observations.size()-1)));
		}
	}

	@Override
	public boolean isParametric() {
		return false;
	}

	@Override
	public boolean isMultivariate() {
		if (observations != null && observations.size() > 0) {
			return observations.get(0) instanceof Matrix && ((Matrix) observations.get(0)).m_iRows > 1;
		} else {
			return false;
		}
	}

	@Override
	public Type getType() {
		return Type.NONPARAMETRIC;
	}

	@Override
	public N getRandomRealization() {
		int observationIndex = (int) (StatisticalUtility.getRandom().nextDouble() * getNumberOfRealizations());
		return getRealizations().get(observationIndex);
	}

//	@Override
//	public double getQuantile(double... values) {
//		if (observationsgetM)
//		// TODO to be implemented
//		return -1;
//	}

}
