/*
 * This file is part of the repicea-util library.
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
package repicea.util;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Index class contains two synchronized map that makes it possible to get the index of a particular value. The put method ensures there is
 * no value repeated twice.
 * @author Mathieu Fortin - June 2014
 */
@SuppressWarnings("serial")
public class Index<K, V> extends HashMap<K, V>{
	
	Map<V, K> valueMap;
	
	public Index() {
		super();
		valueMap = new HashMap<V, K>();
	}

	@Override
	public V put(K key, V value) {
		if (containsKey(key)) {
			valueMap.remove(get(key));
		}
		
		if (valueMap.containsKey(value)) {
			throw new InvalidParameterException("This value has already been linked with a key!");
		} 
		
		valueMap.put(value, key);
		return super.put(key, value);
	}

	/**
	 * This method returns the key associated with this value.
	 * @param value a value
	 * @return the key or null if this value is not in the map
	 */
	public K getKeyForThisValue(String value) {
		return valueMap.get(value);
	}
	
	@Override
	public void clear() {
		valueMap.clear();
		super.clear();
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		if (value != null) {
			valueMap.remove(value);
		}
		return value;
	}
	
}
