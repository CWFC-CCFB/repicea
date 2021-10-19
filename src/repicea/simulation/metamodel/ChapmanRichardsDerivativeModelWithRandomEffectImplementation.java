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
 * An implementation of the derivative form of the Chapman-Richards model including random effects.
 * @author Mathieu Fortin - October 2021
 */
class ChapmanRichardsDerivativeModelWithRandomEffectImplementation extends ChapmanRichardsDerivativeModelImplementation {

	/**
	 * The likelihood implementation for this model implementation.
	 * @author Mathieu Fortin - September 2021
	 */
	@SuppressWarnings("serial")
	class DataBlockWrapper extends ChapmanRichardsDerivativeModelImplementation.DataBlockWrapper {
		
		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
		}
		
		
		@Override
		double getMarginalLogLikelihood() {
			Matrix lowerCholeskyTriangle = ChapmanRichardsDerivativeModelWithRandomEffectImplementation.this.getVarianceRandomEffect().getLowerCholTriangle();
			double integratedLikelihood = ghq.getIntegralApproximation(this, ghqIndices, lowerCholeskyTriangle);
			return Math.log(integratedLikelihood);
		}
		

	}

	int indexRandomEffectVariance;
	
	ChapmanRichardsDerivativeModelWithRandomEffectImplementation(String outputType, MetaModel model) throws StatisticalDataException {
		super(outputType, model);
	}

	
	Matrix getVarianceRandomEffect() {
		return getParameters().getSubMatrix(indexRandomEffectVariance, indexRandomEffectVariance, 0, 0);
	}


	@Override
	AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new DataBlockWrapper(k, indices, structure, varCov);
	}


	@Override
	double getLogLikelihood(Matrix parameters) {
		setParameters(parameters);
		double logLikelihood = 0d;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			double marginalLogLikelihoodForThisBlock = dbw.getMarginalLogLikelihood();
			logLikelihood += marginalLogLikelihoodForThisBlock;
		}
		return logLikelihood;
	}

	
	@Override
	GaussianDistribution getStartingParmEst(double coefVar) {
		Matrix parmEst = new Matrix(5,1);
		parmEst.setValueAt(0, 0, 1000d);
		parmEst.setValueAt(1, 0, 0.02);
		parmEst.setValueAt(2, 0, 2d);
		parmEst.setValueAt(3, 0, 1000d);
		parmEst.setValueAt(4, 0, .92);
		
		fixedEffectsParameterIndices = new ArrayList<Integer>();
		fixedEffectsParameterIndices.add(0);
		fixedEffectsParameterIndices.add(1);
		fixedEffectsParameterIndices.add(2);
		
		this.indexRandomEffectVariance = 3;
		this.indexCorrelationParameter = 4;
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		
		Matrix lowerBound = new Matrix(5,1);
		Matrix upperBound = new Matrix(5,1);
		lowerBound.setValueAt(0, 0, 0);
		upperBound.setValueAt(0, 0, 2000);
		
		lowerBound.setValueAt(1, 0, 0.00001);
		upperBound.setValueAt(1, 0, 0.05);
		
		lowerBound.setValueAt(2, 0, 1);
		upperBound.setValueAt(2, 0, 6);

		lowerBound.setValueAt(3, 0, 0);
		upperBound.setValueAt(3, 0, 2000);

		lowerBound.setValueAt(4, 0, .90);
		upperBound.setValueAt(4, 0, .99);

		priors = new UniformDistribution(lowerBound, upperBound);

		return gd;
	}

	
}
