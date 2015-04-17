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

/**
 * This interface ensures the tree can provide the basic features to be eligible for
 * the MaritimePineBasicTreeLogger.
 * @author Mathieu Fortin - November 2014
 */
public interface MaritimePineBasicTree extends LoggableTree {

	public static final String PINE = "pine";

	/**
	 * This method returns the stump volume (m3) of the tree.
	 * @return a double
	 */
	public double getStumpVolumeM3();

	/**
	 * This method returns the volume (m3) of fine woody debris.
	 * @return a double
	 */
	public double getFineWoodyDebrisVolumeM3();
}
