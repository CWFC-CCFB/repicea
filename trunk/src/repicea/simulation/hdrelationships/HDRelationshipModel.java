/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.hdrelationships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
import repicea.stats.estimates.GaussianEstimate;

/**
 * The HDRelationshipModel class is the basic class for all HD relationships based on linear mixed-effects modelling.
 * @author Mathieu Fortin - August 2015
 *
 * @param <Stand> a HDRelationshipStand-derived class
 * @param <Tree> a HDRelationshipTree-derived class
 */
@SuppressWarnings("serial")
public abstract class HDRelationshipModel<Stand extends HDRelationshipStand, Tree extends HDRelationshipTree> extends REpiceaPredictor {

	protected static class RegressionElements {
		public Matrix vectorZ;
		public double fixedPred;
		public Enum<?> species;
		
		public RegressionElements() {}
	}

	protected static class GaussianErrorTermForHeight extends GaussianErrorTerm {
		public GaussianErrorTermForHeight(IndexableErrorTerm caller, double normalizedValue, double observedValue) {
			super(caller, normalizedValue);
			this.value = observedValue;
		}
	}

	/**
	 * Preferred constructor.
	 * @param isVariabilityEnabledEnabled enables the variability in the parameter estimates, the random effects and the
	 * residual errors at the same time
	 */
	protected HDRelationshipModel(boolean isVariabilityEnabledEnabled) {
		this(isVariabilityEnabledEnabled, isVariabilityEnabledEnabled, isVariabilityEnabledEnabled);
	}

	/**
	 * Second constructor for greater flexilibity
	 * @param isParameterVariabilityEnabled enables the variability in the parameter estimates
	 * @param isRandomEffectVariabilityEnabled enables the variability in the random effects
	 * @param isResidualErrorVariabilityEnabled enables the variability in the residual errors
	 */
	protected HDRelationshipModel(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualErrorVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualErrorVariabilityEnabled);
	}

	/**
	 * This method calculates the height for individual trees and also implements the Monte Carlo simulation automatically. In case of 
	 * exception, it also returns -1. If the predicted height is lower than 1.3, this method returns 1.3.
	 * @param stand a MonteCarloSimulationCompliantObject instance which stands for the stand or the plot
	 * @param tree a MonteCarloSimulationCompliantObject instance which stands for the tree
	 * @return the predicted height (m)
	 */
	public double predictHeight(Stand stand, Tree tree) {
		try {
			if (!hasSubjectBeenTestedForBlups(stand)) {
				predictHeightRandomEffects(stand);	// this method now deals with the blups and the residual error so that if observed height is greater than 1.3 m there is no need to avoid predicting the height
			}
			RegressionElements regElement = fixedEffectsPrediction(stand, tree, getParametersForThisRealization(stand));
			double predictedHeight = regElement.fixedPred;
			predictedHeight += blupImplementation(stand, regElement);
			predictedHeight += residualImplementation(tree);
			if (predictedHeight < 1.3) {
				predictedHeight = 1.3;
			}
			return predictedHeight;
		} catch (Exception e) {
			System.out.println("Error while estimating tree height for tree " + tree.toString());
			e.printStackTrace();
			return -1d;
		}
	}

	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand a Stand object
	 * @param regElement a RegressionElements object
	 * @return a simulated random effect (double)
	 */
	protected double blupImplementation(Stand stand, RegressionElements regElement) {
		Matrix randomEffects = getRandomEffectsForThisSubject(stand);
		return regElement.vectorZ.multiply(randomEffects).m_afData[0][0];
	}
	

	
	/**
	 * This method records a normalized residuals into the simulatedResidualError member which is
	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
	 * @param tree a MonteCarloSimulationCompliantObject instance which stands for the tree
	 * @param errorTerm a GaussianErrorTerm instance
	 */
	protected final void setSpecificResiduals(Tree tree, GaussianErrorTerm errorTerm) {
		GaussianErrorTermList list = getGaussianErrorTerms(tree);
		if (!list.getDistanceIndex().contains(tree.getErrorTermIndex())) {		// we add the GaussianErrorTerm only if it is not already in the list
			list.add(errorTerm);
		}
	}
	
	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param tree a HDRelationshipTree instance
	 * @param regElement a RegressionElements instance
	 * @return a simulated residual (double)
	 */
	protected double residualImplementation(Tree tree) {
		double residualForThisPrediction = 0d; 
		if (isResidualVariabilityEnabled) {
			Matrix residuals = getResidualErrorForThisSubject(tree, getErrorGroup(tree));
			int index = getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
			residualForThisPrediction = residuals.m_afData[index][0]; 
		} else {
			if (doesThisSubjectHaveResidualErrorTerm(tree)) {		// means that height was initially measured
				setSpecificResiduals(tree, new GaussianErrorTerm(tree, 0d));
				GaussianErrorTermList list = getGaussianErrorTerms(tree);
				Matrix meanResiduals = getDefaultResidualError(getErrorGroup(tree)).getMean(list);
				residualForThisPrediction = meanResiduals.m_afData[meanResiduals.m_iRows - 1][0];
			} 
		}
		return residualForThisPrediction;
	}

	
	/**
	 * This method computes the best linear unbiased predictors of the random effects
	 * @param stand a HeightableStand instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected synchronized void predictHeightRandomEffects(Stand stand) {
		if (!hasSubjectBeenTestedForBlups(stand)) {
			Matrix matGbck = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance();

			RegressionElements regElement;

			List<HDRelationshipTree> heightableTrees = new ArrayList<HDRelationshipTree>(); // put all the trees for which the height is available in a List


			Matrix defaultBeta = getParameterEstimates().getMean();		// at this point the mean only contains the fixed effects
			Matrix omega = getParameterEstimates().getVariance();

			Collection trees = getTreesFromStand(stand);
			heightableTrees.clear();
			if (trees != null && !trees.isEmpty()) {
				for (Object tree : trees) {
					if (tree instanceof HDRelationshipTree) {
						double height = ((HDRelationshipTree) tree).getHeightM();
						if (height > 1.3) {
							heightableTrees.add((HDRelationshipTree) tree);
						}

					}
				}
			}
			if (!heightableTrees.isEmpty()) {
				// matrices for the blup calculation
				int nbParameters = getParameterEstimates().getMean().m_iRows;
				int nbObs = heightableTrees.size();
				Matrix matZ_i = new Matrix(nbObs, matGbck.m_iRows);		// design matrix for random effects 
				Matrix matR_i = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
				Matrix matX_i = new Matrix(nbObs, nbParameters);					// within-tree variance-covariance matrix  
				Matrix res_i = new Matrix(nbObs, 1);						// vector of residuals

				for (int i = 0; i < nbObs; i++) {
					Tree t = (Tree) heightableTrees.get(i);
					double height = t.getHeightM();
					regElement = fixedEffectsPrediction(stand, t, defaultBeta);
					matX_i.setSubMatrix(oXVector.getSubMatrix(0, 0, 0, nbParameters - 1), i, 0);
					matZ_i.setSubMatrix(regElement.vectorZ, i, 0);
					double variance = getDefaultResidualError(getErrorGroup(t)).getVariance().m_afData[0][0];
					matR_i.m_afData[i][i] = variance;
					double residual = height - regElement.fixedPred;
					res_i.m_afData[i][0] = residual;
				}
				Matrix matV_i = matZ_i.multiply(matGbck).multiply(matZ_i.transpose()).add(matR_i);
				Matrix invV_i = matV_i.getInverseMatrix();
				Matrix blups_i = matGbck.multiply(matZ_i.transpose()).multiply(invV_i).multiply(res_i);

				Matrix newMatG_i = null;

				if (isRandomEffectsVariabilityEnabled) {
					Matrix matP = invV_i.subtract(invV_i.multiply(matX_i).multiply(omega).multiply(matX_i.transpose()).multiply(invV_i));  
					newMatG_i = matGbck.subtract(matGbck.multiply(matZ_i.transpose()).multiply(matP).multiply(matZ_i).multiply(matGbck));
				}

				setBlupsForThisSubject(stand, new GaussianEstimate(blups_i, newMatG_i));
				
				for (HDRelationshipTree t : heightableTrees) {
					Tree tree = (Tree) t;
					double observedHeight = tree.getHeightM();
					double predictedHeight; 
					regElement = fixedEffectsPrediction(stand, tree, getParametersForThisRealization(stand));
					predictedHeight = regElement.fixedPred;
					predictedHeight += blupImplementation(stand, regElement);

					double variance = getDefaultResidualError(getErrorGroup(tree)).getVariance().m_afData[0][0];
					double dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
					GaussianErrorTerm errorTerm = new GaussianErrorTermForHeight(tree, dNormResidual, observedHeight - predictedHeight);
					setSpecificResiduals(tree, errorTerm);	// the residual is set in the simulatedResidualError member
				}
			} 
		}
		recordSubjectTestedForBlups(stand);
	}

	protected Enum<?> getErrorGroup(Tree tree) {
		Enum<?> errorGroup = tree.getHDRelationshipTreeErrorGroup();
		if (errorGroup == null) {
			return ErrorTermGroup.Default;
		} else {
			return errorGroup;
		}
	}
	
	/**
	 * This method selects the trees from which the blups must be calculated.
	 * @param stand a Stand instance
	 * @return return a Collection of Tree instances
	 */
	protected abstract Collection<Tree> getTreesFromStand(Stand stand);
	
	/**
	 * This method computes the fixed effect prediction and put the prediction, the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies in any cases no matter
	 * it is deterministic or stochastic. NOTE: This method must be synchronized!!!!
	 * @param stand a Stand instance
	 * @param t a Tree instance
	 * @param beta a Matrix that contains the parameters
	 */
	protected abstract RegressionElements fixedEffectsPrediction(Stand stand, Tree t, Matrix beta);

	
	
}
