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

import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class WBirchProdVolTreeLogCategory extends TreeLogCategory {

	protected final double lengthM;
	protected final Double minimumSmallEndDiameterCm;
	protected final Double maximumDecayDiameterCm;
	protected final String eligibleLogGrade;
	
	private transient WBirchProdVolTreeLogCategoryPanel guiInterface;
	
	protected WBirchProdVolTreeLogCategory(String name,
			String speciesName, 
			String eligibleLogGrade,
			double lengthM, 
			Double minimumSmallEndDiameterCm, 
			Double maximumDecayDiameterCm) {
		super(name, false);
		this.setSpecies(speciesName);
		this.eligibleLogGrade = eligibleLogGrade;
		this.lengthM = lengthM;
		this.minimumSmallEndDiameterCm = minimumSmallEndDiameterCm;
		this.maximumDecayDiameterCm = maximumDecayDiameterCm;
	}

	@Override
	public WBirchProdVolTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new WBirchProdVolTreeLogCategoryPanel(this);
		} 
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
