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
package repicea.treelogger.diameterbasedtreelogger;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class DiameterBasedTreeLoggerParameters extends TreeLoggerParameters<DiameterBasedTreeLogCategory> {

//	public static enum MessageID implements TextableEnum {
//		WoodProducts("Wood products", "Produits bois"),
//		ProductType("Product type", "Type de produit"),
//		;
//
//		MessageID(String englishText, String frenchText) {
//			setText(englishText, frenchText);
//		}
//
//		@Override
//		public void setText(String englishText, String frenchText) {
//			REpiceaTranslator.setString(this, englishText, frenchText);
//		}
//		
//		@Override
//		public String toString() {
//			return REpiceaTranslator.getString(this);
//		}
//	}
	
	public static enum Grade implements TextableEnum {
		EnergyWood("Industry and energy wood", "Bois d'industrie et bois \u00E9nergie (BIBE)"),
		SmallLumberWood("Small lumber wood", "Petit bois d'oeuvre (BO)"),
		LargeLumberWood("Lumber wood", "Bois d'oeuvre (BO)"),
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

	private static DiameterBasedTreeLogCategory LargeLumber;
	
	private transient DiameterBasedTreeLoggerParametersDialog guiInterface;

	protected DiameterBasedTreeLoggerParameters(Class<? extends TreeLogger<?,?>> clazz) {
		super(clazz);
		initializeDefaultLogCategories();
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		String species = getSpeciesName();
		getLogCategories().clear();
		getLogCategories().put(species, categories);
		categories.add(new DiameterBasedTreeLogCategory(Grade.LargeLumberWood, species, 37.5, false));	// not small end but dbh in this case
		categories.add(new DiameterBasedTreeLogCategory(Grade.SmallLumberWood, species, 27.5, false));
		categories.add(new DiameterBasedTreeLogCategory(Grade.EnergyWood, species, 7, false));
	}

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public DiameterBasedTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new DiameterBasedTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	protected DiameterBasedTreeLogCategory getLargeLumberWoodLogCategory() {
		if (LargeLumber == null) {
			for (DiameterBasedTreeLogCategory lc : this.getLogCategories().get(TreeLoggerParameters.ANY_SPECIES)) {
				if (lc.getGrade() == Grade.LargeLumberWood) {
					LargeLumber = lc;
				}
			}
		}
		return LargeLumber;
	}
	
	public static void main(String[] args) {
		DiameterBasedTreeLoggerParameters params = new DiameterBasedTreeLoggerParameters(DiameterBasedTreeLogger.class);
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
	}

	protected String getSpeciesName() {
		return TreeLoggerParameters.ANY_SPECIES;
	}

}
