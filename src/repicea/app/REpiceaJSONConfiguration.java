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
 * The REpiceaJSONConfiguration class defines a specific JSON configuration file by defining 
 * its key locations into static constant Strings.  The application should use those strings instead
 * of hardcoding a path
 * 
 * @author Jean-François Lavoie - September 2021
 * @see JSONConfigurationGlobal
 */
public class REpiceaJSONConfiguration {
	public static final String processingMaxThreads = "processing/maxThreads";
	public static final String processingMaxMemoryLimitMB = "processing/maxMemoryLimitMB";
}
