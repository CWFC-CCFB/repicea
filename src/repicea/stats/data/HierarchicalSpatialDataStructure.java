/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.data;

import java.util.List;
import java.util.Map;

import repicea.math.Matrix;


public interface HierarchicalSpatialDataStructure extends HierarchicalStatisticalDataStructure {
	
	/**
	 * This method returns the distance map between the observations. The keys represent the observation index, whereas the double 
	 * is the distance.
	 * @return a Map instance
	 */
	public Map<Integer, Map<Integer, Double>> getDistancesBetweenObservations();
	
	/**
	 * This method returns the angle map between the observations. The keys represent the observation index, whereas the double 
	 * is the angle in radians. 
	 * @return a Map instance
	 */
	public Map<Integer, Map<Integer, Matrix>> getAngleBetweenObservations();
	
	/**
	 * This method sets the distance field names.
	 * @param fields a List that contains the names of the fields that serve to compute the Euclidian distance
	 */
	public void setDistanceFields(List<String> fields);
	
}
