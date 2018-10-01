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
package repicea.treelogger.europeanbeech;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters.Grade;

public class EuropeanBeechBasicTreeLoggerTests {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void TestWithSimpleTreeWithStandardDeviation() {
		EuropeanBeechBasicTreeLogger treeLogger = new EuropeanBeechBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<EuropeanBeechTree>();
		EuropeanBeechTree tree = new EuropeanBeechTree(30,10);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		double sum = 0;
		for (WoodPiece piece : treeLogger.getWoodPieces().get(tree)) {
			double volumeM3 = piece.getVolumeM3();
			sum += volumeM3;
		}
		Assert.assertEquals("Comparing bole volume", 1d, sum, 1E-8); 
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void TestWithSimpleTreeWithNoStandardDeviation() {
		EuropeanBeechBasicTreeLogger treeLogger = new EuropeanBeechBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<EuropeanBeechTree>();
		EuropeanBeechTree tree = new EuropeanBeechTree(29,0);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
		Assert.assertTrue(woodPieces.size() == 1);
		WoodPiece woodPiece = woodPieces.iterator().next();
		EuropeanBeechBasicTreeLogCategory logCategory = (EuropeanBeechBasicTreeLogCategory) woodPiece.getLogCategory();
		Assert.assertTrue(logCategory.getGrade() == Grade.SawlogLowQuality);
	}


}
