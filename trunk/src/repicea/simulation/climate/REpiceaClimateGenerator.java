/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.climate;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The REpiceaClimateGenerator interface ensures that the climate generator returns 
 * a REpiceaClimateVariableMap instance.
 * @author Mathieu Fortin - June 2019
 */
public interface REpiceaClimateGenerator<P extends GeographicalCoordinatesProvider> {

	public static enum RepresentativeConcentrationPathway implements TextableEnum {
		RCP2_6("RCP 2.6", "RCP 2.6"),
		RCP4_5("RCP 4.5", "RCP 4.5"),
		RCP6_0("RCP 6.0", "RCP 6.0"),
		RCP8_5("RCP 8.5", "RCP 8.5");

		RepresentativeConcentrationPathway(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	/**
	 * Returns a map of climate variables depending on the geographical coordinates of the plot.
	 * @param plot a GeographicalCoordinatesProvider instance
	 * @return a REpiceaClimateVariableMap-derived instance
	 */
	public REpiceaClimateVariableMap getClimateVariables(P plot);
}
