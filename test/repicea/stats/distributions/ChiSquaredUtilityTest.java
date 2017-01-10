/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;

public class ChiSquaredUtilityTest {

	
	@Test
	public void randomGenerationTest() {
		int df = 100;
		double expectedMean = 5d;
		ChiSquaredDistribution dist = new ChiSquaredDistribution(df, expectedMean);
		NonparametricDistribution receiver = new NonparametricDistribution();
		for (int i = 0; i < 1000000; i++) {
			receiver.addRealization(dist.getRandomRealization());
		}
		
		Matrix mean = receiver.getMean();
		Assert.assertEquals("Testing the mean", dist.getMean().m_afData[0][0], mean.m_afData[0][0], 1E-2);
		Matrix variance = receiver.getVariance();
		Assert.assertEquals("Testing the variance", dist.getVariance().m_afData[0][0], variance.m_afData[0][0], 1E-2);
	}
	
	
	
}
