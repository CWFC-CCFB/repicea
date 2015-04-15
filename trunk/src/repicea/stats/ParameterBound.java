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
package repicea.stats;

import java.io.Serializable;

/**
 * This class defines the bound for a particular parameter.
 * @author Mathieu Fortin - October 2011
 */
@SuppressWarnings("serial")
public class ParameterBound implements Serializable {
	
	private Double lowerBound;
	private Double upperBound;
	
	public ParameterBound(Double lowerBound, Double upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	protected double validateParameter(double parameter) {
		if (lowerBound != null && parameter < lowerBound) {
			return lowerBound;
		}
		if (upperBound != null && parameter > upperBound) {
			return upperBound;
		}
		return parameter;
	}

}
