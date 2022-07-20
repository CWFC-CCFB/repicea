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
package repicea.stats.model.glm.measerr.simulation;

import java.util.List;

import repicea.math.Matrix;
import repicea.math.optimizer.AbstractOptimizer.LineSearchMethod;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataSet;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.measerr.GLMUniformBerksonMeasErrorDefinition;
import repicea.stats.model.glm.measerr.GLMWithMeasurementError;
import repicea.stats.sampling.SamplingUtility;

class NonSpatialPopulation extends AbstractPopulation<NonSpatialPopulationUnit>{

	NonSpatialPopulation(Matrix trueBeta, int size) {
		this(trueBeta, size, false);
	}

	NonSpatialPopulation(Matrix trueBeta, int size, boolean isClone) {
		super(trueBeta, size);
		if (!isClone) {
			int id = 0;
			for (int x = 1; x <= size; x++) {
				for (int y = 1; y <= size; y++) {
					if (y <= size * .5) {
						populationUnits.add(new NonSpatialPopulationUnit(id++, StatisticalUtility.getRandom().nextDouble() < 0.6));
					} else {
						populationUnits.add(new NonSpatialPopulationUnit(id++, false));
					}
				}
			}
			generateRealizations();
		}
	}
	
	@Override
	public Object[] samplePopulation(int n) {
		Object[] record = new Object[6];
		int nbPositive = generateRealizations();
//		System.out.println("Nb positive cases = " + nbPositive);
		List<NonSpatialPopulationUnit> sample = SamplingUtility.getSample(populationUnits, n);
		
		DataSet ds = new DataSet(sample.get(0).getFieldname(false));
		for (PopulationUnit pu : sample) {
			ds.addObservation(pu.asObservation(false));
		}
		ds.indexFieldType();
		
		GeneralizedLinearModel pre_glm = new GeneralizedLinearModel(ds, Type.CLogLog, "y ~ distanceToConspecific");
		pre_glm.doEstimation();
//		pre_glm.getSummary();
		GLMWithMeasurementError glm = new GLMWithMeasurementError(ds, "y ~ distanceToConspecificMax", pre_glm.getParameters(), 
				new GLMUniformBerksonMeasErrorDefinition("distanceToConspecificMax", 0d, "distanceToConspecificMin", "distanceToConspecificMax", 0.1));
		((MaximumLikelihoodEstimator) glm.getEstimator()).setLineSearchMethod(LineSearchMethod.HALF_STEP);

		glm.doEstimation();
//		glm.getSummary();
		if (glm.getEstimator().isConvergenceAchieved()) {
			record = new Object[8];
			Estimate<?> est = glm.getEstimator().getParameterEstimates();
			record[0] = trueBeta.getValueAt(0, 0);
			record[1] = trueBeta.getValueAt(1, 0);
			record[2] = est.getMean().getValueAt(0, 0);
			record[3] = est.getVariance().getValueAt(0, 0);
			record[4] = est.getMean().getValueAt(1, 0);
			record[5] = est.getVariance().getValueAt(1, 1);
			record[6] = pre_glm.getParameters().getValueAt(0, 0);
			record[7] = pre_glm.getParameters().getValueAt(1, 0);
			return record;
		} else {
			return null;
		}
	}

	@Override
	public NonSpatialPopulation clone() {
		NonSpatialPopulation pop = new NonSpatialPopulation(trueBeta, size, true);
		for (NonSpatialPopulationUnit pu : populationUnits) {
			pop.populationUnits.add(pu.clone());
		}
		return pop;
	}

	
	
}
