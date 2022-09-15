/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.model.glm.GLModelTest;
import repicea.util.ObjectUtility;

public class DataStructureTest {

	private static DataSet DATASET;
	
	@BeforeClass
	public static void initialization() throws Exception {
		String filename = ObjectUtility.getPackagePath(GLModelTest.class) + "exampleDistanceSample.csv";
		DATASET = new DataSet(filename, true);
	}
	
	@Test
	public void simpleDataStructureTest() throws StatisticalDataException {
		GenericStatisticalDataStructure struct = new GenericStatisticalDataStructure(DATASET);
		struct.setModelDefinition("isConspecificIn ~ distance");
		Matrix matrixX = struct.constructMatrixX();
		double intercept = matrixX.getValueAt(0, 0);
		Assert.assertEquals("Comparing intercept", 1d, intercept, 1E-8);
		double observedDistance = matrixX.getValueAt(0, 1);
		Assert.assertEquals("Comparing distances", 4.123105625617661, observedDistance, 1E-8);
	}

	@Test
	public void simpleDataStructureWithFormulaTest() throws StatisticalDataException {
		GenericStatisticalDataStructure struct = new GenericStatisticalDataStructure(DATASET);
		struct.setModelDefinition("isConspecificIn ~ exp(distance)");
		Matrix matrixX = struct.constructMatrixX();
		double intercept = matrixX.getValueAt(0, 0);
		Assert.assertEquals("Comparing intercept", 1d, intercept, 1E-8);
		double observedDistance = matrixX.getValueAt(0, 1);
		Assert.assertEquals("Comparing distances", Math.exp(4.123105625617661), observedDistance, 1E-8);
	}

	@Test
	public void simpleDataStructureWithFormulaTest2() throws StatisticalDataException {
		GenericStatisticalDataStructure struct = new GenericStatisticalDataStructure(DATASET);
		struct.setModelDefinition("isConspecificIn ~ exp(1 + distance) + exp(distance)");
		Matrix matrixX = struct.constructMatrixX();
		double intercept = matrixX.getValueAt(0, 0);
		Assert.assertEquals("Comparing intercept", 1d, intercept, 1E-8);
		double observedDistance = matrixX.getValueAt(0, 1);
		Assert.assertEquals("Comparing distances", Math.exp(1 + 4.123105625617661), observedDistance, 1E-8);
		double observedDistance2 = matrixX.getValueAt(0, 2);
		Assert.assertEquals("Comparing distances", Math.exp(4.123105625617661), observedDistance2, 1E-8);
	}

	@Test
	public void hierarchicalDataStructureWithFormulaTest1() throws StatisticalDataException {
		HierarchicalStatisticalDataStructure struct = new GenericHierarchicalStatisticalDataStructure(DATASET);
		struct.setModelDefinition("isConspecificIn ~ exp(1 + distance) + exp(distance) + (distance | id)");
		Matrix matrixX = struct.constructMatrixX();
		double intercept = matrixX.getValueAt(0, 0);
		Assert.assertEquals("Comparing intercept", 1d, intercept, 1E-8);
		double observedDistance = matrixX.getValueAt(0, 1);
		Assert.assertEquals("Comparing distances", Math.exp(1 + 209.69024774652732), observedDistance, 1E-8);
		double observedDistance2 = matrixX.getValueAt(0, 2);
		Assert.assertEquals("Comparing distances", Math.exp(209.69024774652732), observedDistance2, 1E-8);
	}

}
