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

import repicea.math.Matrix;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.sampling.PopulationUnit;

@SuppressWarnings("serial")
public abstract class PointEstimate<O extends PopulationUnit> extends Estimate<GaussianDistribution> {

	
	protected PointEstimate() {
		super(new GaussianDistribution(new Matrix(new double[]{0}), new Matrix(new double[]{1})));
	}

	/**
	 * This method adds an observation to the sample.
	 * @param obs a PopulationUnitObservation instance
	 */
	public abstract void addObservation(O obs);
	
}
