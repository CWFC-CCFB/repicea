/*
 * This file is part of the repicea library.
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
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulatorEvent.ModelBasedSimulatorEventProperty;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

/**
 * This class is the basic class for all models that are designed for predictions.
 * It implements the methods for stochastic simulations.
 * @author Mathieu Fortin - October 2011
 */
@SuppressWarnings("serial")
public abstract class ModelBasedSimulator implements Serializable {

	protected static final List<Integer> DefaultZeroIndex = new ArrayList<Integer>();
	static {
		DefaultZeroIndex.add(0);
	}
	
	/**
	 * The SASParameterEstimate class is customized for SAS outputs. The major difference
	 * is related to how the random deviates are calculated. Since SAS produces false estimates,
	 * such as 0 for reference class or 1 for offset, the getRandomDeviate() method makes sure
	 * the false estimate are not accounted for during the simulation of the random deviates.
	 * @author Mathieu Fortin - September 2012
	 */
	public static class SASParameterEstimate extends GaussianEstimate {
		
		protected final List<Integer> trueParameterIndices;
		
		/**
		 * Constructor.
		 * @param mean a vector that corresponds to the mean value
		 * @param variance a symmetric positive definite matrix 
		 */
		public SASParameterEstimate(Matrix mean, Matrix variance) {
			super(mean, variance);
			trueParameterIndices = new ArrayList<Integer>();
			for (int i = 0; i < mean.m_iRows; i++) {
				if (mean.m_afData[i][0] != 0.0 && mean.m_afData[i][0] != 1.0) { 
					trueParameterIndices.add(i);
				}
			}
		}
		
		@Override
		public Matrix getRandomDeviate() {
			Matrix lowerChol = getDistribution().getStandardDeviation();
			Matrix randomVector = StatisticalUtility.drawRandomVector(lowerChol.m_iRows, Distribution.Type.GAUSSIAN);
			Matrix oMat = lowerChol.multiply(randomVector);
			Matrix deviate = getMean().getDeepClone();
			deviate.addElementsAt(trueParameterIndices, oMat);
			return deviate;
		}
	}
	
	
	protected static class IntervalNestedInPlotDefinition implements MonteCarloSimulationCompliantObject, Serializable {

		private final int monteCarloRealization;
		private final String subjectID;
		
		protected IntervalNestedInPlotDefinition(MonteCarloSimulationCompliantObject stand, int date) {
			monteCarloRealization = stand.getMonteCarloRealizationId();
			subjectID = getSubjectID(stand, date);
		}
		
		
		@Override
		public String getSubjectId() {
			return subjectID;
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return HierarchicalLevel.INTERVAL_NESTED_IN_PLOT;
		}


		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloRealization;
		}
		
		private static String getSubjectID(MonteCarloSimulationCompliantObject stand, int date) {
			return stand.getSubjectId() + date;
		}
	}

	
	public class ParameterEstimates extends SASParameterEstimate {
		
		private final int firstBlupIndex;
		private final boolean sasEstimateDerived;
		private final Matrix fixedEffectsPart;
		
		private final Map<String, Map<String, List<Integer>>> subjectIndex;
		
		protected ParameterEstimates(GaussianEstimate estimate) {
			super(estimate.getMean(), estimate.getVariance());
			fixedEffectsPart = estimate.getMean();
			sasEstimateDerived = estimate instanceof SASParameterEstimate;
			firstBlupIndex = getMean().m_iRows;
			if (!sasEstimateDerived) {
				trueParameterIndices.clear();
				for (int i = 0; i < firstBlupIndex; i++) {
					trueParameterIndices.add(i);
				}
			}
			subjectIndex = new HashMap<String, Map<String, List<Integer>>>();
		}
		
		protected void registerBlups(Matrix mean, 
				Matrix variance, 
				Matrix covariance, 
				List<MonteCarloSimulationCompliantObject> subjectList) {
			int nbBlupsPerSubject = mean.m_iRows / subjectList.size();
			Matrix newMean = getMean().matrixStack(mean, true);
			Matrix newVariance = getVariance().matrixStack(covariance.transpose(), false).matrixStack(covariance.matrixStack(variance, false), true);
			setMean(newMean);
			setVariance(newVariance);
			int index = firstBlupIndex;
			for (int i = 0; i < subjectList.size(); i++) {
				MonteCarloSimulationCompliantObject subject = subjectList.get(i);
				String levelName = subject.getHierarchicalLevel().getName();
				if (!subjectIndex.containsKey(levelName)) {
					subjectIndex.put(levelName, new HashMap<String, List<Integer>>());
				}
				Map<String, List<Integer>> innerMap = subjectIndex.get(levelName);
				String subjectId = subject.getSubjectId();
				if (!innerMap.containsKey(subjectId)) {
					innerMap.put(subjectId, new ArrayList<Integer>());
				}
				for (int j = 0; j < nbBlupsPerSubject; j++) {
					innerMap.get(subjectId).add(index++);
				}
			}
			ModelBasedSimulatorEvent event = new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.BLUPS_JUST_SET, 
					null, 
					new Object[]{ModelBasedSimulator.this.defaultRandomEffects, mean, subjectList}, 
					ModelBasedSimulator.this);
			fireModelBasedSimulatorEvent(event);
			
		}
		
		
		protected boolean doBlupsExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
			HierarchicalLevel level = subject.getHierarchicalLevel();
			return subjectIndex.containsKey(level.getName()) && subjectIndex.get(level.getName()).containsKey(subject.getSubjectId());
		}

		protected GaussianEstimate getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
			if (doBlupsExistForThisSubject(subject)) {
				List<Integer> rowIndices = subjectIndex.get(subject.getHierarchicalLevel().getName()).get(subject.getSubjectId());
				return new GaussianEstimate(getMean().getSubMatrix(rowIndices, DefaultZeroIndex), getVariance().getSubMatrix(rowIndices, rowIndices));
			} else {
				return null;
			}
		}
		
		protected void simulateBlups(MonteCarloSimulationCompliantObject subject) {
			Matrix simulatedDeviate = getRandomDeviate();
			ModelBasedSimulator.this.simulatedParameters.put(subject.getMonteCarloRealizationId(), simulatedDeviate.getSubMatrix(0, firstBlupIndex - 1, 0, 0));
			Matrix parametersForThisRealization = simulatedParameters.get(subject.getMonteCarloRealizationId());
			fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.PARAMETERS_DEVIATE_JUST_GENERATED, 
					null, 
					new Object[]{subject.getMonteCarloRealizationId(), parametersForThisRealization.getDeepClone()}, 
					ModelBasedSimulator.this));
			List<Integer> columnIndex = new ArrayList<Integer>();
			for (String levelName : subjectIndex.keySet()) {
				if (!ModelBasedSimulator.this.simulatedRandomEffects.containsKey(levelName)) {
					ModelBasedSimulator.this.simulatedRandomEffects.put(levelName, new HashMap<String, Matrix>());
				}
				Map<String, Matrix> innerMap = ModelBasedSimulator.this.simulatedRandomEffects.get(levelName);
				Map<String, List<Integer>> subjectList = subjectIndex.get(levelName);
				for (String subjectId : subjectList.keySet()) {
					Matrix randomDeviates = simulatedDeviate.getSubMatrix(subjectList.get(subjectId), columnIndex);
					innerMap.put(subjectId, randomDeviates);
					Estimate<? extends StandardGaussianDistribution> defaultRandomEffect = ModelBasedSimulator.this.defaultRandomEffects.get(levelName);
					ModelBasedSimulatorEvent event = new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.RANDOM_EFFECT_DEVIATE_JUST_GENERATED, 
							null, 
							new Object[]{defaultRandomEffect, randomDeviates.getDeepClone(), levelName, subjectId}, 
							ModelBasedSimulator.this);
					fireModelBasedSimulatorEvent(event);
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
			copyList.addAll(trueParameterIndices);
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
		
	public static enum ErrorTermGroup {
		Default
	}
	
	
	protected final CopyOnWriteArrayList<ModelBasedSimulatorListener> listeners;
	
	protected boolean areBlupsEstimated;

	// set by the constructor
	protected boolean isRandomEffectsVariabilityEnabled;
	protected boolean isParametersVariabilityEnabled;
	protected boolean isResidualVariabilityEnabled;
		
	protected Matrix oXVector;

	private ParameterEstimates parameterEstimates;
	private final Map<Integer, Matrix> simulatedParameters;		// refers to the realization id only
	
	private final Map<String, Estimate<? extends StandardGaussianDistribution>> defaultRandomEffects;
	private final Map<String, Map<String, Matrix>> simulatedRandomEffects;	// refers to the subject + realization ids

	private final Map<Enum<?>, GaussianErrorTermEstimate> defaultResidualError;
	private final Map<String, GaussianErrorTermList> simulatedResidualError;		// refers to the subject + realization ids
	
	protected boolean rememberRandomDeviates = true; 		// default value
	
	protected Random random = new Random();
	
	private final Map<String, IntervalNestedInPlotDefinition> intervalLists;
	
	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isParametersVariabilityEnabled a boolean that enables the variability at the parameter level
	 * @param isRandomEffectsVariabilityEnabled a boolean that enables the variability at the random effect level
	 * @param isResidualVariabilityEnabled a boolean that enables the variability at the tree level
	 */
	protected ModelBasedSimulator(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		this.isParametersVariabilityEnabled = isParametersVariabilityEnabled;
		this.isRandomEffectsVariabilityEnabled = isRandomEffectsVariabilityEnabled;
		this.isResidualVariabilityEnabled = isResidualVariabilityEnabled;
		
		defaultRandomEffects = new HashMap<String, Estimate<? extends StandardGaussianDistribution>>();
				
		simulatedParameters = new HashMap<Integer, Matrix>();
		simulatedRandomEffects = new HashMap<String, Map<String, Matrix>>();

		simulatedResidualError = new HashMap<String, GaussianErrorTermList>();
		intervalLists = new HashMap<String, IntervalNestedInPlotDefinition>();
		defaultResidualError = new HashMap<Enum<?>, GaussianErrorTermEstimate>();
		
		listeners = new CopyOnWriteArrayList<ModelBasedSimulatorListener>();
	}
	
	/**
	 * This method reads all the parameters in .csv files and stores the estimates into members defaultBeta, defaultResidualError,
	 * and defaultRandomEffects.
	 */
	protected abstract void init();
	
	protected Map<String, Estimate<? extends StandardGaussianDistribution>> getDefaultRandomEffect() {
		return defaultRandomEffects;
	}

	protected void setParameterEstimates(GaussianEstimate gaussianEstimate) {
		this.parameterEstimates = new ParameterEstimates(gaussianEstimate);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_BETA_JUST_SET, null, parameterEstimates, this));
	}
	
	protected ParameterEstimates getParameterEstimates() {return parameterEstimates;}
	
	protected void setDefaultRandomEffects(HierarchicalLevel level, Estimate<? extends StandardGaussianDistribution> newEstimate) {
		Estimate<? extends StandardGaussianDistribution> formerEstimate = defaultRandomEffects.get(level.getName());
		defaultRandomEffects.put(level.getName(), newEstimate);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_RANDOM_EFFECT_AT_THIS_LEVEL_JUST_SET, null, new Object[]{level, formerEstimate, newEstimate}, this));
	}
	
	protected Estimate<? extends StandardGaussianDistribution> getDefaultRandomEffects(HierarchicalLevel level) {return defaultRandomEffects.get(level.getName());}
	
	protected void setDefaultResidualError(Enum<?> enumVar, GaussianErrorTermEstimate estimate) {
		defaultResidualError.put(enumVar, estimate);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_RESIDUAL_ERROR_JUST_SET, null, new Object[]{enumVar, estimate}, this));
	}
	
	protected GaussianErrorTermEstimate getDefaultResidualError(Enum<?> enumVar) {
		return defaultResidualError.get(enumVar);
	}
	
	
	/**
	 * This method generates a stand-specific vector of model parameters using matrix Omega.
	 * @param subject a MonteCarloSimulationCompliantObject object
	 */
	private void setSpecificParametersDeviateForThisRealization(MonteCarloSimulationCompliantObject subject) {
		getParameterEstimates().simulateBlups(subject);
	}

	/**
	 * This method checks if the interval definition is available for the stand at that date. If it is, it returns the
	 * instance. Otherwise, it creates a new interval definition.
	 * @param stand A MonteCarloSimulationCompliantObject that designates the stand
	 * @param date an Integer
	 * @return an IntervalDefinition instance
	 */
	protected synchronized IntervalNestedInPlotDefinition getIntervalNestedInPlotDefinition(MonteCarloSimulationCompliantObject stand, int date) {
		String subjectID = IntervalNestedInPlotDefinition.getSubjectID(stand, date);
		String intervalID = getSubjectPlusMonteCarloSpecificId(subjectID, stand.getMonteCarloRealizationId());
		IntervalNestedInPlotDefinition intDef = intervalLists.get(intervalID);
		if (intDef == null) {
			intDef = new IntervalNestedInPlotDefinition(stand, date);
			intervalLists.put(getSubjectPlusMonteCarloSpecificId(intDef), intDef);
		}
		return intDef;
	}
	
	/**
	 * This method calls the setSpecificParametersDeviateForThisRealization method if the parameter variability is enabled and returns 
	 * a realization-specific simulated vector of model parameters. Otherwise it returns a default vector (beta). Note that the simulated
	 * parameters are related to the Monte Carlo realization. For instance, all subject in a given Monte Carlo realization will have the
	 * same simulation parameters. 
	 * @param subject a subject that implements the MonteCarloSimulationCompliantObject interface
	 * @return a vector of parameters
	 */
	protected final synchronized Matrix getParametersForThisRealization(MonteCarloSimulationCompliantObject subject) {
		if (isParametersVariabilityEnabled) {
			if (!rememberRandomDeviates) {
				simulatedParameters.clear();
			}
			if (!simulatedParameters.containsKey(subject.getMonteCarloRealizationId())) {		// the simulated parameters remain constant within the same Monte Carlo iteration
				setSpecificParametersDeviateForThisRealization(subject);
			}
			return simulatedParameters.get(subject.getMonteCarloRealizationId());
		} else {
			return getParameterEstimates().getFixedEffectsPart();
		}
	}

	
	/**
	 * This method generates a subject-specific random effects vector using matrix G.
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 */
	private void setSpecificRandomEffectsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		
		Matrix randomDeviates;
		Estimate<? extends StandardGaussianDistribution> originalRandomEffects;
		if (getParameterEstimates().doBlupsExistForThisSubject(subject)) {
			getParameterEstimates().simulateBlups(subject);
		} else {
			randomDeviates = simulateDeviatesForRandomEffectsOfThisSubject(subject, defaultRandomEffects.get(subjectLevel.getName()));
			originalRandomEffects = getDefaultRandomEffects(subjectLevel);
			ModelBasedSimulatorEvent event = new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.RANDOM_EFFECT_DEVIATE_JUST_GENERATED, 
					null, 
					new Object[]{originalRandomEffects, randomDeviates.getDeepClone(), subjectLevel.getName(), subject.getSubjectId()},
					this);
			fireModelBasedSimulatorEvent(event);
		}
	}
	
	/**
	 * This method simulates random deviates from an estimate and stores them in the simulatedRandomEffects
	 * member.
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 * @param randomEffectsEstimate the estimate from which the random deviates are generated
	 * @return the random deviates as a Matrix instance
	 */
	protected synchronized Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<?> randomEffectsEstimate) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		Map<String, Matrix> randomEffectsMap = simulatedRandomEffects.get(subjectLevel.getName());
		if (randomEffectsMap == null) {
			randomEffectsMap = new HashMap<String, Matrix>();
			simulatedRandomEffects.put(subjectLevel.getName(), randomEffectsMap);
		}
		Matrix randomDeviates = randomEffectsEstimate.getRandomDeviate();
		randomEffectsMap.put(getSubjectPlusMonteCarloSpecificId(subject), randomDeviates);
		return randomDeviates;
	}
	
	protected final String getSubjectPlusMonteCarloSpecificId(MonteCarloSimulationCompliantObject object) {
		return getSubjectPlusMonteCarloSpecificId(object.getSubjectId(), object.getMonteCarloRealizationId());
	}

	private static String getSubjectPlusMonteCarloSpecificId(String subjectID, int monteCarloRealizationID) {
		return subjectID + monteCarloRealizationID;
	}
	
	/**
	 * This method calls the setSpecificPlotRandomEffectsForThisStand method if the random effects variability is enabled and returns 
	 * a stand-specific simulated vector of random effects. Otherwise it returns a default vector (all elements set to 0).
	 * @param subject a MonteCarloSimulationCompliantObject object
	 * @return a Matrix object
	 */
	protected final synchronized Matrix getRandomEffectsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		if (isRandomEffectsVariabilityEnabled) {
			if (!rememberRandomDeviates) {
				simulatedRandomEffects.clear();
			}
			if (!doRandomDeviatesExistForThisSubject(subject)) {
				setSpecificRandomEffectsForThisSubject(subject);
			}
			return simulatedRandomEffects.get(subjectLevel.getName()).get(getSubjectPlusMonteCarloSpecificId(subject));
		} else {
			GaussianEstimate blups = getParameterEstimates().getBlupsForThisSubject(subject);
			if (blups != null) {
				return blups.getMean();
			} else {
				return defaultRandomEffects.get(subjectLevel.getName()).getMean();
			}
		} 
	}
	
	protected final boolean doRandomDeviatesExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		return simulatedRandomEffects.get(subjectLevel.getName()) != null && simulatedRandomEffects.get(subjectLevel.getName()).containsKey(getSubjectPlusMonteCarloSpecificId(subject)); 
	}
	
	/**
	 * This method returns the residual error or the vector of residual errors associated with the subjectId.
	 * If the subject parameter is entered as null, the method assumes there is no need to store the simulated
	 * error terms in the simulatedResidualError map. This feature is useful if the residual error terms are 
	 * identically and independently distributed.
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 * @param group an Enum that defines the group in case of different error term specifications
	 * @return a Matrix instance
	 */
	protected synchronized final Matrix getResidualErrorForThisSubject(MonteCarloSimulationCompliantObject subject, Enum<?> group) {
		if (isResidualVariabilityEnabled) {				// running in Monte Carlo mode
			if (!rememberRandomDeviates) {
				simulatedResidualError.clear();
			}
			if (subject!= null && subject instanceof IndexableErrorTerm && defaultResidualError.get(group).getDistribution().isStructured()) {
				IndexableErrorTerm indexable = (IndexableErrorTerm) subject;
				GaussianErrorTermList list = getGaussianErrorTerms(subject);
				if (!list.getDistanceIndex().contains(indexable.getErrorTermIndex())) {
					list.add(new GaussianErrorTerm(indexable));
				}
				Matrix randomDeviate = defaultResidualError.get(group).getRandomDeviate(list);
				fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.RESIDUAL_ERROR_DEVIATE_JUST_GENERATED, null, new Object[]{subject, group, randomDeviate.getDeepClone()}, this));
				return randomDeviate; 
			} else {
				Matrix randomDeviate = defaultResidualError.get(group).getRandomDeviate();
				fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.RESIDUAL_ERROR_DEVIATE_JUST_GENERATED, null, new Object[]{subject, group, randomDeviate.getDeepClone()}, this));
				return randomDeviate;
			}
		} else {
			return defaultResidualError.get(group).getMean();
		}
	}
	
	protected synchronized final GaussianErrorTermList getGaussianErrorTerms(MonteCarloSimulationCompliantObject subject) {
		if (!doesThisSubjectHaveResidualErrorTerm(subject)) {		// the simulated parameters remain constant within the same Monte Carlo iteration
			simulatedResidualError.put(getSubjectPlusMonteCarloSpecificId(subject), new GaussianErrorTermList());
		}
		GaussianErrorTermList list =  simulatedResidualError.get(getSubjectPlusMonteCarloSpecificId(subject));
		return list;
	}
	
	protected final boolean doesThisSubjectHaveResidualErrorTerm(MonteCarloSimulationCompliantObject subject) {
		return simulatedResidualError.containsKey(getSubjectPlusMonteCarloSpecificId(subject));
	}
	
	protected final boolean doBlupsExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return getParameterEstimates().doBlupsExistForThisSubject(subject);
	}
	
	/**
	 * This method returns the residual error under the assumption of iid and that the error is unique for all groups. 
	 * See the getResidualErrorForThisSubject method.
	 * @return a Matrix instance
	 */
	protected final Matrix getResidualError() {
		return getResidualErrorForThisSubject(null, ErrorTermGroup.Default);
	}

	protected void fireModelBasedSimulatorEvent(ModelBasedSimulatorEvent event) {
		for (ModelBasedSimulatorListener listener : listeners) {
			listener.modelBasedSimulatorDidThis(event);
		}
	}
	
	protected void registerBlups(Matrix mean, Matrix variance, Matrix covariance, List<MonteCarloSimulationCompliantObject> subjectList) {
		getParameterEstimates().registerBlups(mean, variance, covariance, subjectList);
	}
	
	/**
	 * This method adds the listener instance to the list of listeners.
	 * @param listener a ModelBasedSimulatorListener listener
	 */
	public void addModelBasedSimulatorListener(ModelBasedSimulatorListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * This method removes the listener instance from the list of listeners.
	 * @param listener a ModelBasedSimulatorListener listener
	 */
	public void removeModelBasedSimulatorListener(ModelBasedSimulatorListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * This method enables the recording of the random deviates. By default, this option is set to true.
	 * It can be desirable to set this option to false when running large stochastic simulations.
	 * @param rememberRandomDeviates a boolean
	 */
	public void setRememberRandomDeviates(boolean rememberRandomDeviates) {
		this.rememberRandomDeviates = rememberRandomDeviates;
	}

	/**
	 * This method returns the blups for the subject or nothing if there is no blups for this subject
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 * @return a GaussianEstimate instance or null
	 */
	protected GaussianEstimate getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return getParameterEstimates().getBlupsForThisSubject(subject);
	}

	
//	/**
//	 * This method resets all the map instances that contain the simulated random effects, 
//	 * the residuals and parameter estimates.
//	 */
//	public synchronized void clear() {
//		defaultRandomEffects.clear();
//		simulatedParameters.clear();
//		simulatedRandomEffects.clear();
//		simulatedResidualError.clear();
//		blupsLibrary.clear();
//	}
	
}
