/*
 * This file is part of the repicea library.
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
package repicea.util;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A class to manager logger and to avoid keeping them as weak references.
 * @author Mathieu Fortin - November 2021
 */
public class REpiceaLogManager {
	
	private static final Map<String, Logger> LoggerMap = new HashMap<String, Logger>();
	
	/**
	 * Create a logger or return an existing logger. <br>
	 * <br>
	 * The logger is saved in a Map so that is not just a weak reference as in the LogManager class.
	 * @param loggerName the name of the logger.
	 * @return a Logger instance
	 */
	public static Logger getLogger(String loggerName) {
		if (loggerName == null || loggerName.isEmpty()) 
			throw new InvalidParameterException("Argument loggerName cannot be null");
		if (!LoggerMap.containsKey(loggerName)) {
			LoggerMap.put(loggerName, Logger.getLogger(loggerName));	// ensure the logger is not just a weak reference in the LogManager singleton
		}
		return LoggerMap.get(loggerName);
	}
	
	/**
	 * Log a message in a particular logger.
	 * @param loggerName the name of the logger
	 * @param level the level (see Level enum)
	 * @param prefix an eventual prefix (can be null)
	 * @param obj the object to be logged
	 */
	public static void logMessage(String loggerName, Level level, String prefix, Object obj) {
		if (level == null)
			throw new InvalidParameterException("Argument level cannot be null");
		String message = "";
		if (prefix != null) {
			message += prefix + ": ";
		}
		message += obj.toString();
		getLogger(loggerName).log(level, message);
	}

}
