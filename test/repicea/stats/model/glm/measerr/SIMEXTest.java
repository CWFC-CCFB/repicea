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
package repicea.stats.model.glm.measerr;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.data.DataSet;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

public class SIMEXTest {

	
	@BeforeClass
	public static void doThis() {
		Level l = Level.OFF;
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);		
		SIMEXModel.OverrideVarianceForTest = true;
	}

	@Test
	public void simpleTest() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("sample0.csv");
		DataSet dataSet = new DataSet(filename, true);
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "y ~ distanceToConspecific");
		glm.doEstimation();
		SIMEXModel s = new SIMEXModel(glm, "distanceToConspecific", "variance");
		s.setNumberOfBootstrapRealizations(200);
		s.doEstimation();
		Assert.assertTrue("Checking if successfully extrapolated", s.getEstimator().isConvergenceAchieved());
		Estimate<?> estimate = s.getEstimator().getParameterEstimates();
		Assert.assertEquals("Checking parm estimate 1", -0.1687, estimate.getMean().getValueAt(0, 0), 4E-3);
		Assert.assertEquals("Checking standard error parm estimate 1", 0.067178, Math.sqrt(estimate.getVariance().getValueAt(0, 0)), 6E-4);
		Assert.assertEquals("Checking parm 2", -0.05505, estimate.getMean().getValueAt(1, 0), 4E-4);
		Assert.assertEquals("Checking standard error parm estimate 2", 0.004364, Math.sqrt(estimate.getVariance().getValueAt(1, 1)), 6E-5);
	}
	
	@Test
	public void multipleOccurrenceOfMeasurementErrorTest() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("sample0.csv");
		DataSet dataSet = new DataSet(filename, true);
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "y ~ distanceToConspecific + log(10 + distanceToConspecific)");
		glm.doEstimation();
		SIMEXModel s = new SIMEXModel(glm, "distanceToConspecific", "variance");
		s.setNumberOfBootstrapRealizations(200);
		s.doEstimation();
		Assert.assertTrue("Checking if successfully extrapolated", s.getEstimator().isConvergenceAchieved());
		Estimate<?> estimate = s.getEstimator().getParameterEstimates();
		Assert.assertEquals("Checking parm 2", -0.062640, estimate.getMean().getValueAt(1, 0), 5E-3);
		Assert.assertEquals("Checking standard error parm estimate 2", 0.020396, Math.sqrt(estimate.getVariance().getValueAt(1, 1)), 1E-3);
	}

	@AfterClass
	public static void doThat() {
		SIMEXModel.OverrideVarianceForTest = false;
	}

	@Test
	public void checkingPredParmsDataSetTest() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("sample0.csv");
		DataSet dataSet = new DataSet(filename, true);
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "y ~ distanceToConspecific");
		glm.doEstimation();
		SIMEXModel s = new SIMEXModel(glm, "distanceToConspecific", "variance");
		s.setNumberOfBootstrapRealizations(100);
		s.doEstimation();
		DataSet dsPred = s.getPredictedParameterEstimates();
		Assert.assertTrue("Testing that the dataset is not null", dsPred != null);
		int index = dsPred.getIndexOfThisField("pred");
		double parmValue = Double.parseDouble(dsPred.getObservations().get(0).getValueAt(index).toString());
		Estimate<?> estimate = s.getEstimator().getParameterEstimates();
		Assert.assertEquals("Checking parm estimate 1", estimate.getMean().getValueAt(0, 0), parmValue, 1E-8);
	}

	@Test
	public void checkingSIMEXPredictionsTest() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("sample0.csv");
		DataSet dataSet = new DataSet(filename, true);
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "y ~ distanceToConspecific");
		glm.doEstimation();
		SIMEXModel s = new SIMEXModel(glm, "distanceToConspecific", "variance");
		s.setNumberOfBootstrapRealizations(100);
		s.doEstimation();
		Matrix pred = s.getPredicted();
		Assert.assertTrue("Testing that the dataset is not null", pred != null);
		double predValue = pred.getValueAt(0, 0);
		Assert.assertEquals("Checking parm estimate 1", 0.054082156776618984, predValue, 3E-3);
	}

	
}
