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

public interface CommercialVolumeM3Provider {

	
	public default double getCommercialVolumeM3() {
		if (isCommercialVolumeUnderbark()) {
			return ((CommercialUnderbarkVolumeM3Provider) this).getCommercialUnderbarkVolumeM3();
		} else {
			return ((CommercialOverbarkVolumeM3Provider) this).getCommercialOverbarkVolumeM3();
		}
	}
	
	public default boolean isCommercialVolumeUnderbark() {
		if (this instanceof CommercialUnderbarkVolumeM3Provider) {
			return true;
		} else if (this instanceof CommercialOverbarkVolumeM3Provider) {
			return false;
		} else {
			throw new InvalidParameterException("The instance should implement either the CommercialUnderbarkVolumeM3Provider or CommercialOverbarkVolumeM3Provider interface!");
		}

	}
}
