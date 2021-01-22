/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge-Epicea
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

import org.junit.Assert;
import org.junit.Test;

public class LogNormalEstimateTest {

	@Test
	public void testWithMeanAndVarianceOnLogScale() {
		LogNormalEstimate est = new LogNormalEstimate(2,0.5,true);
		double mean = est.getMean().m_afData[0][0];
		double expectedMean = Math.exp(2 + 0.5 * 0.5);
		Assert.assertEquals("Testing the mean", expectedMean, mean, 1E-8);
		
		double variance = est.getVariance().m_afData[0][0];
		double expectedVariance = (Math.exp(0.5) - 1) * Math.exp(2 * 2 + 0.5);
		Assert.assertEquals("Testing the variance", expectedVariance, variance, 1E-8);
	}
	
	
	@Test
	public void testWithMeanAndVarianceOnOriginalScale() {
		LogNormalEstimate est = new LogNormalEstimate(9.487735836358526, 58.39602780205479, false);
		double mean = est.getDistribution().getMean().m_afData[0][0];
		Assert.assertEquals("Testing the mean", 2, mean, 1E-8);
		
		double variance = est.getDistribution().getVariance().m_afData[0][0];
		Assert.assertEquals("Testing the variance", 0.5, variance, 1E-8);
	}
	
	
	// TODO implement random realization generation

	
	
}
