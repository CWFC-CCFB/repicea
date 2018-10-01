/*
 * This file is part of the repicea-foresttools library.
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
package repicea.treelogger.maritimepine;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;

/**
 * The MaritimePineBasicTreeLogger class is a simple tree logger which considers
 * not only the commercial volume, but also the harvest of stumps and fine woody debris.
 *
 * @author Mathieu Fortin - November 2014
 */
public class MaritimePineBasicTreeLogger extends DiameterBasedTreeLogger {

	protected static double LowQualityPercentageWithinHighQualityGrade = 0.65;

	@Override
	public MaritimePineBasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new MaritimePineBasicTreeLoggerParameters();
	}

	@Override
	public MaritimePineBasicLoggableTree getEligible(LoggableTree t) {
		if (t instanceof MaritimePineBasicLoggableTree) {
			return (MaritimePineBasicLoggableTree) t;
		} else {
			return null;
		}
	}


	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof MaritimePineBasicLoggableTree;
	}
	
	
}
