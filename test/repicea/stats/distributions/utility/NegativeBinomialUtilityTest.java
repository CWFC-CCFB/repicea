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


public class NegativeBinomialUtilityTest {

	@Test
	public void simpleValueTest() {
		double expected = NegativeBinomialUtility.getMassProbabilityOLD(2, 1, .5);
		double observed = NegativeBinomialUtility.getMassProbability(2, 1, .5);
		Assert.assertEquals("Testing the two methods", expected, observed, 1E-8);
	}

	@Test
	public void performanceTest() {
		for (int i = 0; i < 100000; i++) 
			NegativeBinomialUtility.getMassProbabilityOLD(2, 1, .5);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) 
			NegativeBinomialUtility.getMassProbabilityOLD(2, 1, .5);
		long elapsedTimeReference = System.currentTimeMillis() - startTime;
		
		startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) 
			NegativeBinomialUtility.getMassProbability(2, 1, .5);
		long elapsedTimeNewMethod = System.currentTimeMillis() - startTime;
		System.out.println("Performance: Reference time = " + elapsedTimeReference + " - New method = " + elapsedTimeNewMethod);
		Assert.assertTrue("Testing that the new method is faster", elapsedTimeNewMethod < elapsedTimeReference);
	}
	
	
	@Test
	public void simpleValueTest2() {
		double observed = NegativeBinomialUtility.getMassProbability(22, 1, 1);
		double observed2 = NegativeBinomialUtility.getMassProbabilityOLD(22, 1, 1);
		Assert.assertEquals("Testing the two methods", observed, observed2, 1E-8);
	}

	
	

}
