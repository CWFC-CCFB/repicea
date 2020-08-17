/*
 * This file is part of the repicea library.
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
package repicea.simulation.covariateproviders.treelevel;

import java.security.InvalidParameterException;

public interface TotalVolumeM3Provider {

	public default double getTotalVolumeM3() {
		if (isTotalVolumeUnderbark()) {
			return ((TotalUnderbarkVolumeM3Provider) this).getTotalUnderbarkVolumeM3();
		} else {
			return ((TotalOverbarkVolumeM3Provider) this).getTotalOverbarkVolumeM3();
		}
	}
	
	public default boolean isTotalVolumeUnderbark() {
		if (this instanceof TotalUnderbarkVolumeM3Provider) {
			return true;
		} else if (this instanceof TotalOverbarkVolumeM3Provider) {
			return false;
		} else {
			throw new InvalidParameterException("The instance should implement either the TotalUnderbarkVolumeM3Provider or TotalOverbarkVolumeM3Provider interface!");
		}
	}
	
	public default boolean isTotalVolumeOverbark() {
		return !isTotalVolumeUnderbark();
	}

}
