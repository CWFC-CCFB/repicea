/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.ABCDQualityProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface ensures the tree object is compatible with the tree logger WBirchProdVol.
 * @author Mathieu Fortin - September 2013
 */
interface WBirchProdVolTree extends DbhCmProvider, ABCDQualityProvider, MonteCarloSimulationCompliantObject {
	
	public static enum WBirchProdVolTreeSpecies implements TextableEnum {
		WhiteBirch("White birch", "Bouleau \u00E0 papier");

		WBirchProdVolTreeSpecies(String englishText, String frenchText) {
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
	 * This method ensures the tree instance knows its species. It should return null if the tree is not eligible.
	 * @return a WBirchProdVolTreeSpecies enum 
	 */
	public WBirchProdVolTreeSpecies getWBirchProdVolTreeSpecies();
	
	
	
}
