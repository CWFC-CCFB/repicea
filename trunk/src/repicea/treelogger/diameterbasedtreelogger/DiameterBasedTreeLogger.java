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
package repicea.treelogger.diameterbasedtreelogger;

import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

public abstract class DiameterBasedTreeLogger extends TreeLogger<DiameterBasedTreeLoggerParameters, DiameterBasedTree> {


	@Override
	protected void logThisTree(DiameterBasedTree tree) {
		List<DiameterBasedTreeLogCategory> logCategories = params.getSpeciesLogCategories(getTreeLoggerParameters().getSpeciesName());
		DiameterBasedWoodPiece piece;
		for (DiameterBasedTreeLogCategory logCategory : logCategories) {
			piece = producePiece(tree, logCategory);
			if (piece != null) {
				addWoodPiece(tree, piece);	
			} 
		}
	}

	@Override
	public void setTreeLoggerParameters() {}

	@Override
	public abstract DiameterBasedTreeLoggerParameters createDefaultTreeLoggerParameters();
	
	@Override
	public abstract DiameterBasedTree getEligible(LoggableTree t);

	
	protected abstract DiameterBasedWoodPiece producePiece(DiameterBasedTree tree, DiameterBasedTreeLogCategory logCategory);
}

