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
package repicea.stats.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;

/**
 * The EmpiricalDistribution class handles empirical distribution from observations
 * or realizations.
 * @author Mathieu Fortin - May 2018
 *
 */
public class EmpiricalDistribution implements Distribution, Serializable {

	private static final long serialVersionUID = 20120826L;
	
	private final List<Matrix> realizations;

	/**
	 * Constructor.
	 */
	public EmpiricalDistribution() {
		realizations = new ArrayList<Matrix>();
	}
	
	/**
	 * This method returns the number of observations in this nonparametric distribution.
	 * @return an integer
	 */
	public int getNumberOfRealizations() {return realizations.size();}
	
	/**
	 * This method sets a given observation of the nonparametric distribution.
	 * @param value the value of the observation
	 */
	public void addRealization(Matrix value) {realizations.add(value);}
	
	/**
	 * This method returns the array that contains all the observations of this distribution.
	 * @return an array of Matrix instances
	 */
	public List<Matrix> getRealizations() {return realizations;}
	
	@Override
	public Matrix getMean() {
		if (realizations == null || realizations.isEmpty()) {
			return null;
		} else {
			Matrix sum = null;
			for (Matrix mat : realizations) {
				if (sum == null) {
					sum = ((Matrix) mat).getDeepClone();
				} else {
					sum = sum.add((Matrix) mat);
				}
			}
			return (Matrix) sum.scalarMultiply(1d / realizations.size());
		}
	}

	@Override
	public Matrix getVariance() {
		Matrix mean = (Matrix) getMean();
		Matrix sse = null;
		Matrix error;
		for (Matrix mat : realizations) {
			error = ((Matrix) mat).subtract(mean);
			if (sse == null) {
				sse = error.multiply(error.transpose());
			} else {
				sse = sse.add(error.multiply(error.transpose()));
			}
		}
		return sse.scalarMultiply(1d / (realizations.size()-1));
	}

	@Override
	public boolean isParametric() {
		return false;
	}

	@Override
	public boolean isMultivariate() {
		if (realizations != null && realizations.size() > 0) {
			return realizations.get(0) instanceof Matrix && ((Matrix) realizations.get(0)).m_iRows > 1;
		} else {
			return false;
		}
	}

	@Override
	public Type getType() {
		return Type.NONPARAMETRIC;
	}


//	@Override
//	public double getQuantile(double... values) {
//		if (observationsgetM)
//		// TODO to be implemented
//		return -1;
//	}

	@Override
	public Matrix getRandomRealization() {
		int observationIndex = (int) (StatisticalUtility.getRandom().nextDouble() * getNumberOfRealizations());
		return getRealizations().get(observationIndex);
	}

}
