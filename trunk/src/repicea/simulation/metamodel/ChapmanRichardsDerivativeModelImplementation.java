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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.UniformDistribution;

/**
 * An implementation of the derivative form of the Chapman-Richards model.
 * @author Mathieu Fortin - October 2021
 */
class ChapmanRichardsDerivativeModelImplementation extends ChapmanRichardsModelImplementation {

	@SuppressWarnings("serial")
	class DataBlockWrapper extends ChapmanRichardsModelImplementation.DataBlockWrapper {
		
		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
		}
	}

	ChapmanRichardsDerivativeModelImplementation(String stratumGroup, 
			String outputType, 
			Map<Integer, ScriptResult> scriptResults) throws StatisticalDataException {
		super(stratumGroup, outputType, scriptResults);
	}
	
	
	@Override
	double getPrediction(double ageYr, double timeSinceBeginning, double r1) {
		double b1 = getParameters().getValueAt(0, 0);
		double b2 = getParameters().getValueAt(1, 0);
		double b3 = getParameters().getValueAt(2, 0);
		double pred = (b1 + r1) * Math.exp(-b2 * ageYr) * Math.pow(1 - Math.exp(-b2 * ageYr), b3);
		return pred;
	}


	@Override
	AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new DataBlockWrapper(k, indices, structure, varCov);
	}
	

	
	@Override
	GaussianDistribution getStartingParmEst(double coefVar) {
		Matrix parmEst = new Matrix(4,1);
		parmEst.setValueAt(0, 0, 1000d);
		parmEst.setValueAt(1, 0, 0.02);
		parmEst.setValueAt(2, 0, 2d);
		parmEst.setValueAt(3, 0, .92);
		
		fixedEffectsParameterIndices = new ArrayList<Integer>();
		fixedEffectsParameterIndices.add(0);
		fixedEffectsParameterIndices.add(1);
		fixedEffectsParameterIndices.add(2);

		this.indexCorrelationParameter = 3;
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		Matrix lowerBound = new Matrix(4,1);
		Matrix upperBound = new Matrix(4,1);
		lowerBound.setValueAt(0, 0, 0);
		upperBound.setValueAt(0, 0, 2000);
		
		lowerBound.setValueAt(1, 0, 0.00001);
		upperBound.setValueAt(1, 0, 0.05);
		
		lowerBound.setValueAt(2, 0, 1);
		upperBound.setValueAt(2, 0, 6);

		lowerBound.setValueAt(3, 0, .90);
		upperBound.setValueAt(3, 0, .99);

		priors = new UniformDistribution(lowerBound, upperBound);

		return gd;
	}

	@Override
	Matrix getFirstDerivative(double ageYr, double timeSinceBeginning, double r1) {
		double b1 = getParameters().getValueAt(0, 0);
		double b2 = getParameters().getValueAt(1, 0);
		double b3 = getParameters().getValueAt(2, 0);
		
		double exp = Math.exp(-b2 * ageYr);
		double root = 1 - exp;
		
		Matrix derivatives = new Matrix(3,1);
		derivatives.setValueAt(0, 0, exp * Math.pow(root, b3));
		derivatives.setValueAt(1, 0, - ageYr * b1 * exp * Math.pow(root, b3) + 
				b1 * exp * b3 * Math.pow(root, b3 - 1) * exp * ageYr);
		derivatives.setValueAt(2, 0, b1 * exp * Math.pow(root, b3) * Math.log(root));
		return derivatives;
	}


	
}
