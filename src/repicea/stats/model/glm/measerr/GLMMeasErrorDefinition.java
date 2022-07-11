/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.model.glm.measerr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GLMMeasErrorDefinition {

	protected final String effectWithMeasError;
	protected final double minimumValueForConsideringMeasurementError;
	protected final List<Double> xValuesForIntegration; 
	
	public GLMMeasErrorDefinition(String effectWithMeasError, 
			double minimumValueForConsideringMeasurementError,
			List<Double> xValuesForIntegration) {
		this.effectWithMeasError = effectWithMeasError;
		this.minimumValueForConsideringMeasurementError = minimumValueForConsideringMeasurementError;
		this.xValuesForIntegration = new ArrayList<Double>();
		for (Double d : xValuesForIntegration) {
			if (!this.xValuesForIntegration.contains(d)) {
				this.xValuesForIntegration.add(d);
			}
		}
		Collections.sort(this.xValuesForIntegration);
	}
	
}
