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
public abstract class ModelBasedSimulator implements Serializable {

	/**
	 * The SASParameterEstimate class is customized for SAS outputs. The major difference
	 * is related to how the random deviates are calculated. Since SAS produces false estimates,
	 * such as 0 for reference class or 1 for offset, the getRandomDeviate() method makes sure
	 * the false estimate are not accounted for during the simulation of the random deviates.
	 * @author Mathieu Fortin - September 2012
	 */
	public static class SASParameterEstimate extends GaussianEstimate {
		
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructor.
		 * @param mean a vector that corresponds to the mean value
		 * @param variance a symmetric positive definite matrix 
		 */
		public SASParameterEstimate(Matrix mean, Matrix variance) {
			super(mean, variance);
		}
		
		
		@Override
		public Matrix getRandomDeviate() {
			Matrix lowerChol = getDistribution().getStandardDeviation();
			Matrix randomVector = StatisticalUtility.drawRandomVector(lowerChol.m_iRows, Distribution.Type.GAUSSIAN);
			Matrix oMat = lowerChol.multiply(randomVector);
			return StatisticalUtility.performSpecialAdd(getMean(), oMat);
		}
	}
	
	protected static class IntervalNestedInPlotDefinition implements MonteCarloSimulationCompliantObject {

		private final int monteCarloRealization;
		private int subjectID;
		
		protected IntervalNestedInPlotDefinition(MonteCarloSimulationCompliantObject stand, int date) {
			monteCarloRealization = stand.getMonteCarloRealizationId();
			subjectID = getSubjectID(stand, date);
		}
		
		
		@Override
		public int getSubjectId() {
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
		
		private static int getSubjectID(MonteCarloSimulationCompliantObject stand, int date) {
			return (int) (stand.hashCode() * 10000L + date);
		}
		
		
	}
	
		
	private static final long serialVersionUID = 20100902L;
	
	public static enum ErrorTermGroup {
		Default
	}
	
	
	protected final CopyOnWriteArrayList<ModelBasedSimulatorListener> listeners;
	
	// set by the constructor
	protected boolean isRandomEffectsVariabilityEnabled;
	protected boolean isParametersVariabilityEnabled;
	protected boolean isResidualVariabilityEnabled;
		
	protected Matrix oXVector;

	private GaussianEstimate defaultBeta;
	private final Map<Integer, Matrix> simulatedParameters;		// refers to the realization id only
	
	private final Map<HierarchicalLevel, GaussianEstimate> defaultRandomEffects;
	private final Map<HierarchicalLevel, Map<Integer, Estimate<? extends StandardGaussianDistribution>>> blupsLibrary;	// refers to the subject id only - this map contains the blups and their variances whenever these can be estimated
	protected final List<Integer> blupEstimationDone;
	private final Map<HierarchicalLevel, Map<Long, Matrix>> simulatedRandomEffects;	// refers to the subject + realization ids

	private final Map<Enum<?>, GaussianErrorTermEstimate> defaultResidualError;
	private final Map<Long, GaussianErrorTermList> simulatedResidualError;		// refers to the subject + realization ids
	
	protected boolean rememberRandomDeviates = true; 		// default value
	
	protected Random random = new Random();
	
	private final Map<Long, IntervalNestedInPlotDefinition> intervalLists;
	
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
		
		blupEstimationDone = new ArrayList<Integer>();

		defaultRandomEffects = new HashMap<HierarchicalLevel, GaussianEstimate>();
				
		simulatedParameters = new HashMap<Integer, Matrix>();
		simulatedRandomEffects = new HashMap<HierarchicalLevel, Map<Long, Matrix>>();
		blupsLibrary = new HashMap<HierarchicalLevel, Map<Integer, Estimate<? extends StandardGaussianDistribution>>>();
		simulatedResidualError = new HashMap<Long, GaussianErrorTermList>();
		intervalLists = new HashMap<Long, IntervalNestedInPlotDefinition>();
		defaultResidualError = new HashMap<Enum<?>, GaussianErrorTermEstimate>();
		
		listeners = new CopyOnWriteArrayList<ModelBasedSimulatorListener>();
	}
	
	/**
	 * This method reads all the parameters in .csv files and stores the estimates into members defaultBeta, defaultResidualError,
	 * and defaultRandomEffects.
	 */
	protected abstract void init();
	
	protected void setDefaultBeta(GaussianEstimate defaultBeta) {
		this.defaultBeta = defaultBeta;
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_BETA_JUST_SET, null, defaultBeta, this));
	}
	
	protected GaussianEstimate getDefaultBeta() {return defaultBeta;}
	
	protected void setDefaultRandomEffects(HierarchicalLevel level, GaussianEstimate estimate) {
		defaultRandomEffects.put(level, estimate);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_RANDOM_EFFECT_AT_THIS_LEVEL_JUST_SET, null, new Object[]{level, estimate}, this));
	}
	
	protected GaussianEstimate getDefaultRandomEffects(HierarchicalLevel level) {return defaultRandomEffects.get(level);}
	
	protected void setDefaultResidualError(Enum<?> enumVar, GaussianErrorTermEstimate estimate) {
		defaultResidualError.put(enumVar, estimate);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.DEFAULT_RESIDUAL_ERROR_JUST_SET, null, new Object[]{enumVar, estimate}, this));
	}
	
	protected GaussianErrorTermEstimate getDefaultResidualError(Enum<?> enumVar) {
		return defaultResidualError.get(enumVar);
	}
	
	protected Map<Integer, Estimate<? extends StandardGaussianDistribution>> getBlupsAtThisLevel(HierarchicalLevel level) {
		return blupsLibrary.get(level);
	}
	
	protected Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel level = subject.getHierarchicalLevel();
		Map<Integer, Estimate<? extends StandardGaussianDistribution>> innerMap = getBlupsAtThisLevel(level);
		if (innerMap != null) {
			return innerMap.get(subject.getSubjectId());
		}
		return null;
	}

	@Deprecated
	protected void setBlupsForThisSubject(HierarchicalLevel level, int subjectID, Estimate<? extends StandardGaussianDistribution> blups) {
		if (!blupsLibrary.containsKey(level)) {
			blupsLibrary.put(level, new HashMap<Integer, Estimate<? extends StandardGaussianDistribution>>());
		}
		Map<Integer, Estimate<? extends StandardGaussianDistribution>> internalMap = getBlupsAtThisLevel(level);
		internalMap.put(subjectID, blups);
	}

	protected void setBlupsForThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<? extends StandardGaussianDistribution> blups) {
		setBlupsForThisSubject(subject.getHierarchicalLevel(), subject.getSubjectId(), blups);
		GaussianEstimate originalRandomEffects = getDefaultRandomEffects(subject.getHierarchicalLevel());
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.BLUPS_AT_THIS_LEVEL_JUST_SET, null, new Object[]{subject, originalRandomEffects, blups}, this));
	}
	
	/**
	 * This method generates a stand-specific vector of model parameters using matrix Omega.
	 * @param subject a MonteCarloSimulationCompliantObject object
	 */
	private void setSpecificParametersDeviateForThisRealization(MonteCarloSimulationCompliantObject subject) {
		Matrix parametersForThisRealization = defaultBeta.getRandomDeviate();
		simulatedParameters.put(subject.getMonteCarloRealizationId(), parametersForThisRealization);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.PARAMETERS_DEVIATE_JUST_GENERATED, null, new Object[]{subject.getMonteCarloRealizationId(), parametersForThisRealization.getDeepClone()}, this));
	}

	/**
	 * This method checks if the interval definition is available for the stand at that date. If it is, it returns the
	 * instance. Otherwise, it creates a new interval definition.
	 * @param stand A MonteCarloSimulationCompliantObject that designates the stand
	 * @param date an Integer
	 * @return an IntervalDefinition instance
	 */
	protected synchronized IntervalNestedInPlotDefinition getIntervalNestedInPlotDefinition(MonteCarloSimulationCompliantObject stand, int date) {
		int subjectID = IntervalNestedInPlotDefinition.getSubjectID(stand, date);
		long intervalID = getSubjectPlusMonteCarloSpecificId(subjectID, stand.getMonteCarloRealizationId());
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
			return defaultBeta.getMean();
		}
	}

	
	/**
	 * This method generates a subject-specific random effects vector using matrix G.
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 */
	private void setSpecificRandomEffectsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		Estimate<?> randomEffectEstimate = null;
		if (blupsLibrary.get(subjectLevel) != null) {
			randomEffectEstimate = blupsLibrary.get(subjectLevel).get(subject.getSubjectId()); // get the reference Blups
 		}
		
		Estimate<?> estimatedBlups;
		
		if (randomEffectEstimate != null) {
			estimatedBlups = randomEffectEstimate;
		} else  {
			estimatedBlups = defaultRandomEffects.get(subjectLevel);
		}
		
//		Map<Long, Matrix> randomEffectsMap = simulatedRandomEffects.get(subjectLevel);
//		if (randomEffectsMap == null) {
//			randomEffectsMap = new HashMap<Long, Matrix>();
//			simulatedRandomEffects.put(subjectLevel, randomEffectsMap);
//		}
//		
//		Matrix randomDeviates = estimatedBlups.getRandomDeviate();
//		randomEffectsMap.put(getSubjectPlusMonteCarloSpecificId(subject), randomDeviates);
		Matrix randomDeviates = simulateDeviatesForRandomEffectsOfThisSubject(subject, estimatedBlups);
		GaussianEstimate originalRandomEffects = getDefaultRandomEffects(subjectLevel);
		fireModelBasedSimulatorEvent(new ModelBasedSimulatorEvent(ModelBasedSimulatorEventProperty.RANDOM_EFFECT_DEVIATE_JUST_GENERATED, null, new Object[]{subject, originalRandomEffects, randomDeviates.getDeepClone()}, this));
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
		Map<Long, Matrix> randomEffectsMap = simulatedRandomEffects.get(subjectLevel);
		if (randomEffectsMap == null) {
			randomEffectsMap = new HashMap<Long, Matrix>();
			simulatedRandomEffects.put(subjectLevel, randomEffectsMap);
		}
		Matrix randomDeviates = randomEffectsEstimate.getRandomDeviate();
		randomEffectsMap.put(getSubjectPlusMonteCarloSpecificId(subject), randomDeviates);
		return randomDeviates;
	}
	
	protected final long getSubjectPlusMonteCarloSpecificId(MonteCarloSimulationCompliantObject object) {
		return getSubjectPlusMonteCarloSpecificId(object.getSubjectId(), object.getMonteCarloRealizationId());
	}

	private static long getSubjectPlusMonteCarloSpecificId(int subjectID, int monteCarloRealizationID) {
		return subjectID * 1000000L + monteCarloRealizationID;
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
//			if (simulatedRandomEffects.get(subjectLevel) == null ||	!simulatedRandomEffects.get(subjectLevel).containsKey(getSubjectPlusMonteCarloSpecificId(subject))) {		// the null condition is necessary otherwise the second condition could throw an exception
			if (!doRandomDeviatesExistForThisSubject(subject)) {
				setSpecificRandomEffectsForThisSubject(subject);
			}
			return simulatedRandomEffects.get(subjectLevel).get(getSubjectPlusMonteCarloSpecificId(subject));
		} else {
			if (blupsLibrary.get(subjectLevel) != null && blupsLibrary.get(subjectLevel).containsKey(subject.getSubjectId())) {		// the first condition is necessary otherwise the second contidion could throw an exception
				return blupsLibrary.get(subjectLevel).get(subject.getSubjectId()).getMean();
			} else {
				return defaultRandomEffects.get(subjectLevel).getMean();
			}
		} 
	}
	
	protected final boolean doRandomDeviatesExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
		HierarchicalLevel subjectLevel = subject.getHierarchicalLevel();
		return simulatedRandomEffects.get(subjectLevel) != null && simulatedRandomEffects.get(subjectLevel).containsKey(getSubjectPlusMonteCarloSpecificId(subject)); 
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
	 * This method resets all the map instances that contain the simulated random effects, 
	 * the residuals and parameter estimates.
	 */
	public synchronized void clear() {
		defaultRandomEffects.clear();
		simulatedParameters.clear();
		simulatedRandomEffects.clear();
		simulatedResidualError.clear();
		blupsLibrary.clear();
	}
	
}

