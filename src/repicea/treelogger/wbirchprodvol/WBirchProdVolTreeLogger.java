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
import repicea.treelogger.wbirchprodvol.WBirchProdVolPredictor.Version;
import repicea.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;

public class WBirchProdVolTreeLogger extends TreeLogger<WBirchProdVolTreeLoggerParameters, WBirchProdVolLoggableTree> {

	private final static double VERY_SMALL = 1E-6;
	
	private WBirchProdVolPredictor wbp;

	
	/**
	 * Constructor. 
	 */
	public WBirchProdVolTreeLogger(boolean isParameterVariabilityEnabled) {
		super();
		wbp = new WBirchProdVolPredictor(isParameterVariabilityEnabled);
	}
	

	@Override
	protected void logThisTree(WBirchProdVolLoggableTree tree) {
		WBirchProdVolStand stand = tree.getStand();

		WBirchProdVolEstimate estimate  = wbp.getLogGradeVolumePredictions(stand, tree);
		Matrix predictedVolumes = estimate.getMean();
		Version version = estimate.getVersion();
		
		List<WBirchProdVolTreeLogCategory> logCategory = getTreeLoggerParameters().getLogCategoryList();
		
		addThisWoodPiece(logCategory.get(ProductID.PulpAndPaper.ordinal()), predictedVolumes.m_afData[2][0], tree);
		if (version == Version.Full) {
			addThisWoodPiece(logCategory.get(ProductID.Sawlog.ordinal()), predictedVolumes.m_afData[3][0], tree);
			addThisWoodPiece(logCategory.get(ProductID.LowGradeVeneer.ordinal()), predictedVolumes.m_afData[4][0], tree);
			addThisWoodPiece(logCategory.get(ProductID.Veneer.ordinal()), predictedVolumes.m_afData[5][0], tree);
		} else if (version == Version.FullTruncated || version == Version.DClass) {
			addThisWoodPiece(logCategory.get(ProductID.Sawlog.ordinal()), predictedVolumes.m_afData[3][0], tree);
		} else {
			addThisWoodPiece(logCategory.get(ProductID.LowGradeSawlog.ordinal()), predictedVolumes.m_afData[3][0], tree);
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
		stlp.showInterface(null);
		setTreeLoggerParameters(stlp);
	}

	@Override
	public WBirchProdVolTreeLoggerParameters createDefaultTreeLoggerParameters() {
		WBirchProdVolTreeLoggerParameters stlp = new WBirchProdVolTreeLoggerParameters();
		stlp.initializeDefaultLogCategories();
		return stlp;
	}

//	@Override
//	public void init(Collection<WBirchProdVolLoggableTree> loggableTrees) {
//		Collection<WBirchProdVolLoggableTree> validLoggableTrees = getValidLoggableTreesFromACollection(loggableTrees);
//		super.init(validLoggableTrees);
//	}

//	/**
//	 * This method extracts a collection of TreePetroLoggable objects from a collection of LoggableTree instances.
//	 * @param trees a Collection of LoggableTree-derived instances
//	 * @return a Collection of PetroLoggableTree instances
//	 */
//	private Collection<WBirchProdVolLoggableTree> getValidLoggableTreesFromACollection(Collection<? extends LoggableTree> loggableTrees) {
//		Collection<WBirchProdVolLoggableTree> validLoggableTrees = new ArrayList<WBirchProdVolLoggableTree>();
//		for (LoggableTree t : loggableTrees) {
//			if (t instanceof WBirchProdVolLoggableTree) {
//				WBirchProdVolLoggableTree tree = (WBirchProdVolLoggableTree) t;
//				if (isEligible(tree)) {	
//					validLoggableTrees.add(tree);
//				}
//			}
//		}
//		return validLoggableTrees;
//	}


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
		WBirchProdVolTreeLogger log = new WBirchProdVolTreeLogger(false);
		log.setTreeLoggerParameters();
	}



}