/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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

package repicea.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

import repicea.util.ObjectUtility;

/**
 * The JSONConfiguration class can be used to load json files and access their data in map form.  
 * This is particularly useful for configuration files.  It can be used with JSONConfigurationGlobal
 * to store a particular configuration into a global singleton acessible anywhere in the application.
 * 
 * @author Jean-François Lavoie - September 2021
 * @see JSONConfigurationGlobal
 */
public class JSONConfiguration {
				
	private LinkedHashMap<String, Object> data;
	static private final String Separator = "/";
	
	/**
	 * This constructor creates an empty data map only
	 */
	public JSONConfiguration() {
		data = new LinkedHashMap<String, Object>();
	}
	
	/**
	 * This constructor creates a deep copy of the given object.
	 * @param object the JSONConfiguration instance to be cloned.
	 */
	public JSONConfiguration(JSONConfiguration object) {
		data = (LinkedHashMap<String, Object>) ObjectUtility.copyMap(object.data);		
	}
	
	/**
	 * This constructor loads the given filename into data.
	 * @param filename the path to the file that contains the configuration.
	 * @throws FileNotFoundException if the file cannot be found.
	 */
	public JSONConfiguration(String filename) throws FileNotFoundException {
		this();
		load(filename);
	}
	
	/**
	 * This method loads the given filename into data.
	 * @param filename the path to the file that contains the configuration.
	 * @throws FileNotFoundException if the file cannot be found.
	 */
	public void load(String filename) throws FileNotFoundException {
		FileInputStream stream = new FileInputStream(filename);
		data = new LinkedHashMap<String, Object>(JsonReader.jsonToMaps(stream, null));
	}
	
	/**
	 * This method retrieves the required key from the data map and returns its corresponding value, or defaultValue if not found.
	 * 
	 * @param key The complete key including its path, i.e. "level1/level2/thisparam" where thisparam is the key located in a section 
	 * called level2 located in a section called level1 located at the root of the JSON file
	 * @param defaultValue The default value to be returned if the key is not found.
	 * @return the value located at the location pointed by key, or defaultValue if this location is empty  
	 */
	public Object get(String key, Object defaultValue) {								
		return get(key, defaultValue, data);
	}
	
	/**
	 * This method is the recursive internal implementation of get(). It looks for the "/" separator into the key, 
	 * and if it finds one, it calls get() again with a new key without the prefix and with a new submap without the first level.
	 * If no separator is found in the key, it returns the value stored in the key at the actual level or the default value if not found.
	 *  
	 * @param key The key including its path for the current hierarchical level, i.e. "level2/thisparam" if level1 was already processed 
	 * @param defaultValue The default value to be returned if the key is not found.
	 * @param submap The map corresponding to the hierachical level into which to perform searching.
	 * @return the value located at the location pointed by key, or defaultValue if this location is empty
	 * @see get(String key, Object defaultValue)
	 */
	private Object get(String key, Object defaultValue, LinkedHashMap<String, Object> submap) {
		int index = key.indexOf(Separator);
		if (index == -1) {				
			Object value = submap.get(key);
			if (value == null) 
				return defaultValue;
			
			return value;
		}
		else {			
			LinkedHashMap<String, Object> nextSubmap = (LinkedHashMap<String, Object>)submap.get(key.substring(0, index));
			
			return nextSubmap == null ? defaultValue : get(key.substring(index + 1), defaultValue, nextSubmap);
		}
	}
	
	/**
	 * This method stores the specified value at the location pointed by key into the data map.  
	 * Note : If the key points to an empty location, it will be created.  
	 * 
	 * @param key The complete key including its path, i.e. "level1/level2/thisparam" where thisparam is the key located in a section 
	 * called level2 located in a section called level1 located at the root of the JSON file
	 * @param value The value to be set in the map.  
	 */
	public void put(String key, Object value) {		
		put(key, value, data);
	}
	
	/**
	 * This method is the recursive internal implementation of put(). It looks for the "/" separator into the key, 
	 * and if it finds one, it calls put() again with a new key without the prefix and creates a new submap stored in the current level.
	 * If no separator is found in the key, it puts the value in the current level.
	 *  
	 * @param key The key including its path for the current hierarchical level, i.e. "level2/thisparam" if level1 was already processed  
	 * @param value The value to be set in the map.  
	 * @param submap The map corresponding to the hierarchical level into which to store the value.
	 * @see put(String key, Object value)
	 */
	private void put(String key, Object value, LinkedHashMap<String, Object> submap) {
		int index = key.indexOf(Separator);
		if (index == -1) {	
			submap.put(key, value);
		}
		else {
			String subkey = key.substring(index + 1);
			LinkedHashMap<String, Object> nextSubmap = (LinkedHashMap<String, Object>)submap.get(key.substring(0, index));
			if (nextSubmap == null) {
				nextSubmap = new LinkedHashMap<String, Object>();
				submap.put(key.substring(0, index), nextSubmap);
			}				
			
			put(subkey, value, nextSubmap);
		}
	}
}
