/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.treelogger.europeanbeech;

import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.species.REpiceaSpecies;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class EuropeanBeechBasicTreeLoggerParameters extends DiameterBasedTreeLoggerParameters {

	public static enum Grade implements TextableEnum {
		EnergyWood("Energy wood", "Bois \u00E9nergie"),
		IndustryWood("Particle", "Bois industrie"),
		SawlogLowQuality("Sawlog low quality", "Sciage basse qualit\u00E9"),
		SawlogRegularQuality("Sawlog regular quality", "Sciage moyenne qualit\u00E9"),
		VeneerQuality("Veneer quality", "Qualit\u00E9 placage"),
		;

		Grade(String englishText, String frenchText) {
			setText(englishText, frenchText);
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

	protected EuropeanBeechBasicTreeLoggerParameters() {
		super(EuropeanBeechBasicTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		String species = getSpeciesName();
		getLogCategories().clear();
		getLogCategories().put(species, categories);
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.VeneerQuality, species, 47.5));	// not small end but dbh in this case
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.SawlogRegularQuality, species, 37.5));
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.SawlogLowQuality, species, 27.5));
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.IndustryWood, species, 17.5));
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.EnergyWood, species, 5));
	}
	
	
	@Override
	protected String getSpeciesName() {
		return REpiceaSpecies.Species.Fagus_sylvatica.toString();
	}

	public static void main(String[] args) {
		EuropeanBeechBasicTreeLoggerParameters params = new EuropeanBeechBasicTreeLoggerParameters();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
	}

}
