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

import repicea.math.Matrix;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.UniformDistribution;

class SimpleSlopeModelImplementation extends AbstractModelImplementation {

	SimpleSlopeModelImplementation(String outputType, MetaModel model) throws StatisticalDataException {
		super(outputType, model);
	}

	@Override
	double getPrediction(double ageYr, double timeSinceBeginning, double r1, Matrix parameters) {
		Matrix params = parameters == null ? getParameters() : parameters;
		double b1 = params.getValueAt(0, 0);
		double pred = (b1 + r1) * ageYr;
		return pred;
	}


	@Override
	public GaussianDistribution getStartingParmEst(double coefVar) {
		this.indexCorrelationParameter = 1;
		
		Matrix parmEst = new Matrix(2,1);
		parmEst.setValueAt(0, 0, 0.1);
		parmEst.setValueAt(1, 0, .92);
		
		fixedEffectsParameterIndices = new ArrayList<Integer>();
		fixedEffectsParameterIndices.add(0);

		mh.getPriorHandler().addFixedEffectDistribution(new UniformDistribution(0.00001, 4), 0);
		mh.getPriorHandler().addFixedEffectDistribution(new UniformDistribution(0.80, 0.995), 1);

		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		return gd;
	}

	@Override
	Matrix getFirstDerivative(double ageYr, double timeSinceBeginning, double r1) {
		Matrix derivatives = new Matrix(1,1);
		derivatives.setValueAt(0, 0, ageYr);
		return derivatives;
	}


}
