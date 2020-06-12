/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2020 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.covariateproviders.standlevel;

/**
 * This interface ensures that the stand object can provide its
 * drainage category: xeric, mesic, subhydric or hydric.
 * @author Mathieu Fortin - November 2012 (refactored June 2020)
 * 		
 */
public interface DrainageCategoryProvider {

	public static enum DrainageCategory {
		Xeric,
		Mesic,
		Subhydric,
		Hydric;
	}
	
	
	/**
	 * This method returns the drainage category: xeric, mesic, subhydric or hydric.
	 * @return a DrainageCategoryProvider enum 
	 */
	public DrainageCategoryProvider getDrainageClass();

}
