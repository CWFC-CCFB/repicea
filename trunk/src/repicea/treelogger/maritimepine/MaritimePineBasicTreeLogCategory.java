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
import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.Grade;

/**
 * The MaritimePineBasicTreeLogCategory class is the log category for 
 * the MaritimePineBasicTreeLogger.
 * 
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategory extends TreeLogCategory {

	protected final Double smallEndDiameter;
	
	private transient MaritimePineBasicTreeLogCategoryPanel guiInterface;
	protected final Grade logGrade;
	
	/**
	 * Constructor.
	 * @param str the name of the category
	 * @param species the species name
	 * @param merchantableVolumeProportion the proportion of the merchantable volume that falls into this category
	 */
	protected MaritimePineBasicTreeLogCategory(Grade logGrade, String species, double smallEndDiameter) {
		super(logGrade.toString());
		setSpecies(species);
		this.logGrade = logGrade;
		if (smallEndDiameter == -1) {
			this.smallEndDiameter = Double.NaN;
		} else {
			this.smallEndDiameter = smallEndDiameter;
		}
	}

		
	/*
	 * Useless for this class (non-Javadoc)
	 * @see capsis.extension.treelogger.TreeLogCategory#getTreeLogCategoryPanel()
	 */
	@Override
	public MaritimePineBasicTreeLogCategoryPanel getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new MaritimePineBasicTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}


}
