/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge-Epicea
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

import java.util.List;

/**
 * This interface ensures that the plot can return a list of all species.
 * @author Mathieu Fortin - September 2018
 */
public interface SpeciesListProvider {
	

	/**
	 * This method returns a list of species name that are found in either this plot or all other plots.
	 * @return a List of String instances
	 */
	public List<String> getExtensiveSpeciesList();
	
}
