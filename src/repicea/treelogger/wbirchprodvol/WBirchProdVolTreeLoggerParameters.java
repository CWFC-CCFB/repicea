/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.treelogger.wbirchprodvol.WBirchProdVolTree.WBirchProdVolTreeSpecies;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class WBirchProdVolTreeLoggerParameters extends TreeLoggerParameters<WBirchProdVolTreeLogCategory> {

	protected static enum ProductID implements TextableEnum {
		Veneer("veneer", "d\u00E9roulage", 5),
		LowGradeVeneer("low grade veneer", "d\u00E9roulable", 4),
		Sawlog("sawlog", "sciage", 3),
		LowGradeSawlog("low grade sawlog", "sciable", 6),
		PulpAndPaper("pulp", "p\u00E2te", 2),
		;

		int index;
		
		ProductID(String englishText, String frenchText, int index) {
			setText(englishText, frenchText);
			this.index = index;
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		protected int getIndex() {return index;}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	private transient WBirchProdVolTreeLoggerParametersDialog guiInterface;

	protected WBirchProdVolTreeLoggerParameters() {
		super(WBirchProdVolTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		getLogCategories().clear();
		List<WBirchProdVolTreeLogCategory> logCategories = new ArrayList<WBirchProdVolTreeLogCategory>();
		String speciesName = WBirchProdVolTreeSpecies.WhiteBirch.toString();
		getLogCategories().put(speciesName, logCategories);
		logCategories.add(new WBirchProdVolTreeLogCategory(ProductID.Veneer.toString(), speciesName, "A", 2.5, null, null));
		logCategories.add(new WBirchProdVolTreeLogCategory(ProductID.LowGradeVeneer.toString(), speciesName, "B", 2.5, 27.1, 3.1));
		logCategories.add(new WBirchProdVolTreeLogCategory(ProductID.Sawlog.toString(), speciesName, "B,C", 2.5, null, null));
		logCategories.add(new WBirchProdVolTreeLogCategory(ProductID.LowGradeSawlog.toString(), speciesName, "E", 2.5, null, null));
		logCategories.add(new WBirchProdVolTreeLogCategory(ProductID.PulpAndPaper.toString(), speciesName, "D", 2.5, null, null));
		
		setFilename("");
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public TreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new WBirchProdVolTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	/*
	 * For test purposes
	 */
	public static void main(String[] args) {
		WBirchProdVolTreeLoggerParameters stlp = new WBirchProdVolTreeLoggerParameters();
		stlp.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(false));
		stlp.initializeDefaultLogCategories();
		stlp.showUI(null);
		stlp.showUI(null);
		System.exit(0);
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

}
