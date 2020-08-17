/*
 * This file is part of the repicea library.
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
package repicea.simulation.covariateproviders.treelevel;

/**
 * This interface ensures the tree instance can provide its own commercial volume.
 * @author Mathieu Fortin - November 2012
 */
public interface CommercialOverbarkVolumeM3Provider {

	/**
	 * This method returns the overbark commercial volume, i.e. volume up to a particular 
	 * small-end diameter, for the CalculableTree instance. The volume is returned WITHOUT 
	 * any expansion factor.
	 * @return the merchantable volume in m3 (double)
	 */
	public double getCommercialOverbarkVolumeM3();

}
