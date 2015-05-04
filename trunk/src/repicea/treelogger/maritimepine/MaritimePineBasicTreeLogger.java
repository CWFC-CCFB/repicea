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

import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

/**
 * The MaritimePineBasicTreeLogger class is a simple tree logger which considers
 * not only the commercial volume, but also the harvest of stumps and fine woody debris.
 *
 * @author Mathieu Fortin - November 2014
 */
public class MaritimePineBasicTreeLogger extends TreeLogger<MaritimePineBasicTreeLoggerParameters, MaritimePineBasicTree> {

	@Override
	protected void logThisTree(MaritimePineBasicTree tree) {
		List<MaritimePineBasicTreeLogCategory> logCategories = params.getSpeciesLogCategories(MaritimePineBasicTree.Species.MaritimePine.toString());
		for (MaritimePineBasicTreeLogCategory logCategory : logCategories) {
			addWoodPiece(tree, new MaritimePineBasicWoodPiece(logCategory, tree));	
		}

		
	}

	@Override
	public void setTreeLoggerParameters() {}

	@Override
	public MaritimePineBasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new MaritimePineBasicTreeLoggerParameters();
	}

	@Override
	public MaritimePineBasicTree getEligible(LoggableTree t) {
		if (t instanceof MaritimePineBasicTree) {
			return (MaritimePineBasicTree) t;
		} else {
			return null;
		}
	}

}
