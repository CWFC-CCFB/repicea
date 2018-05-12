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

import repicea.math.Matrix;

@SuppressWarnings("serial")
public class SampleMeanDistribution extends EmpiricalDistribution {

	public SampleMeanDistribution() {
		super();
	}
	
	@Override
	public Matrix getRandomRealization() {
		return null;
	}

	@Override
	public Matrix getVariance() {
		return super.getVariance().scalarMultiply(1d / getNumberOfRealizations());
	}
}
