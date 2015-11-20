/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.matapedia;

import repicea.math.Matrix;
import repicea.predictor.matapedia.MatapediaTree.MatapediaTreeSpecies;
import repicea.simulation.LogisticModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.LFParameter;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * The MatapediaMortalityPredictor class implements a mortality module for the
 * Matapedia simulator. The compatibility with this module is ensured through 
 * the MatapediaTree and MatapediaStand interfaces. The random effect implementation
 * includes a Gauss-Hermite quadrature to account for the interval random effect. 
 *
 * @author Mathieu Fortin - September 2012
 * 
 * @see <a href=http://www.nrcresearchpress.com/doi/pdf/10.1139/cjfr-2012-0268> 
 * Fortin, M. 2013. Population-averaged predictions with generalized linear mixed-effects models
 * in forestry: an estimator based on Gauss-Hermite quadrature. Canadian Journal of Forest Research
 * 43: 129-138. </a> 
 */
public final class MatapediaMortalityPredictor extends LogisticModelBasedSimulator<MatapediaStand, MatapediaTree>{

	private static final long serialVersionUID = 20120912L;

	private final static double offset5Years = Math.log(5d);		
	
	private final LinkFunction linkFunction;
	private final LinearStatisticalExpression eta;
	private final GaussHermiteQuadrature ghq;
	
	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isRandomEffectVariabilityEnabled true to enable the variability due to the random effect
	 * @param isResidualVariabilityEnabled true to enable the residual variability
	 */
	public MatapediaMortalityPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		init();
		oXVector = new Matrix(1, defaultBeta.getMean().m_iRows);
		linkFunction = new LinkFunction(Type.CLogLog);
		eta = new LinearStatisticalExpression();
		linkFunction.setParameterValue(LFParameter.Eta, eta);
		eta.setParameterValue(0, 0d);		// random parameter
		eta.setVariableValue(0, 1d);		// variable that multiplies the random parameter
		eta.setParameterValue(1, 1d);		// paramter that multiplies the xBeta
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);
	}

	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MortBeta.csv";
			String omegaFilename = path + "0_MortOmega.csv";
			String covParmsFilename = path + "0_MortCovParms.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			Matrix randomEffectVariance = ParameterLoader.loadVectorFromFile(covParmsFilename).get();
			
			Matrix meanRandomEffect = new Matrix(1,1);
			defaultRandomEffects.put(HierarchicalLevel.IntervalNestedInPlot, new GaussianEstimate(meanRandomEffect, randomEffectVariance));
			defaultBeta = new SASParameterEstimate(defaultBetaMean, defaultBetaVariance); 
			
		} catch (Exception e) {
			System.out.println("MatapediaMortalityPredictor.init() : Unable to initialize the mortality module!");
		}
	}
	
	/**
	 * This method predicts the probability of mortality in the upcoming 5 years. Note that this method needs
	 * to be synchronized as several threads may access the xVector at the same time otherwise
	 * @param stand a MatapediaStand instance
	 * @param tree a MatapediaTree instance
	 * @param parms some additional parameters
	 * @return the predicted probability of mortality
	 */
	@Override
	public synchronized double predictEventProbability(MatapediaStand stand, MatapediaTree tree, Object... parms) {
		
		double etaValue = fixedEffectsPrediction(stand, tree);
		eta.setVariableValue(1, etaValue);
		double prob;
		
		if (isRandomEffectsVariabilityEnabled) { 
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = getRandomEffectsForThisSubject(interval);
			eta.setParameterValue(0, randomEffects.m_afData[0][0]);
			prob = linkFunction.getValue();
		} else {
			eta.setParameterValue(0, 0d);
			prob = ghq.getOneDimensionIntegral(linkFunction, eta, 0, ((GaussianEstimate) defaultRandomEffects.get(HierarchicalLevel.IntervalNestedInPlot)).getDistribution().getStandardDeviation().m_afData[0][0]);
		}
		
		if (parms != null && parms.length > 0 && parms[0] instanceof Double) {
			double timeStep = (Double) parms[0];
			prob = 1 - Math.pow (1 - prob, timeStep / 5d);		// correction in case of 6-yr growth step
		}
		return prob;
	}

	
	private double fixedEffectsPrediction(MatapediaStand stand, MatapediaTree tree) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		
		int pointer = 0;
		
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		
		MatapediaTreeSpecies species = tree.getMatapediaTreeSpecies();
		Matrix dummySpeciesDbh0 = species.getDummy().scalarMultiply(tree.getDbhCm());
		oXVector.setSubMatrix(dummySpeciesDbh0, 0, pointer);
		pointer += species.getDummy().m_iCols;

		oXVector.m_afData[0][pointer] = tree.getSquaredDbhCm();
		pointer++;
		
		if (stand.isSBWDefoliated() && !stand.isGoingToBeSprayed()) {
			oXVector.setSubMatrix(dummySpeciesDbh0, 0, pointer);
		} 
		pointer += species.getDummy().m_iCols;
		
		double bal = tree.getBasalAreaLargerThanSubjectM2Ha();
		oXVector.setSubMatrix(species.getDummy().scalarMultiply(bal), 0, pointer);
		pointer += species.getDummy().m_iCols;
		
		oXVector.m_afData[0][pointer] = offset5Years;
		pointer++;
		
		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}

}
