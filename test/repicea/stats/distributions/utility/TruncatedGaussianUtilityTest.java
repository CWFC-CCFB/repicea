/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.distributions.utility;

import org.junit.Assert;
import org.junit.Test;


public class TruncatedGaussianUtilityTest {

	@Test
	public void getSimpleProbabilityDensity() {
		double value = TruncatedGaussianUtility.getProbabilityDensity(0, 0, 1, Double.NEGATIVE_INFINITY, 0);
		Assert.assertEquals("Testing probability density", 0.7978845608028654, value, 1E-8);
	}
	
	@Test
	public void getSimpleCumulativeProbability() {
		double value = TruncatedGaussianUtility.getCumulativeProbability(0, 0, 1, Double.NEGATIVE_INFINITY, 0);
		Assert.assertEquals("Testing cumulative probability", 1d, value, 1E-8);
	}

	@Test
	public void getSimpleQuantile() {
		double value = TruncatedGaussianUtility.getQuantile(0.99, 0, 1, Double.NEGATIVE_INFINITY, 0);
		Assert.assertEquals("Testing cumulative probability", -0.012533469522069105, value, 1E-8);
	}

	@Test
	public void getSimpleQuantile2() {
		double value = TruncatedGaussianUtility.getQuantile(0.01, 0, 1, Double.NEGATIVE_INFINITY, 0);
		Assert.assertEquals("Testing cumulative probability", -2.5758293064439264, value, 1E-8);
	}


	@Test
	public void getSimpleQuantile3() {
		double value = TruncatedGaussianUtility.getQuantile(0.005, 0, 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		Assert.assertEquals("Testing cumulative probability", -2.5758293064439264, value, 1E-8);
	}

	
}
