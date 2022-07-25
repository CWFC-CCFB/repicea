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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

public class TruncatedGaussianModelTest {

	@BeforeClass
	public static void doThis() {
		Level l = Level.FINEST;
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(l);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);		
	}
	
	@Test
	public void TruncatedGaussianModelTest() throws IOException {
		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < 1000000; i++) {
			values.add(StatisticalUtility.getRandom().nextTruncatedGaussian(6, 10, 2, Double.POSITIVE_INFINITY));
		}
//		String path = ObjectUtility.getPackagePath(getClass());
//		CSVWriter writer = new CSVWriter(new File(path + "sample.csv"), false);
//		List<FormatField> fields = new ArrayList<FormatField>();
//		fields.add(new CSVField("Obs"));
//		writer.setFields(fields);
//		for (Double d : values) {
//			Object[] record  = new Object[1];
//			record[0] = d;
//			writer.addRecord(record);
//		}
//		writer.close();
		Matrix startingParameters = new Matrix(2,1);
		startingParameters.setValueAt(0, 0, 6);
		startingParameters.setValueAt(1, 0, 10);
		
		TruncatedGaussianModel wm = new TruncatedGaussianModel(values, 2, Double.POSITIVE_INFINITY, startingParameters);
//		((MaximumLikelihoodEstimator) wm.getEstimator()).gridSearch(0, 5, 7, .1);
//		wm.setParameters(startingParameters);
//		((MaximumLikelihoodEstimator) wm.getEstimator()).gridSearch(1, 8, 11, .1);
		wm.doEstimation();
		wm.getSummary();
//		Assert.assertTrue("Is convergence achieved?", wm.getEstimator().isConvergenceAchieved());
//		Assert.assertEquals("Shape parameter", 1d, wm.getParameters().getValueAt(0, 0), 1E-2);
//		Assert.assertEquals("Scale parameter", 1d, wm.getParameters().getValueAt(1, 0), 1E-2);
	}

}
