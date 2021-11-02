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
package repicea.stats.mcmc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.distributions.ContinuousDistribution;
import repicea.stats.distributions.GaussianDistribution;

/** 
 * A class to handle prior distributions.
 * @author Mathieu Fortin - November 2021
 */
class MetropolisHastingsPriorHandler {
	final Map<ContinuousDistribution, List<Integer>> distributions;
	final Map<GaussianDistribution, ContinuousDistribution> randomEffectDistributions;
	int nbElements;

	MetropolisHastingsPriorHandler() {
		distributions = new LinkedHashMap<ContinuousDistribution, List<Integer>>();
		randomEffectDistributions = new HashMap<GaussianDistribution, ContinuousDistribution>();
	}

	Matrix getRandomRealization() {
		Matrix realization = new Matrix(nbElements, 1);
		for (ContinuousDistribution d : distributions.keySet()) {
			updateRandomEffectVarianceIfNeedsBe(d, realization);
			Matrix thisR = d.getRandomRealization();
			List<Integer> indices = distributions.get(d);
			realization.setElements(indices, thisR);
		}
		return realization;
	}

	private void updateRandomEffectVarianceIfNeedsBe(ContinuousDistribution d, Matrix realization) {
		if (randomEffectDistributions.containsKey(d)) {	// it is a random effect. So we must update its variance
			ContinuousDistribution varianceDist = randomEffectDistributions.get(d);
			int index = distributions.get(varianceDist).get(0);	// TODO FP MF2021-11-01 here we assume that there is only one index 
			Matrix realizedRandomEffectVariance = realization.getSubMatrix(index, index, 0, 0);
			((GaussianDistribution) d).setVariance(realizedRandomEffectVariance);
		}
	}

	double getProbabilityDensity(Matrix m) {
		double logProb = 0;
		for (ContinuousDistribution d : distributions.keySet()) {
			updateRandomEffectVarianceIfNeedsBe(d, m);
			List<Integer> indices = distributions.get(d);
			double thisProb = d.getProbabilityDensity(m.getSubMatrix(indices, null));
			if (thisProb == 0d) {
				return 0d;
			}
			logProb += Math.log(thisProb);
		}
		return Math.exp(logProb);
	}


	void addFixedEffectDistribution(ContinuousDistribution dist, Integer... indices) {
		List<Integer> ind = Arrays.asList(indices);
		distributions.put(dist, ind);
		nbElements += ind.size();
	}

	void addRandomEffectVariance(GaussianDistribution dist, ContinuousDistribution variancePrior, Integer... indices) {
		addFixedEffectDistribution(dist, indices);
		randomEffectDistributions.put(dist, variancePrior);
	}

}
