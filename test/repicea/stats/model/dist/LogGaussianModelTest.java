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
package repicea.stats.model.dist;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.util.REpiceaLogManager;

public class LogGaussianModelTest {

	@BeforeClass
	public static void doThis() {
		Level l = Level.OFF;
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);		
	}

	@Test
	public void simpleDistributionTest() {
		double mu = 3;
		double sigma2 = 10;
		double sigma = Math.sqrt(sigma2);
		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < 100000; i++) {
			double randomValue = Math.exp(StatisticalUtility.getRandom().nextGaussian() * sigma + mu);
			values.add(randomValue);
		}

		LogGaussianModel gm = new LogGaussianModel(values);
		gm.doEstimation();
		gm.getSummary();
		double observedMu = gm.getParameters().getValueAt(0, 0);
		double observedSigma2 = gm.getParameters().getValueAt(1, 0);
		Assert.assertTrue("Checking if the model has converged", ((MaximumLikelihoodEstimator) gm.getEstimator()).isConvergenceAchieved());
		Assert.assertEquals("Checking mu parameter", mu, observedMu, 5E-2);
		Assert.assertEquals("Checking variance parameter", sigma2, observedSigma2, 1E-1);
	}

}
