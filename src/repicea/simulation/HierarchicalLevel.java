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
package repicea.simulation;

/**
 * The HierarchicalLevel class is used in the ModelBasedSimulator class. It defines the
 * different levels of GaussianEstimate instance.
 * @author Mathieu Fortin - December 2015
 */
public class HierarchicalLevel {

	public static final HierarchicalLevel PLOT = new HierarchicalLevel("plot");
	public static final HierarchicalLevel TREE = new HierarchicalLevel("tree");
	public static final HierarchicalLevel INTERVAL_NESTED_IN_PLOT = new HierarchicalLevel("interval_nested_in_plot");
	
	private final String levelName;
	
	protected HierarchicalLevel(String levelName) {
		this.levelName = levelName;
	}
	
	@Override
	public String toString() {return levelName;}
	
}
