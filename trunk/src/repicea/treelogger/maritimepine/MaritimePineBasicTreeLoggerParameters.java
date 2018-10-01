/*
 * This file is part of the repicea-foresttools library.
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
package repicea.treelogger.maritimepine;

import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.species.REpiceaSpecies;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The MaritimePineBasicTreeLoggerParameters class defines the parameters
 * of the MaritimePineBasicTreeLogger.
 * 
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicTreeLoggerParameters extends DiameterBasedTreeLoggerParameters {

	
	public static enum Grade implements TextableEnum {
		IndustryWood("Particle", "Bois industrie"),
		SawlogLowQuality("Sawlog low quality", "Sciage basse qualit\u00E9"),
		SawlogHighQuality("Sawlog high quality", "Sciage haute qualit\u00E9"),
		Stump("Stump", "Souche"),
		Crown("Crown", "Houppier")
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


	protected MaritimePineBasicTreeLoggerParameters() {
		super(MaritimePineBasicTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		String species = getSpeciesName();
		getLogCategories().clear();
		getLogCategories().put(species, categories);
		categories.add(new MaritimePineBasicTreeLogCategory(Grade.Stump, species, -1, true));
		categories.add(new MaritimePineBasicTreeLogCategory(Grade.SawlogHighQuality, species, 25, false));
		categories.add(new MaritimePineBasicTreeLogCategory(Grade.SawlogLowQuality, species, 16, false));
		categories.add(new MaritimePineBasicTreeLogCategory(Grade.IndustryWood, species, 10, false));
		categories.add(new MaritimePineBasicTreeLogCategory(Grade.Crown, species, -1, false));
	}

	protected String getSpeciesName() {return REpiceaSpecies.Species.Pinus_pinaster.toString();}
	
	public static void main(String[] args) {
		MaritimePineBasicTreeLoggerParameters params = new MaritimePineBasicTreeLoggerParameters();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
	}

}
