/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class MonteCarloSettings {
	
	public static enum MonteCarloVarSource implements TextableEnum {
		Parameters("Module parameters", "Param\u00E9trage des modules"),
		Mortality("Mortality module", "Module de mortalit\u00E9"),
		DiameterGrowth("Diameter increment module", "Module de croissance diam\u00E9trale"),
		Recruitment("Recruitment module", "Module de recrutement"),
		RecruitsDBH("Recruit DBH module", "Module de diam\u00E8tre des recrues"),
		HDRelationship("Height module", "Module de hauteur"),
		Volume("Volume module", "Module de volume"),
		Harvesting("Harvesting module", "Module de r\u00E9colte");
		
		MonteCarloVarSource(String englishString, String frenchString) {
			setText(englishString, frenchString);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

}
