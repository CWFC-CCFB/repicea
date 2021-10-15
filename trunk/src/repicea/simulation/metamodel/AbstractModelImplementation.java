/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
package repicea.simulation.metamodel;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataBlock;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.distributions.GaussianDistribution;

/**
 * A package class to handle the different type of meta-models (e.g. Richards-Chapman and others).
 * @author Mathieu Fortin - September 2021
 */
abstract class AbstractModelImplementation {

	static class Bound {
		final double lower;
		final double upper;
		final double extent;
		
		Bound(double lower, double upper) {
			this.lower = lower;
			this.upper = upper;
			this.extent = this.upper - lower;
		}

		boolean checkValue(double value) {
			if (value < lower || value > upper) {
				return false;
			} else {
				return true;
			}
		}
		
		double getRandomValue() {
			return lower + extent * StatisticalUtility.getRandom().nextDouble(); 
		}
	}

	
	protected List<Bound> bounds;
	private Matrix parameters;
	private Matrix parmsVarCov;
	protected final List<AbstractDataBlockWrapper> dataBlockWrappers;
	protected List<Integer> fixedEffectsParameterIndices;

	/**
	 * Internal constructor
	 * @param structure
	 * @param varCov
	 */
	AbstractModelImplementation(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		Map<String, DataBlock> formattedMap = new LinkedHashMap<String, DataBlock>();
		Map<String, DataBlock> ageMap = structure.getHierarchicalStructure(); 
		for (String ageKey : ageMap.keySet()) {
			DataBlock db = ageMap.get(ageKey);
			Object o = db.keySet();
			for (String speciesGroupKey : db.keySet()) {
				DataBlock innerDb = db.get(speciesGroupKey);
				formattedMap.put(ageKey + "_" + speciesGroupKey, innerDb);
			}
		}

		dataBlockWrappers = new ArrayList<AbstractDataBlockWrapper>();
		for (String k : formattedMap.keySet()) {
			DataBlock db = formattedMap.get(k);
			List<Integer> indices = db.getIndices();
			dataBlockWrappers.add(createDataBlockWrapper(k, indices, structure, varCov));
		}
	}

	abstract AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov);

	abstract Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect, boolean includePredVariance);

	final Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect) {
		return generatePredictions(dbw, randomEffect, false);
	}
	
	abstract double getPrediction(double ageYr, double timeSinceBeginning, double r1);
	
	abstract Matrix getFirstDerivative(double ageYr, double timeSinceBeginning, double r1);

	final double getPredictionVariance(double ageYr, double timeSinceBeginning, double r1) {
		if (parmsVarCov == null) {
			throw new InvalidParameterException("The variance-covariance matrix of the parameter estimates has not been set!");
		}
		Matrix firstDerivatives = getFirstDerivative(ageYr, timeSinceBeginning, r1);
		Matrix variance = firstDerivatives.transpose().multiply(parmsVarCov.getSubMatrix(fixedEffectsParameterIndices, fixedEffectsParameterIndices)).multiply(firstDerivatives);
		return variance.getValueAt(0, 0);
	}
	
	/**
	 * Return the loglikelihood for the model implementation. This likelihood is used in 
	 * the Metropolis-Hastings algorithm.
	 * @param parameters
	 * @return
	 */
	abstract double getLogLikelihood(Matrix parameters);

	void setParameters(Matrix parameters) {
		this.parameters = parameters;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			dbw.updateCovMat(this.parameters);
		}

	}
	
	Matrix getParameters() {
		return parameters;
	}
	
	Matrix getParmsVarCov() {
		return parmsVarCov;
	}
	
	void setParmsVarCov(Matrix m) {
		parmsVarCov = m;
	}

	Matrix getVectorOfPopulationAveragedPredictionsAndVariances() {
		int size = 0;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			size += dbw.indices.size();
		}
		Matrix predictions = new Matrix(size,2);
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			Matrix y_i = generatePredictions(dbw, 0d, true);
			for (int i = 0; i < dbw.indices.size(); i++) {
				int index = dbw.indices.get(i);
				predictions.setValueAt(index, 0, y_i.getValueAt(i, 0));
				predictions.setValueAt(index, 1, y_i.getValueAt(i, 1));
			}
		}
		return predictions;
	}

	abstract GaussianDistribution getStartingParmEst(double coefVar);

	boolean checkBounds(Matrix parms) {
		for (int i = 0; i < parms.m_iRows; i++) {
			if (!bounds.get(i).checkValue(parms.getValueAt(i, 0))) {
				return false;
			} 
		}
		return true;
	}
	
	Matrix getRandomValueBetweenBounds() {
		Matrix parms = new Matrix(bounds.size(), 1);
		for (int i = 0; i < bounds.size(); i++) {
			parms.setValueAt(i, 0, bounds.get(i).getRandomValue());
		}
		return parms;
	}

}
