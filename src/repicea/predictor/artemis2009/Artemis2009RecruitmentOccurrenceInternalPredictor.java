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
import repicea.simulation.LogisticModelBasedSimulator;

@SuppressWarnings("serial")
class Artemis2009RecruitmentOccurrenceInternalPredictor extends LogisticModelBasedSimulator<Artemis2009CompatibleStand, Artemis2009CompatibleTree> {

	private List<Integer> effectList;
	
	protected Artemis2009RecruitmentOccurrenceInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
	}

	protected void init() {
		effectList = new ArrayList<Integer>();
	}
	
	protected void setBeta(Matrix beta, Matrix omega) {
		defaultBeta = new SASParameterEstimate(beta, omega);
		oXVector = new Matrix(1, defaultBeta.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectList) {
		for (int i = 0; i < effectList.m_iRows; i++) {
			this.effectList.add((int) effectList.m_afData[i][0]);
		}
	}
	
	@Override
	public synchronized double predictEventProbability(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree, Object... parms) {
		Matrix beta = getParametersForThisRealization(stand);
		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2009RecruitmentOccurrencePredictor.ModuleName, effectList);
		double xBeta = oXVector.multiply(beta).m_afData[0][0];
		double recruitmentProbability = Math.exp(xBeta)/(1.0 + Math.exp(xBeta));
		return recruitmentProbability;
	}

}