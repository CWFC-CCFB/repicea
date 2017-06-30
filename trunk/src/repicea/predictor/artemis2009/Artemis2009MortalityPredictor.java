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
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.util.Index;

/**
 * The Artemis2009MortalityPredictor class implements the mortality module for Artemis-2009 simulator.
 * @author Mathieu Fortin - July 2014
 */
@SuppressWarnings("serial")
public final class Artemis2009MortalityPredictor extends REpiceaLogisticPredictor<Artemis2009CompatibleStand, Artemis2009CompatibleTree> {

	protected static final String ModuleName = "mortalite"; 

	private Map<String, Artemis2009MortalityInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled
	 * @param isResidualVariabilityEnabled
	 */
	public Artemis2009MortalityPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this module
		init();
	}

	protected void init() {
		internalPredictors = new HashMap<String, Artemis2009MortalityInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009MortalityInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009MortalityInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	@Override
	public double predictEventProbability(Artemis2009CompatibleStand stand,	Artemis2009CompatibleTree tree, Object... parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictEventProbability(stand, tree);
		} else {
			return -1d;
		}
	}
	
	@Override
	public void clearDeviates() {
		for (Artemis2009MortalityInternalPredictor p : internalPredictors.values()) {
			p.clearDeviates();
		}
	}


}
