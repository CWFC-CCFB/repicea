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

/**
 * The JSONConfigurationGlobal class stores a singleton object that can be used to get and set
 * configuration parameters anywhere in the application.  The key and their paths should not be hardcoded but defined
 * in a configuration specific class instead (i.e. REpiceaJSONConfiguration) as static strings.  
 * This allows the file format to change over time without having to modify all the accesses to the JSONConfiguration
 * object throughout the application.  
 * 
 * @author Jean-François Lavoie - September 2021
 * @see JSONConfiguration
 * @see REpiceaJSONConfiguration
 */
public class JSONConfigurationGlobal {
	private static JSONConfiguration globalConfig;
	
	/**
	 * This method stores the given configuration into the singleton using a deep copy in JSONConfiguration ctor.
	 * This allows setting a local configuration that has been loaded from a file to the singleton so that 
	 * access to the singleton params is allowed no matter the lifecycle of the initial local instance.
	 * 
	 * @param config The configuration object to keep a copy of into the singleton instance 
	 */
	public static void setInstance(JSONConfiguration config) {	
		globalConfig = new JSONConfiguration(config);		
	}
	
	/**
	 * This method returns the singleton object reference allowing access to its data.  
	 * If the instance is null, it creates a new empty one.  
	 * 
	 * @return A reference to the configuration object stored into the singleton object  
	 */
	public static JSONConfiguration getInstance() {
		if (globalConfig == null) {
			globalConfig = new JSONConfiguration();
		}
		
		return globalConfig;
	}
}
