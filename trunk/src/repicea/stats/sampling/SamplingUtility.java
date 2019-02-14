/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.sampling;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SamplingUtility {

	private static Random random = new Random();
	
	/**
	 * This method returns a sample from a population.
	 * @param population a List instance with the population
	 * @param sampleSize an integer 
	 * @param withReplacement true if sampling with replacement or false otherwise
	 * @return a List instance containing the sample
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<?> getSample(List<?> population, int sampleSize, boolean withReplacement) {
		if (sampleSize <= 0 || sampleSize > population.size()) {
			throw new InvalidParameterException("The sample size must be greater than 0 and smaller than or equal to the population size!");
		}
		
		List<Integer> sampleIndex = new ArrayList<Integer>();
		int index;
		while (sampleIndex.size() < sampleSize) {
			index = (int) Math.floor(random.nextDouble() * population.size());
			if (withReplacement || !sampleIndex.contains(index)) {
				sampleIndex.add(index);
			}
		}
		List sample = new ArrayList();
		for (Integer ind : sampleIndex) {
			sample.add(population.get(ind));
		}
		return sample;
	}
	
	
	/**
	 * This method returns a sample from a population drawn according to a random sample without replacement.
	 * design.
	 * @param population a List instance with the population
	 * @param sampleSize an integer 
	 * @return a List instance containing the sample
	 */
	public static List<?> getSample(List<?> population, int sampleSize) {
		return getSample(population, sampleSize, false);
	}
	
}
