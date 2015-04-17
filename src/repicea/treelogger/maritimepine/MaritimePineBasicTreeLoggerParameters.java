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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The MaritimePineBasicTreeLoggerParameters class defines the parameters
 * of the MaritimePineBasicTreeLogger.
 * 
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicTreeLoggerParameters extends TreeLoggerParameters<MaritimePineBasicTreeLogCategory> {

	public static enum MessageID implements TextableEnum {
		WoodProducts("Wood products", "Produits bois"),
		ProductType("Product type", "Type de produit"),
		ShortLived("Particle", "Bois d'industrie"),
		LongLived("Sawing", "Bois d'oeuvre"),
		Stump("Stump", "Souche"),
		FWD("Fine wood debris", "Branches fines"),
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

	
	private transient MaritimePineBasicTreeLoggerParametersDialog guiInterface;

	protected MaritimePineBasicTreeLoggerParameters() {
		super(MaritimePineBasicTreeLogger.class);
		initializeDefaultLogCategories();
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<MaritimePineBasicTreeLogCategory> categories = new ArrayList<MaritimePineBasicTreeLogCategory>();
		getLogCategories().clear();
		getLogCategories().put(MaritimePineBasicTree.PINE, categories);
		categories.add(new MaritimePineBasicTreeLogCategory(MessageID.ShortLived.toString(), MaritimePineBasicTree.PINE, .3));
		categories.add(new MaritimePineBasicTreeLogCategory(MessageID.LongLived.toString(), MaritimePineBasicTree.PINE, .7));
		categories.add(new MaritimePineBasicTreeLogCategory(MessageID.Stump.toString(), MaritimePineBasicTree.PINE, 1d));
		categories.add(new MaritimePineBasicTreeLogCategory(MessageID.FWD.toString(), MaritimePineBasicTree.PINE, 1d));
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public MaritimePineBasicTreeLoggerParametersDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new MaritimePineBasicTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}
	
	public static void main(String[] args) {
		MaritimePineBasicTreeLoggerParameters params = new MaritimePineBasicTreeLoggerParameters();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(false));
		params.showInterface(null);
		params.showInterface(null);
	}

}
