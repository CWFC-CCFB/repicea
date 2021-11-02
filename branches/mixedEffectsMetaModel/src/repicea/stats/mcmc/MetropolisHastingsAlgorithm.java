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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaLogManager;

/**
 * An implementation of the MCMC Metropolis-Hastings algorithm.
 * @author Mathieu Fortin - September 2021
 */
public class MetropolisHastingsAlgorithm implements Runnable {
		
	private String loggerName;
	private String loggerPrefix;
	
	protected final MetropolisHastingsParameters simParms;
	protected final MetropolisHastingsPriorHandler priors;
	protected final MetropolisHastingsCompatibleModel model;
	private Matrix parameters;
	private Matrix parmsVarCov;
	
	protected double lnProbY;
	protected List<MetropolisHastingsSample> finalMetropolisHastingsSampleSelection;
	private boolean converged;
	protected int indexCorrelationParameter;

	public MetropolisHastingsAlgorithm(MetropolisHastingsCompatibleModel model) {
		simParms = new MetropolisHastingsParameters();
		priors = new MetropolisHastingsPriorHandler();
		this.model = model; 
	}

	private String getLoggerName() {
		if (loggerName == null) {
			loggerName = getClass().getName();
		}
		return loggerName;
	}
	
	private MetropolisHastingsSample findFirstSetOfParameters(int desiredSize, boolean isForIntegral) {
		long startTime = System.currentTimeMillis();
		double llk = Double.NEGATIVE_INFINITY;
		List<MetropolisHastingsSample> myFirstList = new ArrayList<MetropolisHastingsSample>();
		while (myFirstList.size() < desiredSize) {
			Matrix parms = priors.getRandomRealization();
			llk = isForIntegral ? model.getLogLikelihood(parms) : model.getLogLikelihood(parms) + Math.log(priors.getProbabilityDensity(parms)); // if isForIntegral then there is no need for the density of the parameters since the random realizations account for the distribution of the prior 
			if (llk > Double.NEGATIVE_INFINITY) {
				myFirstList.add(new MetropolisHastingsSample(parms, llk));
				if (myFirstList.size()%1000 == 0) {
					REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Initial sample list has " + myFirstList.size() + " sets.");
				}
			}
		}
 		Collections.sort(myFirstList);
		MetropolisHastingsSample startingParms = myFirstList.get(myFirstList.size() - 1);
		REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Time to find a first set of plausible parameters = " + (System.currentTimeMillis() - startTime) + " ms");
		REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "LLK = " + startingParms.llk + " - Parameters = " + startingParms.parms);
		return startingParms;
	}

	private String getLogMessagePrefix() {
		return loggerPrefix;
	}

	public MetropolisHastingsParameters getSimulationParameters() {
		return simParms;
	}

	public Matrix getFinalParameterEstimates() {
		return parameters;
	}
	
	public Matrix getParameterCovarianceMatrix() {
		return parmsVarCov;
	}
	
	public boolean hasConverged() {
		return converged;
	}
	
	
	/**
	 * Implement the Metropolis-Hastings algorithm.
	 * @param nbRealizations number of samples in the chain before removing burn in and selected one sample every x samples
	 * @param nbBurnIn number of samples to discard at the beginning of the chain
	 * @param nbInternalIter maximum number of realizations to find the next acceptable sample of the chain
	 * @param metropolisHastingsSample A list of MetaModelMetropolisHastingsSample instance that represents the chain
	 * @param gaussDist the sampling distribution
	 * @return a boolean
	 */
	private boolean generateMetropolisSample(List<MetropolisHastingsSample> metropolisHastingsSample, GaussianDistribution gaussDist) {
		long startTime = System.currentTimeMillis();
		Matrix newParms = null;
		double llk = 0d;
		boolean completed = true;
		int trials = 0;
		int successes = 0;
		double acceptanceRatio; 
		for (int i = 0; i < simParms.nbRealizations - 1; i++) { // Metropolis-Hasting  -1 : the starting parameters are considered as the first realization
			gaussDist.setMean(metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).parms);
			if (i > 0 && i < simParms.nbBurnIn && i%1000 == 0) {
				acceptanceRatio = ((double) successes) / trials;
				REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "After " + i + " realizations, the acceptance rate is " + acceptanceRatio);
				if (acceptanceRatio > 0.35) {	// we aim at having an acceptance rate slightly larger than 0.3 because it will decrease as the chain reaches its steady state
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(1.2*1.2));
				} else if (acceptanceRatio < 0.28) {
					gaussDist.setVariance(gaussDist.getVariance().scalarMultiply(0.8*0.8));
				}
				successes = 0;
				trials = 0;
			}
			if (i%10000 == 0 && i > simParms.nbBurnIn) {
				acceptanceRatio = ((double) successes) / trials;
				REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Processing realization " + i + " / " + simParms.nbRealizations + "; " + acceptanceRatio);
			}
			boolean accepted = false;
			int innerIter = 0;
			
			while (!accepted && innerIter < simParms.nbInternalIter) {
				newParms = gaussDist.getRandomRealization();
				double parmsPriorDensity = priors.getProbabilityDensity(newParms);
				if (parmsPriorDensity > 0d) {
					llk = model.getLogLikelihood(newParms) + Math.log(parmsPriorDensity);
					double ratio = Math.exp(llk - metropolisHastingsSample.get(metropolisHastingsSample.size() - 1).llk);
					accepted = StatisticalUtility.getRandom().nextDouble() < ratio;
					trials++;
					if (accepted) {
						successes++;
					}
				}
				innerIter++;
			}
			if (innerIter >= simParms.nbInternalIter && !accepted) {
				REpiceaLogManager.logMessage(getLoggerName(), Level.SEVERE,  getLogMessagePrefix(), "Stopping after " + i + " realization");
				completed = false;
				break;
			} else {
				metropolisHastingsSample.add(new MetropolisHastingsSample(newParms, llk));  // new set of parameters is recorded
				if (metropolisHastingsSample.size()%100 == 0) {
					REpiceaLogManager.logMessage(getLoggerName(), Level.FINEST, getLogMessagePrefix(), metropolisHastingsSample.get(metropolisHastingsSample.size() - 1));
				}
			}
		}
		
		if (completed) {
			acceptanceRatio = ((double) successes) / trials;
			REpiceaLogManager.logMessage(getLoggerName(), Level.INFO, getLogMessagePrefix(), "Time to obtain " + metropolisHastingsSample.size() + " samples = " + (System.currentTimeMillis() - startTime) + " ms");
			REpiceaLogManager.logMessage(getLoggerName(), Level.INFO, getLogMessagePrefix(), "Acceptance ratio = " + acceptanceRatio);
		} 
		return completed;
	}

	private void resetSuccessAndTrialMaps(GaussianDistribution dist, 
			Map<Integer, Integer> trials, 
			Map<Integer, Integer> successes) {
		trials.clear();
		successes.clear();
		for (int i = 0; i < dist.getMean().m_iRows; i++) {
			trials.put(i, 0);
			successes.put(i, 0);
		}
	}
	
	private Matrix computeSuccessRates(Map<Integer, Integer> trials, Map<Integer, Integer> successes) {
		Matrix ratios = new Matrix(trials.size(), 1);
		for (int i = 0; i < trials.size(); i++) {
			double ratio = ((double) successes.get(i)) / trials.get(i);
			ratios.setValueAt(i, 0, ratio);
		}
		return ratios;
	}
	
	/**
	 * Implement Gibbs sampling in a preliminary stage to balance the variance of the sampler.
	 * @param firstSample the MetaModelMetropolisHastingsSample instance that was found through random sampling
	 * @param sampler the sampling distribution
	 * @return a boolean
	 */
	private boolean balanceVariance(MetropolisHastingsSample firstSample, GaussianDistribution sampler) {
		long startTime = System.currentTimeMillis();
		List<MetropolisHastingsSample> initSample = new ArrayList<MetropolisHastingsSample>();
		initSample.add(firstSample);
		Matrix newParms = null;
		double llk = 0d;
		boolean completed = true;
		Matrix acceptanceRatios; 
		Map<Integer, Integer> trialMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> successMap = new HashMap<Integer, Integer>();
		resetSuccessAndTrialMaps(sampler, trialMap, successMap);
		double targetAcceptance = 0.5; // MF2021-11-01 This number does not matter much in absolute value. It just makes sure that the acceptance rate is balanced across the parameters.
		for (int i = 0; i < simParms.nbBurnIn - 1; i++) { // Metropolis-Hasting  -1 : the starting parameters are considered as the first realization
			Matrix originalParms = initSample.get(initSample.size() - 1).parms.getDeepClone();
			sampler.setMean(originalParms);
			if (i > 0 && i < simParms.nbBurnIn && i%1000 == 0) {
				acceptanceRatios = this.computeSuccessRates(trialMap, successMap);
				REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "After " + i + " realizations, the acceptance rates are " + acceptanceRatios);
				for (int j = 0; j < acceptanceRatios.m_iRows; j++) {
					double currentRatio = acceptanceRatios.getValueAt(j, 0);
					if (currentRatio > targetAcceptance + .05) {	// then we must increase the CoefVar
						Matrix variance = sampler.getVariance();
						variance.setValueAt(j, j, variance.getValueAt(j, j) * 1.2 * 1.2);
					} else if (currentRatio < targetAcceptance - .05) {
						Matrix variance = sampler.getVariance();
						variance.setValueAt(j, j, variance.getValueAt(j, j) * 0.8 * 0.8);
					}
				}
				resetSuccessAndTrialMaps(sampler, trialMap, successMap);
			}
			boolean accepted = false;
			int innerIter = 0;

			int j = 0;
			while (j < originalParms.m_iRows && innerIter < simParms.nbInternalIter) {
				double originalValue = originalParms.getValueAt(j, 0);
				double newValue = getNewParms(sampler, j);
				originalParms.setValueAt(j, 0, newValue);
				double parmsPriorDensity = priors.getProbabilityDensity(originalParms);
				if (parmsPriorDensity > 0d) {
					llk = model.getLogLikelihood(originalParms) + Math.log(parmsPriorDensity);
					double ratio = Math.exp(llk - initSample.get(initSample.size() - 1).llk);
					accepted = StatisticalUtility.getRandom().nextDouble() < ratio;
					trialMap.put(j, trialMap.get(j) + 1);
					if (accepted) {
						successMap.put(j, successMap.get(j) + 1);
						j++;
						accepted = false;
						innerIter = 0;
					} else {
						originalParms.setValueAt(j, 0, originalValue);	// we put the old value back into the vector of parameters
					}
				} else {
					originalParms.setValueAt(j, 0, originalValue);	// we put the old value back into the vector of parameters
				}
				innerIter++;
			}
			newParms = originalParms;
			if (innerIter >= simParms.nbInternalIter && !accepted) {
				REpiceaLogManager.logMessage(getLoggerName(), Level.SEVERE,  getLogMessagePrefix(), "Stopping after " + i + " realization");
				completed = false;
				break;
			} else {
				initSample.add(new MetropolisHastingsSample(newParms, llk));  // new set of parameters is recorded
				if (initSample.size()%100 == 0) {
					REpiceaLogManager.logMessage(getLoggerName(), Level.FINEST, getLogMessagePrefix(), initSample.get(initSample.size() - 1));
				}
			}
		}
		
		if (completed) {
			acceptanceRatios = computeSuccessRates(trialMap, successMap);
			REpiceaLogManager.logMessage(getLoggerName(), Level.INFO, getLogMessagePrefix(), "Time to balance the variance of the sample: " + (System.currentTimeMillis() - startTime) + " ms");
			REpiceaLogManager.logMessage(getLoggerName(), Level.INFO, getLogMessagePrefix(), "Acceptance ratio = " + acceptanceRatios);
		} 
		return completed;
	}

	private double getNewParms(GaussianDistribution dist, int i) {
		double variance = dist.getVariance().getValueAt(i, i);
		double mean = dist.getMean().getValueAt(i, 0);
		double newValue = mean + StatisticalUtility.getRandom().nextGaussian() * Math.sqrt(variance);
		return newValue;
	}
	
	private List<MetropolisHastingsSample> retrieveFinalSample(List<MetropolisHastingsSample> metropolisHastingsSample) {
		List<MetropolisHastingsSample> finalMetropolisHastingsGibbsSample = new ArrayList<MetropolisHastingsSample>();
		REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Discarding " + simParms.nbBurnIn + " samples as burn in.");
		for (int i = simParms.nbBurnIn; i < metropolisHastingsSample.size(); i+= simParms.oneEach) {
			finalMetropolisHastingsGibbsSample.add(metropolisHastingsSample.get(i));
		}
		REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Selecting one every " + simParms.oneEach + " samples as final selection.");
		return finalMetropolisHastingsGibbsSample;
	}
	
	public void fitModel() {
		double coefVar = 0.01;
		try {
			GaussianDistribution samplingDist = model.getStartingParmEst(coefVar);
			List<MetropolisHastingsSample> mhSample = new ArrayList<MetropolisHastingsSample>();
			MetropolisHastingsSample firstSet = findFirstSetOfParameters(simParms.nbInitialGrid, false);	// false: not for integration
			mhSample.add(firstSet); // first valid sample
			boolean completed = balanceVariance(firstSet, samplingDist);
			if (completed) {
				completed = generateMetropolisSample(mhSample, samplingDist);
				if (completed) {
					finalMetropolisHastingsSampleSelection = retrieveFinalSample(mhSample);
					MonteCarloEstimate mcmcEstimate = new MonteCarloEstimate();
					for (MetropolisHastingsSample sample : finalMetropolisHastingsSampleSelection) {
						mcmcEstimate.addRealization(sample.parms);
					}

					parameters = mcmcEstimate.getMean();
					parmsVarCov = mcmcEstimate.getVariance();
					List<MetropolisHastingsSample> tempSample = new ArrayList<MetropolisHastingsSample>();
					tempSample.addAll(finalMetropolisHastingsSampleSelection);
					Collections.sort(tempSample);
					lnProbY = getLnProbY(tempSample.get(tempSample.size() - 1).parms, finalMetropolisHastingsSampleSelection, samplingDist);
					REpiceaLogManager.logMessage(getLoggerName(), Level.FINE, getLogMessagePrefix(), "Final sample had " + finalMetropolisHastingsSampleSelection.size() + " sets of parameters.");
					converged = true;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			converged = false;
		} 
	}
	

	private double getLnProbY(Matrix point, 
			List<MetropolisHastingsSample> posteriorSamples, 
			GaussianDistribution samplingDist) {
		double parmsPriorDensity = priors.getProbabilityDensity(point);
		double llkOfThisPoint = model.getLogLikelihood(point) + Math.log(parmsPriorDensity);
		double sumIntegrand = 0;
		double densityFromSamplingDist = 0;
		for (MetropolisHastingsSample s : posteriorSamples) {
			samplingDist.setMean(s.parms);
			double ratio = Math.exp(llkOfThisPoint - s.llk);
			if (ratio > 1d) {
				ratio = 1;
			}
			densityFromSamplingDist = samplingDist.getProbabilityDensity(point); 
			sumIntegrand += ratio * densityFromSamplingDist;
		}
		sumIntegrand /= posteriorSamples.size();
		
		samplingDist.setMean(point);
		double sumRatio = 0d;
		int nbRealizations = posteriorSamples.size();
		for (int j = 0; j < nbRealizations; j++) {
			Matrix newParms = samplingDist.getRandomRealization();
			parmsPriorDensity = priors.getProbabilityDensity(newParms);
			double ratio;
			if (parmsPriorDensity > 0d) {
				double llk = model.getLogLikelihood(newParms) + Math.log(parmsPriorDensity);
				ratio = Math.exp(llk - llkOfThisPoint);
				if (ratio > 1d) {
					ratio = 1d;
				}
			} else {
				ratio = 0d;
			}
			sumRatio += ratio;
		}
		sumRatio /= nbRealizations;
		double pi_theta_y = sumIntegrand / sumRatio;
		double log_m_hat = llkOfThisPoint - Math.log(pi_theta_y);
		return log_m_hat;
	}

	@Override
	public void run() {
		fitModel();
	}

	

}
