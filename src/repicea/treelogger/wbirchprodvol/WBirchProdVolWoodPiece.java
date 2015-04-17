/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class WBirchProdVolWoodPiece extends WoodPiece {

	protected WBirchProdVolWoodPiece(WBirchProdVolTreeLogCategory logCategory, double volumeM3,  WBirchProdVolLoggableTree tree) {
		super(logCategory, tree);
		setVolumeM3(volumeM3);
	}

}
