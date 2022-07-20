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

import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.optimizer.AbstractOptimizer.LineSearchMethod;
import repicea.stats.data.DataSet;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

public class GLModelWithMeasErrorTest {

	@Test
    public void TestGLModelWithMeasurementError() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelWithMeasErrorTest.class).concat("OccurrencePartDataset_ERS.csv");
		DataSet dataSet = new DataSet(filename, true);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(Level.OFF);
		
		GLMWithMeasurementError glmWithMeasError = new GLMWithMeasurementError(dataSet, "occurred ~ lnDt + distanceToConspecificOLD",
				new GLMUniformBerksonMeasErrorDefinition("distanceToConspecificOLD", 0d, null, null, .1));
		((MaximumLikelihoodEstimator) glmWithMeasError.getEstimator()).setLineSearchMethod(LineSearchMethod.HALF_STEP);
		glmWithMeasError.doEstimation();
		glmWithMeasError.getSummary();
		Assert.assertTrue("Testing if convergence has been reached", glmWithMeasError.getEstimator().isConvergenceAchieved());
		Assert.assertEquals("Testing the parameter estimate", -13.911668422111854, glmWithMeasError.getParameters().getValueAt(2, 0), 1E-8);
	}

 }
