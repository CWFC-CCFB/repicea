/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
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
			return HierarchicalLevel.IntervalNestedInPlot;
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
	
	/**
	 * This enum variable determines the hierarchical level of random effects. 
	 * @author Mathieu Fortin - January 2012
	 */
	public static enum HierarchicalLevel {
		Plot, 
		Tree, 
		IntervalNestedInPlot}

	public static enum ErrorTermGroup {
		Default
	}
	
	// set by the constructor
	protected boolean isRandomEffectsVariabilityEnabled;
	protected boolean isParametersVariabilityEnabled;
	protected boolean isResidualVariabilityEnabled;
	

	protected Matrix oXVector;

	protected GaussianEstimate defaultBeta;
	private final Map<Integer, Matrix> simulatedParameters;		// refers to the realization id only
	
	protected final Map<HierarchicalLevel, GaussianEstimate> defaultRandomEffects;
	protected final Map<HierarchicalLevel, Map<Integer, Estimate<?>>> blupsLibrary;	// refers to the subject id only - this map contains the blups and their variances whenever these can be estimated
	private final Map<HierarchicalLevel, Map<Long, Matrix>> simulatedRandomEffects;	// refers to the subject + realization ids

	protected final Map<Enum<?>, GaussianErrorTermEstimate> defaultResidualError;
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
		
		defaultRandomEffects = new HashMap<HierarchicalLevel, GaussianEstimate>();
				
		simulatedParameters = new HashMap<Integer, Matrix>();
		simulatedRandomEffects = new HashMap<HierarchicalLevel, Map<Long, Matrix>>();
		blupsLibrary = new HashMap<HierarchicalLevel, Map<Integer, Estimate<?>>>();
		simulatedResidualError = new HashMap<Long, GaussianErrorTermList>();
		intervalLists = new HashMap<Long, IntervalNestedInPlotDefinition>();
		defaultResidualError = new HashMap<Enum<?>, GaussianErrorTermEstimate>();
	}
	
	/**
	 * This method reads all the parameters in .csv files and stores the estimates into members defaultBeta, defaultResidualError,
	 * and defaultRandomEffects.
	 */
	protected abstract void init();
	
	
	/**
	 * This method generates a stand-specific vector of model parameters using matrix Omega.
	 * @param subject a MonteCarloSimulationCompliantObject object
	 */
	private void setSpecificParametersDeviateForThisRealization(MonteCarloSimulationCompliantObject subject) {
		Matrix parametersForThisRealization = defaultBeta.getRandomDeviate();
		simulatedParameters.put(subject.getMonteCarloRealizationId(), parametersForThisRealization);
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
		
		Map<Long, Matrix> randomEffectsMap = simulatedRandomEffects.get(subjectLevel);
		if (randomEffectsMap == null) {
			randomEffectsMap = new HashMap<Long, Matrix>();
			simulatedRandomEffects.put(subjectLevel, randomEffectsMap);
		}
		
		randomEffectsMap.put(getSubjectPlusMonteCarloSpecificId(subject), estimatedBlups.getRandomDeviate());
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
			if (simulatedRandomEffects.get(subjectLevel) == null ||	!simulatedRandomEffects.get(subjectLevel).containsKey(getSubjectPlusMonteCarloSpecificId(subject))) {		// the null condition is necessary otherwise the second condition could throw an exception
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
				return defaultResidualError.get(group).getRandomDeviate(list);
			} else {
				return defaultResidualError.get(group).getRandomDeviate();
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

	
//	/**
//	 * This method generates a residual errors for this subject.
//	 * @param subject a MonteCarloSimulationCompliantObject object
//	 */
//	private synchronized void setSpecificResidualErrorForThisSubject(MonteCarloSimulationCompliantObject subject) {
//		simulatedResidualError.put(getSubjectPlusMonteCarloSpecificId(subject), new GaussianErrorTermList());
////		Matrix residualErrorForThisSubject = defaultResidualError.getRandomDeviate();
////		simulatedResidualError.put(getSubjectPlusMonteCarloSpecificId(subject), residualErrorForThisSubject);
//	}

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

