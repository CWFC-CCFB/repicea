/*
 * This file is part of the repicea library.
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

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;

/**
 * The NonparametricDistribution is useful for Monte Carlo simulations. Its first two central moments
 * are derived from an array of matrices that represents the observations.
 * @author Mathieu Fortin - August 2012
 */
@Deprecated
class NonparametricDistribution extends EmpiricalDistribution {

	private static final long serialVersionUID = 20120826L;

	/**
	 * Constructor.
	 */
	private NonparametricDistribution() {
		super();
	}
	

	@Override
	public Matrix getRandomRealization() {
		int observationIndex = (int) (StatisticalUtility.getRandom().nextDouble() * getNumberOfRealizations());
		return getRealizations().get(observationIndex);
	}
}
