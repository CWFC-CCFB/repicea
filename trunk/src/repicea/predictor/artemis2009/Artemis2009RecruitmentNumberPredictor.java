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

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.util.Index;

@SuppressWarnings("serial")
public class Artemis2009RecruitmentNumberPredictor extends REpiceaPredictor {

	protected static final String ModuleName = "recrutement_b"; 

	private Map<String, Artemis2009RecruitmentNumberInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled
	 * @param isResidualVariabilityEnabled
	 */
	public Artemis2009RecruitmentNumberPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this module
		init();
	}

	protected void init() {
		internalPredictors = new HashMap<String, Artemis2009RecruitmentNumberInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009RecruitmentNumberInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009RecruitmentNumberInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	/**
	 * This method predicts the number of recruits to be observed given that recruitment occurs.
	 * @param stand
	 * @param tree
	 * @param parms
	 * @return a double
	 */
	public double predictNumberOfRecruits(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree, Object... parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictNumberOfRecruits(stand, tree);
		} else {
			return -1d;
		}
	}
	
//	public static void main(String[] args) {
//		Artemis2009RecruitmentNumberPredictor pred = new Artemis2009RecruitmentNumberPredictor(false, false);
//		int u = 0;
//	}

}
