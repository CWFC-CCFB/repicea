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

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
//import org.apache.commons.math.special.Gamma;

@SuppressWarnings("serial")
class Artemis2009RecruitmentNumberInternalPredictor extends REpiceaPredictor {

	private List<Integer> effectList;
	
	protected Artemis2009RecruitmentNumberInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
	}

	protected void init() {
		effectList = new ArrayList<Integer>();
	}
	
	protected void setBeta(Matrix beta, Matrix omega) {
		ModelParameterEstimates estimate = new SASParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectList) {
		for (int i = 0; i < effectList.m_iRows; i++) {
			this.effectList.add((int) effectList.m_afData[i][0]);
		}
	}
	
	protected synchronized double predictNumberOfRecruits(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(stand).getDeepClone();
		double dispersion = beta.m_afData[beta.m_iRows - 1][0];
		beta.m_afData[beta.m_iRows-1][0] = 1.0;    // last element is replaced by 1 to account for the offset variable	
	
		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2009RecruitmentNumberPredictor.ModuleName, effectList);
		double xBeta = oXVector.multiply(beta).m_afData[0][0];
		double predictedValue = Math.exp(xBeta);
		
		if (isResidualVariabilityEnabled) {
			int nbRecruits = random.nextNegativeBinomial(predictedValue, dispersion) + 1; // 1 is required since the modelled value was y - 1 
			if (nbRecruits > 80 && !Artemis2009RecruitmentNumberPredictor.Override80Limit) { // maximum number of recruits is set to 80
				nbRecruits = 80;
			}
			return nbRecruits;
		} else {
			return predictedValue + 1d;		// deterministic implementation: 1 is required since the modelled value was y - 1
		}
	}


}
