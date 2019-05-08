/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.processsystem;

import java.util.HashMap;
import java.util.Map;

/**
 * The AmountMap class contains all the quantities that can be contained in a ProcessUnit instance.
 * @author Mathieu Fortin - January 2014
 *
 * @param <E> an Enum class
 */
@SuppressWarnings("serial")
public class AmountMap<E extends Enum<?>> extends HashMap<E, Double> implements Cloneable {

	@Override
	public void putAll(Map<? extends E, ? extends Double> map) {
		for (E key : map.keySet()) {
			add(key, map.get(key));
		}
	}

	/**
	 * This method add a particular amount in a key.
	 * @param key
	 * @param amount
	 */
	public void add(E key, double amount) {
		if (containsKey(key)) {
			put(key, get(key) + amount);
		} else {
			put(key, amount);
		}
	}
	
	/**
	 * This method multiplies all the values in the current instance by a scalar.
	 * @param scalar the multiplicative factor
	 * @return a new AmountMap instance with updated values
	 */
	public AmountMap<E> multiplyByAScalar(double scalar) {
		AmountMap<E> outputMap = new AmountMap<E>();
		for (E key : keySet()) {
			outputMap.put(key, get(key) * scalar);
		}
		return outputMap;
	}

	@Override
	public AmountMap<E> clone() {
		AmountMap<E> outputMap = new AmountMap<E>();
		for (E key : keySet()) {
			outputMap.put(key, get(key));
		}
		return outputMap;
	}
}
