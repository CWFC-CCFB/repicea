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
package repicea.treelogger.diameterbasedtreelogger;

import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class DiameterBasedTreeLogCategory extends TreeLogCategory {
	
	protected final Double smallEndDiameter;
	
	private transient DiameterBasedTreeLogCategoryPanel guiInterface;
	protected final Enum<?> logGrade;
	
	/**
	 * Constructor.
	 * @param str the name of the category
	 * @param species the species name
	 * @param merchantableVolumeProportion the proportion of the merchantable volume that falls into this category
	 */
	public DiameterBasedTreeLogCategory(Enum<?> logGrade, String species, double smallEndDiameter) {
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
	public DiameterBasedTreeLogCategoryPanel getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new DiameterBasedTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}

	public Enum<?> getGrade() {return logGrade;}

}
