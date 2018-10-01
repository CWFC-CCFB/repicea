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

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class BasicTreeLoggerWoodPiece extends WoodPiece {

	protected BasicTreeLoggerWoodPiece(LogCategory logCategory, LoggableTree tree, double volumeForThisPiece) {
		super(logCategory, tree);
//		double volumeM3 = tree.getCommercialVolumeM3() * ((BasicLogCategory) logCategory).getVolumeProportion();
		setVolumeM3(volumeForThisPiece);
	}

}
