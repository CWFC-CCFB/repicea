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

import repicea.simulation.treelogger.WoodPiece;

/**
 * The MaritimePineBasicWoodPiece is a simple class that represents 
 * the wood pieces produced by the MaritimePineBasicTreeLogger.
 *  
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicWoodPiece extends WoodPiece {

	
	/**
	 * Constructor.
	 * @param logCategory a MaritimePineBasicTreeLogCategory instance
	 * @param tree a MaritimePineBasicTree instance
	 * @param volumeM3 the volume without any expansion factor
	 */
	protected MaritimePineBasicWoodPiece(MaritimePineBasicTreeLogCategory logCategory, MaritimePineBasicTree tree, double volumeM3) {
		super(logCategory, tree);
		setVolumeM3(volumeM3);
	}

	
		
}
