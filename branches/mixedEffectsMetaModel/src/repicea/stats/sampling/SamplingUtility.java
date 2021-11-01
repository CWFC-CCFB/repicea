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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.stats.StatisticalUtility;

public class SamplingUtility {

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
		
		List<Integer> sampleIndex = getSampleIndex(sampleSize, population.size(), withReplacement);
		List sample = new ArrayList();
		for (Integer ind : sampleIndex) {
			sample.add(population.get(ind));
		}
		return sample;
	}
	
	/**
	 * This method returns the list of different observations and their frequency. 
	 * @param list a population or a sample
	 * @return a Map with observation as keys and frequencies as values
	 */
	@SuppressWarnings("rawtypes")
	public static Map<Object, Integer> getObservationFrequencies(Collection list) {
		Map<Object, Integer> frequencyMap = new HashMap<Object, Integer>();
		for (Object obj : list) {
			if (!frequencyMap.containsKey(obj)) {
				frequencyMap.put(obj, 0);
			}
			frequencyMap.put(obj, frequencyMap.get(obj) + 1);
		}
		return frequencyMap;
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
	

	private static List<Integer> getSampleIndex(int sampleSize, int populationSize, boolean withReplacement) {
		List<Integer> sampleIndex = new ArrayList<Integer>();
		int index;
		while (sampleIndex.size() < sampleSize) {
			index = (int) Math.floor(StatisticalUtility.getRandom().nextDouble() * populationSize);
			if (withReplacement || !sampleIndex.contains(index)) {
				sampleIndex.add(index);
			}
		}
		return sampleIndex;
	}

	
	/**
	 * This method returns a sample from a population drawn without replacement..
	 * @param population a Map instance with the population
	 * @param sampleSize an integer 
	 * @return a Map instance containing the sample
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getSample(Map population, int sampleSize) {
		if (sampleSize <= 0 || sampleSize > population.size()) {
			throw new InvalidParameterException("The sample size must be greater than 0 and smaller than or equal to the population size!");
		}
		List keys = new ArrayList();
		for (Object key : population.keySet()) {
			keys.add(key);
		}
		List<Integer> sampleIndex = getSampleIndex(sampleSize, population.size(), false);
		Map sample = new HashMap();
		for (Integer ind : sampleIndex) {
			Object key = keys.get(ind);
			Object value = population.get(key);
			sample.put(key, value);
		}
		return sample;
	}

}
