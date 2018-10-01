/*
 * This file is part of the repicea-foresttools library.
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
package repicea.treelogger.basictreelogger;


import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


@SuppressWarnings("serial")
public class BasicTreeLoggerParameters extends TreeLoggerParameters<BasicLogCategory> {
	
	public static enum MessageID implements TextableEnum {
		WoodProducts("Wood products", "Produits bois"),
		ProductType("Product type", "Type de produit"),
		ShortLived("Particle", "Bois d'industrie"),
		LongLived("Sawing", "Bois d'oeuvre"),
		Proportion("Splitting", "R\u00E9partition");

		MessageID(String englishText, String frenchText) {
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
	

	private transient BasicTreeLoggerParametersDialog guiInterface;
	
	/**
	 * Default constructor.
	 * @param defaultSettings a DefaultSettings instance
	 */
	public BasicTreeLoggerParameters() {
		super(BasicTreeLogger.class);
		initializeDefaultLogCategories();
	}
	
	/*
	 * Useless (non-Javadoc)
	 * @see capsis.extension.treelogger.TreeLoggerParameters#initializeDefaultLogCategories()
	 */
	@Override
	protected void initializeDefaultLogCategories() {
		List<BasicLogCategory> categories = new ArrayList<BasicLogCategory>();
		getLogCategories().clear();
		getLogCategories().put(ANY_SPECIES, categories);
		categories.add(new BasicLogCategory(MessageID.ShortLived.toString(), ANY_SPECIES, .5));
		categories.add(new BasicLogCategory(MessageID.LongLived.toString(), ANY_SPECIES, .5));
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public BasicTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new BasicTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	
	public static void main(String[] args) {
		BasicTreeLoggerParameters params = new BasicTreeLoggerParameters();
		params.showUI(null);
	}

	
}
