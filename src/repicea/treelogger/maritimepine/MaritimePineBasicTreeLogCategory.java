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

import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.simulation.treelogger.WoodPiece;

/**
 * The MaritimePineBasicTreeLogCategory class is the log category for 
 * the MaritimePineBasicTreeLogger.
 * 
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategory extends TreeLogCategory {

	static class CarbonAccountingToolDefaultLogCategoryPanel extends TreeLogCategoryPanel<MaritimePineBasicTreeLogCategory> {
		private CarbonAccountingToolDefaultLogCategoryPanel(MaritimePineBasicTreeLogCategory logCategory) {
			super(logCategory);
		}
	}

	private double volumeProportionToBeProcessedInThisCategory;

	private transient CarbonAccountingToolDefaultLogCategoryPanel guiInterface;
	
	/**
	 * Constructor.
	 * @param str the name of the category
	 * @param species the species name
	 * @param merchantableVolumeProportion the proportion of the merchantable volume that falls into this category
	 */
	protected MaritimePineBasicTreeLogCategory(String str, String species, double volumeProportionToBeProcessedInThisCategory) {
		super(str);
		this.volumeProportionToBeProcessedInThisCategory = volumeProportionToBeProcessedInThisCategory;
		setSpecies(species);
	}

		
	/*
	 * Useless for this class (non-Javadoc)
	 * @see capsis.extension.treelogger.TreeLogCategory#getTreeLogCategoryPanel()
	 */
	@Override
	public CarbonAccountingToolDefaultLogCategoryPanel getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new CarbonAccountingToolDefaultLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}


	protected void setVolumeProportion(double d) {
		volumeProportionToBeProcessedInThisCategory = d;
	}


	protected double getVolumeProportion() {return volumeProportionToBeProcessedInThisCategory;}

}
