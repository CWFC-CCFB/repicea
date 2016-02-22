/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.artemis2009;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
class Artemis2009RecruitDiameterInternalPredictor extends ModelBasedSimulator {

	private List<Integer> effectList;
	private RandomDataImpl randomGenerator;

	protected Artemis2009RecruitDiameterInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
	}

	protected void init() {
		effectList = new ArrayList<Integer>();
		randomGenerator = new RandomDataImpl();
	}

	
	protected void setBeta(Matrix beta, Matrix omega) {
		GaussianEstimate estimate = new SASParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectList) {
		for (int i = 0; i < effectList.m_iRows; i++) {
			this.effectList.add((int) effectList.m_afData[i][0]);
		}
	}
	
	protected synchronized double[] predictRecruitDiameter(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(stand);
		
		double dispersion = beta.m_afData[beta.m_iRows-1][0];	// last element (dispersion) is taken out of the vector
		beta = beta.getSubMatrix(0, beta.m_iRows - 2, 0, 0); 	// vector is resized to omit the last element (dispersion)

		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2009MortalityPredictor.ModuleName, effectList);
		double xBeta = oXVector.multiply(beta).m_afData[0][0];
		
		double fGammaMean = Math.exp(xBeta);

		double dVariance = 0.0;

		double scale = dispersion;
		double shape = fGammaMean/dispersion;

		double[] result = new double[2];
		double fDiameter;

		if (isResidualVariabilityEnabled) {
			double randomDeviate = 0d;
			try {
				randomDeviate = randomGenerator.nextGamma(shape, scale);
			} catch (Exception e) {
				throw new InvalidParameterException("The parameter of the Gamma distribution are inconsistent!");
			}
			fDiameter = 9.1 + randomDeviate * 0.1;	
			if (fDiameter > 21) {
				fDiameter = 21;			// limiter for inconsistent predictions
			}
		} else {
			fDiameter = 9.1 + fGammaMean * 0.1;
			dVariance = Math.exp(2.0 * xBeta) / dispersion * 0.01; // factor 100 is required to ensure a proper conversion from mm to cm
		}
		
		result[0] = fDiameter;
		result[1] = dVariance;
		return result;
	}

}
