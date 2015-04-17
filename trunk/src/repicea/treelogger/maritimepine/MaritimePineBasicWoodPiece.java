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

import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.MessageID;

/**
 * The MaritimePineBasicWoodPiece is a simple class that represents 
 * the wood pieces produced by the MaritimePineBasicTreeLogger.
 *  
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicWoodPiece extends WoodPiece {

	protected MaritimePineBasicWoodPiece(TreeLogCategory logCategory, MaritimePineBasicTree tree) {
		super(logCategory, tree);
		double eligibleVolumeM3;
		if (logCategory.getName().equals(MessageID.Stump.toString())) {
			eligibleVolumeM3 = tree.getStumpVolumeM3();
		} else if (logCategory.getName().equals(MessageID.FWD.toString())) {
			eligibleVolumeM3 = tree.getStumpVolumeM3();
		} else {
			eligibleVolumeM3 = tree.getCommercialVolumeM3();
		}
		double volumeM3 = eligibleVolumeM3 * ((MaritimePineBasicTreeLogCategory) logCategory).getVolumeProportion();
		setVolumeM3(volumeM3);
	}

}
