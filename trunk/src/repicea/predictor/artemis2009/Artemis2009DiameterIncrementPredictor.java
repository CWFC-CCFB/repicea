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
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.REpiceaPredictor;
import repicea.util.Index;

@SuppressWarnings("serial")
public class Artemis2009DiameterIncrementPredictor extends REpiceaPredictor {

	protected static final String ModuleName = "accroissement"; 

	private Map<String, Artemis2009DiameterIncrementInternalPredictor> internalPredictors;

	public Artemis2009DiameterIncrementPredictor(boolean isParametersVariabilityEnabled, boolean isOtherRandomEffectsVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isOtherRandomEffectsVariabilityEnabled, isOtherRandomEffectsVariabilityEnabled);
		init();
	}

	@Override
	protected final void init() {
		internalPredictors = new HashMap<String, Artemis2009DiameterIncrementInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009DiameterIncrementInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix covparms = pd.getCovarianceParameters().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009DiameterIncrementInternalPredictor(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
				
				internalPredictor.setRandomEffect(HierarchicalLevel.PLOT, covparms.getSubMatrix(0, 0, 0, 0));
				internalPredictor.setRandomEffect(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, covparms.getSubMatrix(1, 1, 0, 0));
				internalPredictor.setResidualErrorCovariance(covparms.m_afData[3][0], covparms.m_afData[2][0]);
//				internalPredictor.setResidualErrorCovariance(covparms.m_afData[3][0], covparms.m_afData[2][0], simulationDates);
			}
		}
	}

	
	public double[] predictGrowth(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictGrowth(stand, tree);
		} else {
			return null;
		}
	}

	/**
	 * This method returns the cumulative variance of the diameter prediction which can be used as a correction 
	 * factor.
	 * @param stand a Artemis2009CompatibleStand instance 
	 * @param tree a Artemis2009CompatibleTree tree
	 * @return a double
	 */
	public double getPredictionVariance(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).getPredictionVariance(stand, tree);
		} else {
			return -1d;
		}
	}
	
}
