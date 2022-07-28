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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.math.optimizer.AbstractOptimizer.LineSearchMethod;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.data.DataSet;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

public class GLModelWithMeasErrorTest {

	@BeforeClass
	public static void doThis() {
		Level l = Level.FINEST;
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);		
	}

//	@Ignore
//	@Test
//    public void TestGLModelWithMeasurementError() throws Exception {
// 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("OccurrencePartDataset_ERS.csv");
//		DataSet dataSet = new DataSet(filename, true);
//		
//		GLMWithMeasurementError glmWithMeasError = new GLMWithMeasurementError(dataSet, "occurred ~ lnDt + distanceToConspecificOLD",
//				new GLMUniformBerksonMeasErrorDefinition("distanceToConspecificOLD", 0d, null, null, .1));
//		((MaximumLikelihoodEstimator) glmWithMeasError.getEstimator()).setLineSearchMethod(LineSearchMethod.HALF_STEP);
//		glmWithMeasError.doEstimation();
//		glmWithMeasError.getSummary();
//		Assert.assertTrue("Testing if convergence has been reached", glmWithMeasError.getEstimator().isConvergenceAchieved());
//		Assert.assertEquals("Testing the parameter estimate", -13.911668422111854, glmWithMeasError.getParameters().getValueAt(2, 0), 1E-8);
//	}

	
	@Test
    public void TestGLModelWithClassicalMeasurementError() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("sample0.csv");
		DataSet dataSet = new DataSet(filename, true);
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "y ~ distanceToConspecific");
		glm.doEstimation();
		glm.getSummary();
		GLMWithMeasurementError glmWithMeasError = new GLMWithMeasurementError(dataSet, "y ~ distanceToConspecific",
				glm.getParameters(), new GLMNormalClassicalMeasErrorDefinition("distanceToConspecific", 3, .2));
		((MaximumLikelihoodEstimator) glmWithMeasError.getEstimator()).setLineSearchMethod(LineSearchMethod.HALF_STEP);
		glmWithMeasError.doEstimation();
		glmWithMeasError.getSummary();
		Assert.assertTrue("Testing if convergence has been reached", glmWithMeasError.getEstimator().isConvergenceAchieved());
		Assert.assertEquals("Testing the parameter estimate", -0.05426475567951968, glmWithMeasError.getParameters().getValueAt(1, 0), 1E-8);
	}

	

 }
