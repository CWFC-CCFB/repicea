/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.matapedia;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The MatapediaTree interface ensures the compatibility of the tree object with 
 * the MatapediaDbhIncrementPredictor and the MatapediaMortalityPredictor classes.
 * @author Mathieu Fortin - November 2012
 */
public interface MatapediaTree extends MonteCarloSimulationCompliantObject,
										DbhCmProvider,
										SquaredDbhCmProvider,
										BasalAreaLargerThanSubjectM2Provider {

	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	/**
	 * An enum variable that defines the species.
	 * @author Mathieu Fortin - November 2012
	 */
	public static enum MatapediaTreeSpecies implements TextableEnum {
		/**
		 * Other species.
		 */
		AUT(false, "Other species", "Autres esp\u00E8ces"),
		/**
		 * Birch species.
		 */
		BOU(false, "Birches", "Bouleaux"),
		/**
		 * Spruce species.
		 */
		EP(true, "Spruces", "Epinettes"),
		/**
		 * Balsam fir.
		 */
		SAB(true, "Balsam fir", "Sapin baumier");
		
		Matrix dummy = new Matrix(1,4);
		boolean isSpruceOrFir;
		
		MatapediaTreeSpecies(boolean isSpruceOrFir, String englishText, String frenchText) {
			setText(englishText, frenchText);
			this.isSpruceOrFir = isSpruceOrFir;
			dummy.m_afData[0][ordinal()] = 1d; 
		}
		
		public Matrix getDummy() {
			return dummy;
		}
		
		public boolean isSpruceOrFir() {
			return isSpruceOrFir;
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

	/**
	 * This method ensures the species compatibility with both the dbh increment and mortality
	 * models. 
	 * @return an MatapediaTreeSpecies instance
	 */
	public MatapediaTreeSpecies getMatapediaTreeSpecies();
	
	
}
