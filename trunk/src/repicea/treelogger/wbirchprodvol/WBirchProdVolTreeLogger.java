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

import java.io.IOException;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;

public class WBirchProdVolTreeLogger extends TreeLogger<WBirchProdVolTreeLoggerParameters, WBirchProdVolLoggableTree> {

	private final static double VERY_SMALL = 1E-6;
	
	private WBirchProdVolPredictor wbp;

	public WBirchProdVolTreeLogger() {
		this(false, false);
	}
	
	/**
	 * Constructor. 
	 */
	public WBirchProdVolTreeLogger(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super();
		wbp = new WBirchProdVolPredictor(isParameterVariabilityEnabled, isResidualVariabilityEnabled);
	}
	

	@Override
	protected void logThisTree(WBirchProdVolLoggableTree tree) {
		WBirchProdVolStand stand = tree.getStand();
		Matrix predictedVolumes = wbp.getLogGradeVolumePredictions(stand, tree);
		List<WBirchProdVolTreeLogCategory> logCategory = getTreeLoggerParameters().getLogCategoryList();
		for (ProductID productID : ProductID.values()) {
			addThisWoodPiece(logCategory.get(productID.ordinal()), predictedVolumes.m_afData[productID.getIndex()][0], tree);
		}
	}
	
	private void addThisWoodPiece(WBirchProdVolTreeLogCategory logCategory, double volumeDm3, WBirchProdVolLoggableTree tree) {
		if (volumeDm3 > VERY_SMALL) {
			addWoodPiece(tree, new WBirchProdVolWoodPiece(logCategory, volumeDm3 * .001, tree));
		} 
	}
	
	@Override
	public void setTreeLoggerParameters() {
		WBirchProdVolTreeLoggerParameters stlp = createDefaultTreeLoggerParameters();				
		stlp.showUI(null);
		setTreeLoggerParameters(stlp);
	}

	@Override
	public WBirchProdVolTreeLoggerParameters createDefaultTreeLoggerParameters() {
		WBirchProdVolTreeLoggerParameters stlp = new WBirchProdVolTreeLoggerParameters();
		stlp.initializeDefaultLogCategories();
		return stlp;
	}


	@Override
	public WBirchProdVolLoggableTree getEligible(LoggableTree t) {
		if (t instanceof WBirchProdVolLoggableTree) {
			WBirchProdVolLoggableTree tree = (WBirchProdVolLoggableTree) t;
			if (tree.getWBirchProdVolTreeSpecies() != null && tree.getDbhCm() >= 18) {	// trees below 18 cm in dbh are not eligible
				return tree;
			}
		}
		return null;
	}

	/*
	 * For test purposes
	 * @param args
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException {
		WBirchProdVolTreeLogger log = new WBirchProdVolTreeLogger(false, false);
		log.setTreeLoggerParameters();
	}


	@Override
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof WBirchProdVolLoggableTree;
	}



}
