/*
 * This file is part of the repicea-foresttools library.
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
package repicea.treelogger.basictreelogger;


import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

public class BasicTreeLogger extends TreeLogger<BasicTreeLoggerParameters, LoggableTree> {
	
	public BasicTreeLogger() {}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see capsis.extension.TreeLogger#setTreeLoggerParameters()
	 */
	@Override
	public void setTreeLoggerParameters() {}



	@Override
	protected void logThisTree (LoggableTree tree) {
		List<BasicLogCategory> logCategories = params.getSpeciesLogCategories(BasicTreeLoggerParameters.ANY_SPECIES);
		if (tree.getCommercialVolumeM3() > 0) {
			for (BasicLogCategory logCategory : logCategories) {
				addWoodPiece(tree, new BasicTreeLoggerWoodPiece(logCategory, tree));
			}
		}
	}

	@Override
	public BasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new BasicTreeLoggerParameters();
	}

	@Override
	public LoggableTree getEligible(LoggableTree t) {
		return t;
	}

	@Override
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof LoggableTree;
	}
	
}