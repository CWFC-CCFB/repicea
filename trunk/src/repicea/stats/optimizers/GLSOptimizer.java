/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.optimizers;

import repicea.math.Matrix;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.model.StatisticalModel;
import repicea.stats.model.lmm.LinearMixedModel;

public class GLSOptimizer extends AbstractOptimizer {

	
	private final NewtonRaphsonOptimizer nro;
	private boolean firstStep;
	
	public GLSOptimizer() {
		nro = new NewtonRaphsonOptimizer();
		firstStep = true;
	}
	
	
	
	@Override
	public boolean optimize(StatisticalModel<? extends StatisticalDataStructure> model)	throws OptimizationException {
		if (!(model instanceof LinearMixedModel)) {
			throw new OptimizationException("The GLS algorithm is designed to work with LinearMixedModel instances only!");
		}
		LinearMixedModel lmm = (LinearMixedModel) model;
		do {
			fixedEffectsEstimationStep(lmm);
		} while (covarianceParametersEstimation(lmm) != 0);
		return nro.isConvergenceAchieved();
	}


	private int covarianceParametersEstimation(LinearMixedModel lmm) throws OptimizationException {
		nro.optimize(lmm);
		return nro.getNumberOfIterations();
	}
	
	private void fixedEffectsEstimationStep(LinearMixedModel lmm) {
		Matrix invV = lmm.getInverseMatrixV();
		Matrix matX = lmm.getDataStructure().getMatrixX();
		Matrix matY = lmm.getDataStructure().getVectorY();
		Matrix beta = matX.transpose().multiply(invV).multiply(matX).getInverseMatrix().multiply(matX.transpose()).multiply(invV).multiply(matY);
		if (firstStep) {
			Matrix residuals = matY.subtract(matX.multiply(beta));
			double sigma2 = residuals.transpose().multiply(residuals).m_afData[0][0] / (lmm.getDataStructure().getDataSet().getNumberOfObservations() - beta.m_iRows);
//			lmm.setResidualVariance(sigma2);
			Matrix theta = lmm.getParameters();
			theta.m_afData[0][0] = 8;
			theta.m_afData[1][0] = sigma2 - 8;
			lmm.setParameters(theta);
			firstStep = false;
		}
		lmm.setFixedEffectsParameters(beta);
	}
	
}
