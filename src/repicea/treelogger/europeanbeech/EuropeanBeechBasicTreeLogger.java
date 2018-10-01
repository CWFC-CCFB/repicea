/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.treelogger.europeanbeech;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;

public class EuropeanBeechBasicTreeLogger extends DiameterBasedTreeLogger {

	
	@Override
	public EuropeanBeechBasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new EuropeanBeechBasicTreeLoggerParameters();
	}

	@Override
	public EuropeanBeechBasicTree getEligible(LoggableTree t) {
		if (t instanceof EuropeanBeechBasicTree) {
			return (EuropeanBeechBasicTree) t;
		} else {
			return null;
		}
	}
	

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof EuropeanBeechBasicTree; 
	}
	
	
}

