/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service, Canadian Wood Fibre Centre
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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerTest {

	public static void main(String[] args) {
		
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %5$s%6$s%n");
		Logger subLogger = REpiceaLogManager.getLogger("SubLogger");
		Object o = subLogger.getHandlers();
		Logger mainLogger = REpiceaLogManager.getLogger("MainLogger");
		REpiceaLogManager.setMainLogger(mainLogger);
		mainLogger.setLevel(Level.INFO);
		ConsoleHandler sh = new ConsoleHandler();
		sh.setLevel(Level.FINEST);
		sh.setFormatter(new SimpleFormatter());
		REpiceaLogManager.getLogger("MainLogger").addHandler(sh);
//		REpiceaLogManager.logMessage("SubLogger", Level.FINE, "From mainLogger:", "This is a warning");
		
		REpiceaLogManager.logMessage("SubLogger", Level.FINE, "From sublogger:", "This is a warning");
		int u = 0;

	}
	
	
	
}
