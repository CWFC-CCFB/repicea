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
public class Artemis2009RecruitDiameterPredictor extends REpiceaPredictor {

	protected static final String ModuleName = "recrutement_g"; 

	private Map<String, Artemis2009RecruitDiameterInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the variability in the parameter estimate and the residual variance
	 */
	public Artemis2009RecruitDiameterPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	
	protected Artemis2009RecruitDiameterPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {		// no random effect in this module
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this module
		init();
	}

	
	protected void init() {
		internalPredictors = new HashMap<String, Artemis2009RecruitDiameterInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009RecruitDiameterInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009RecruitDiameterInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	/**
	 * This method predicts the diameter of a recruit.
	 * @param stand
	 * @param tree
	 * @return an array of two doubles, the first one being the predicted diameter and the second one being its variance
	 */
	public double[] predictRecruitDiameter(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree, Object... parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictRecruitDiameter(stand, tree);
		} else {
			double[] result = new double[2];
			result[0] = -1d;
			result[1] = -1d;
			return result;
		}
	}

//	@Override
//	public void clearDeviates() {
//		for (Artemis2009RecruitDiameterInternalPredictor p : internalPredictors.values()) {
//			p.clearDeviates();
//		}
//	}

//	public static void main(String[] args) {
//		Artemis2009RecruitDiameterPredictor pred = new Artemis2009RecruitDiameterPredictor(false, false);
//		int u = 0;
//	}

}
