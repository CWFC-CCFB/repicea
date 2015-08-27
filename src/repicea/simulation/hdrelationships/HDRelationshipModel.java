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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;

/**
 * The HDRelationshipModel class is the basic class for all HD relationships based on linear mixed-effects modelling.
 * @author Mathieu Fortin - August 2015
 *
 * @param <Stand> a HDRelationshipStand-derived class
 * @param <Tree> a HDRelationshipTree-derived class
 */
@SuppressWarnings("serial")
public abstract class HDRelationshipModel<Stand extends HDRelationshipStand, Tree extends HDRelationshipTree> extends ModelBasedSimulator {

	protected static class RegressionElements {
		public Matrix Z_tree;
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

	protected final List<Integer> blupEstimationDone;

	protected HDRelationshipModel(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		blupEstimationDone = new ArrayList<Integer>();
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
			if (!blupEstimationDone.contains(stand.getSubjectId())) {
				predictHeightRandomEffects(stand);
				blupEstimationDone.add(stand.getSubjectId());
			}
			double observedHeight = tree.getHeightM();
			double predictedHeight; 
			RegressionElements regElement = fixedEffectsPrediction(stand, tree);
			predictedHeight = regElement.fixedPred;
			predictedHeight += blupImplementation(stand, regElement);

			if (observedHeight > 1.3) {			// means that height was already observed
				double variance = defaultResidualError.get(getErrorGroup(tree)).getVariance().m_afData[0][0];
				double dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
				GaussianErrorTerm errorTerm = new GaussianErrorTermForHeight(tree, dNormResidual, observedHeight - predictedHeight);
				setSpecificResiduals(tree, errorTerm);	// the residual is set in the simulatedResidualError member
				return -1d;
			} else {
				predictedHeight += residualImplementation(tree);
				if (predictedHeight < 1.3) {
					predictedHeight = 1.3;
				}
				return predictedHeight;
			}
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
		return regElement.Z_tree.multiply(randomEffects).m_afData[0][0];
	}
	

	
	/**
	 * This method records a normalized residuals into the simulatedResidualError member which is
	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
	 * @param tree a MonteCarloSimulationCompliantObject instance which stands for the tree
	 * @param errorTerm a GaussianErrorTerm instance
	 */
	protected final void setSpecificResiduals(Tree tree, GaussianErrorTerm errorTerm) {
		getGaussianErrorTerms(tree).add(errorTerm);
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
//			Matrix residuals = getResidualErrorForThisSubject(tree, tree.getHeightableTreeSpecies().getSpeciesType());
			Matrix residuals = getResidualErrorForThisSubject(tree, getErrorGroup(tree));
			int index = getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
			residualForThisPrediction = residuals.m_afData[index][0]; 
		} else {
			if (doesThisSubjectHaveResidualErrorTerm(tree)) {		// means that height was initially measured
				setSpecificResiduals(tree, new GaussianErrorTerm(tree, 0d));
				GaussianErrorTermList list = getGaussianErrorTerms(tree);
//				Matrix meanResiduals = defaultResidualError.get(tree.getHeightableTreeSpecies().getSpeciesType()).getMean(list);
				Matrix meanResiduals = defaultResidualError.get(getErrorGroup(tree)).getMean(list);
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
		boolean originalIsParameterVariabilityEnabled = isParametersVariabilityEnabled;
		isParametersVariabilityEnabled = false; // temporarily disabled for the prediction of the random effects
		
		Matrix matrixG = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance();
		
		Matrix blups;
		Matrix blupsVariance;

		RegressionElements regElement;
		
		// put all the trees for which the height is available in a Vector
		List<HDRelationshipTree> heightableTrees = new ArrayList<HDRelationshipTree>();
		Collection trees = getTreesFromStand(stand);
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
			int nbObs = heightableTrees.size();
			Matrix matZ = new Matrix(nbObs, matrixG.m_iRows);		// design matrix for random effects 
			Matrix matR = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
			Matrix vectRes = new Matrix(nbObs, 1);						// vector of residuals

			for (int i = 0; i < nbObs; i++) {
				Tree t = (Tree) heightableTrees.get(i);
				double height = t.getHeightM();
				
				regElement = fixedEffectsPrediction(stand, t);
				matZ.setSubMatrix(regElement.Z_tree, i, 0);
//				double variance = defaultResidualError.get(regElement.species.getSpeciesType()).getVariance().m_afData[0][0];
				double variance = defaultResidualError.get(getErrorGroup(t)).getVariance().m_afData[0][0];
				matR.m_afData[i][i] = variance;
				double residual = height - regElement.fixedPred;
				vectRes.m_afData[i][0] = residual;
			}
			Matrix matV = matZ.multiply(matrixG).multiply(matZ.transpose()).add(matR);	// variance - covariance matrix
			blups = matrixG.multiply(matZ.transpose()).multiply(matV.getInverseMatrix()).multiply(vectRes);							// blup_essHD is redefined according to observed values
			blupsVariance = matZ.transpose().multiply(matR.getInverseMatrix()).multiply(matZ).add(matrixG.getInverseMatrix()).getInverseMatrix();			// blup_essHDvar is redefined according to observed values
			Map<Integer, Estimate<?>> randomEffectsMap = blupsLibrary.get(HierarchicalLevel.Plot);
			if (randomEffectsMap == null) {
				randomEffectsMap = new HashMap<Integer, Estimate<?>>();
				blupsLibrary.put(HierarchicalLevel.Plot, randomEffectsMap);
			}
			randomEffectsMap.put(stand.getSubjectId(), new GaussianEstimate(blups, blupsVariance));
		}
		
		isParametersVariabilityEnabled = originalIsParameterVariabilityEnabled; // set the parameter variability to its original value;
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
	 * it is deterministic or stochastic. NOTE: This method should be synchronized!!!!
	 * @param stand a Stand instance
	 * @param t a Tree instance
	 */
	protected abstract RegressionElements fixedEffectsPrediction(Stand stand, Tree t);

}