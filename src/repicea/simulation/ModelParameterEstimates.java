/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictorEvent.ModelBasedSimulatorEventProperty;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
public class ModelParameterEstimates extends SASParameterEstimates {
	
	static class SubjectSkeleton implements MonteCarloSimulationCompliantObject, Serializable {

		final String subjectId;
		final HierarchicalLevel level;
		int monteCarloRealizationId;
		
		SubjectSkeleton(String subjectId, HierarchicalLevel level) {
			this.subjectId = subjectId;
			this.level = level;
		}
		
		
		@Override
		public String getSubjectId() {
			return subjectId;
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return level;
		}

		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloRealizationId;
		}
	}
	
	static class SubjectRelatedIndices extends ArrayList<Integer> {
		
		final SubjectSkeleton skeleton;
		
		SubjectRelatedIndices(SubjectSkeleton skeleton) {
			this.skeleton = skeleton;
		}
	}
	
	private final REpiceaPredictor model;
	
	private final boolean sasEstimateDerived;
	
	private final Matrix fixedEffectsPart;
	private final List<Integer> estimatedFixedEffectParameterIndices;
	
	private final Map<String, Map<String, SubjectRelatedIndices>> subjectIndex;
	
	protected ModelParameterEstimates(GaussianEstimate estimate, REpiceaPredictor model) {
		super(estimate.getMean(), estimate.getVariance());
		this.model = model;
		fixedEffectsPart = estimate.getMean();
		sasEstimateDerived = estimate instanceof SASParameterEstimates;
		if (!sasEstimateDerived) {
			estimatedParameterIndices.clear();
			for (int i = 0; i < getMean().m_iRows; i++) {
				estimatedParameterIndices.add(i);
			}
		}
		estimatedFixedEffectParameterIndices = new ArrayList<Integer>();
		estimatedFixedEffectParameterIndices.addAll(estimatedParameterIndices);
		subjectIndex = new HashMap<String, Map<String, SubjectRelatedIndices>>();
	}
	
	protected REpiceaPredictor getModel() {return model;}
	
	protected void registerBlups(Matrix mean, Matrix variance, Matrix covariance, List<MonteCarloSimulationCompliantObject> subjectList) {
		int indexFirstBlup = getMean().m_iRows;
		int nbBlupsPerSubject = mean.m_iRows / subjectList.size();
		for (int i = 0; i < mean.m_iRows; i++) {
			estimatedParameterIndices.add(i + getNumberOfFixedEffectParameters());
		}
		Matrix newMean = getMean().matrixStack(mean, true);
		Matrix newVariance = getVariance().matrixStack(covariance.transpose(), false).matrixStack(covariance.matrixStack(variance, false), true);
		setMean(newMean);
		setVariance(newVariance);
		for (int i = 0; i < subjectList.size(); i++) {
			MonteCarloSimulationCompliantObject subject = subjectList.get(i);
			String levelName = subject.getHierarchicalLevel().getName();
			if (!subjectIndex.containsKey(levelName)) {
				subjectIndex.put(levelName, new HashMap<String, SubjectRelatedIndices>());
			}
			Map<String, SubjectRelatedIndices> innerMap = subjectIndex.get(levelName);
			String subjectId = subject.getSubjectId();
			if (!innerMap.containsKey(subjectId)) {
				innerMap.put(subjectId, new SubjectRelatedIndices(new SubjectSkeleton(subjectId, subject.getHierarchicalLevel())));
			}
			for (int j = 0; j < nbBlupsPerSubject; j++) {
				innerMap.get(subjectId).add(indexFirstBlup++);
			}
		}
		REpiceaPredictorEvent event = new REpiceaPredictorEvent(ModelBasedSimulatorEventProperty.BLUPS_JUST_SET, 
				null, 
				new Object[]{getModel().defaultRandomEffects, subjectList}, 
				getModel());
		getModel().fireModelBasedSimulatorEvent(event);
	}
	
	
	protected boolean doBlupsExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel level = subject.getHierarchicalLevel();
		return subjectIndex.containsKey(level.getName()) && subjectIndex.get(level.getName()).containsKey(subject.getSubjectId());
	}

	protected GaussianEstimate getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		if (doBlupsExistForThisSubject(subject)) {
			List<Integer> paramIndices = subjectIndex.get(subject.getHierarchicalLevel().getName()).get(subject.getSubjectId());
			List<Integer> varianceIndices = getVarianceIndicesForThoseParameterIndices(paramIndices);
			return new GaussianEstimate(getMean().getSubMatrix(paramIndices, REpiceaPredictor.DefaultZeroIndex), getVariance().getSubMatrix(varianceIndices, varianceIndices));
		} else {
			return null;
		}
	}
	
	protected void simulateBlups(MonteCarloSimulationCompliantObject subject) {
		Matrix simulatedDeviate = getRandomDeviate();
		getModel().simulatedParameters.put(subject.getMonteCarloRealizationId(), simulatedDeviate.getSubMatrix(0, getNumberOfFixedEffectParameters() - 1, 0, 0));
		Matrix parametersForThisRealization = getModel().simulatedParameters.get(subject.getMonteCarloRealizationId());
		getModel().fireModelBasedSimulatorEvent(new REpiceaPredictorEvent(ModelBasedSimulatorEventProperty.PARAMETERS_DEVIATE_JUST_GENERATED, 
				null, 
				new Object[]{subject.getMonteCarloRealizationId(), parametersForThisRealization.getDeepClone()}, 
				getModel()));
		for (String levelName : subjectIndex.keySet()) {
			Map<String, SubjectRelatedIndices> subjectList = subjectIndex.get(levelName);
			for (String subjectId : subjectList.keySet()) {
				SubjectRelatedIndices indices = subjectList.get(subjectId);
				SubjectSkeleton skeleton = indices.skeleton;
				skeleton.monteCarloRealizationId = subject.getMonteCarloRealizationId();
				Matrix randomDeviates = simulatedDeviate.getSubMatrix(indices, REpiceaPredictor.DefaultZeroIndex);
				getModel().setDeviatesForRandomEffectsOfThisSubject(indices.skeleton, randomDeviates);
				Estimate<? extends StandardGaussianDistribution> defaultRandomEffect = getModel().defaultRandomEffects.get(levelName);
				getModel().fireRandomEffectDeviateGeneratedEvent(skeleton, defaultRandomEffect, randomDeviates);
			}
		}
	}
	
	protected Matrix getFixedEffectsPart() {return fixedEffectsPart;}
	
	/**
	 * This method returns the indices of the true parameters in case of a SAS implementation. 
	 * @return a List of Integer which is a copy of the original list to avoid modifications.
	 */
	public List<Integer> getTrueParameterIndices() {
		List<Integer> copyList = new ArrayList<Integer>();
		copyList.addAll(estimatedParameterIndices);
		return copyList;
	}
	
	/**
	 * This method returns the number of fixed-effect parameters in the model.
	 * @return an integer
	 */
	public int getNumberOfFixedEffectParameters() {
		return getFixedEffectsPart().m_iRows;
	}
}