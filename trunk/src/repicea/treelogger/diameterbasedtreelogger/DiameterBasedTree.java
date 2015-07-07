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

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.treelogger.LoggableTree;

public abstract interface DiameterBasedTree extends LoggableTree, DbhCmProvider {

	
	/**
	 * This method returns the standard deviation of the dbh.
	 * @return a double
	 */
	public double getDbhCmStandardDeviation();

}