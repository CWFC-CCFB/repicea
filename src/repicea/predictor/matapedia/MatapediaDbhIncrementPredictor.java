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
import repicea.simulation.GrowthModel;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * The MatapediaDbhIncrementPredictor class implements a dbh increment module that was fitted using the
 * data of the Matapedia permanent plot network. 
 * @author Mathieu Fortin - September 2012
 */
public class MatapediaDbhIncrementPredictor extends ModelBasedSimulator implements GrowthModel<MatapediaStand, MatapediaTree>{

	private static final long serialVersionUID = 20120911L;

	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled true to enable the variability at the parameter level
	 * @param isRandomEffectsVariabilityEnabled true to enable the variability at the tree level
	 * @param isResidualVariabilityEnabled true to enable the variability at the measurement level
	 */
	public MatapediaDbhIncrementPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled,	boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		init();
		oXVector = new Matrix(1,15);
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_AccroissementBeta.csv";
			String omegaFilename = path + "0_AccroissementOmega.csv";
			String covparmsFilename = path + "0_AccroissementCovParms.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			
			setParameterEstimates(new SASParameterEstimate(defaultBetaMean, defaultBetaVariance)); 
			
			Matrix covParms =  ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			Matrix plotRandomEffectVariance = covParms.getSubMatrix(0, 0, 0, 0);
			GaussianEstimate defRandomEffect = new GaussianEstimate(new Matrix(plotRandomEffectVariance.m_iRows,1), plotRandomEffectVariance);
			setDefaultRandomEffects(HierarchicalLevel.TREE, defRandomEffect);

			Matrix residualErrorVariance = covParms.getSubMatrix(1, 1, 0, 0);
			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(residualErrorVariance));
		} catch (Exception e) {
			System.out.println("MatapediaDbhIncrementPredictor.init() : Unable to initialize the diameter increment module!");
		}
		
	}

	/**
	 * This method predicts the annual dbh increment for the upcoming 5 years.
	 * @param stand a MatapediaStand instance
	 * @param tree a MatapediaTree instance
	 * @return the annual dbh increment (mm)
	 */
	@Override
	public double predictGrowth(MatapediaStand stand, MatapediaTree tree, Object... parms) {
		double prediction = fixedEffectsPrediction(tree, stand);
		double dbh = tree.getDbhCm();
		double randomEffect = getRandomEffectsForThisSubject(tree).scalarMultiply(dbh).m_afData[0][0];
		double residualError = getResidualError().m_afData[0][0]; 
		prediction += randomEffect + residualError; 
		return prediction;
	}
	
	
	private synchronized double fixedEffectsPrediction(MatapediaTree tree, MatapediaStand stand) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		
		int pointer = 0;
		
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		
		MatapediaTreeSpecies species = tree.getMatapediaTreeSpecies();
		oXVector.setSubMatrix(species.getDummy(), 0, pointer);
		pointer += species.getDummy().m_iCols;

		double dbh = tree.getDbhCm();
		oXVector.m_afData[0][pointer] = dbh;
		pointer++;
		
		double dbh2 = tree.getSquaredDbhCm();
		oXVector.m_afData[0][pointer] = dbh2;
		pointer++;
		
		boolean isSBWComing = stand.isGoingToBeDefoliated();
		if (isSBWComing) {
			oXVector.setSubMatrix(species.getDummy(), 0, pointer);
		} 
		pointer += species.getDummy().m_iCols;

		double bal = tree.getBasalAreaLargerThanSubjectM2Ha();
		oXVector.setSubMatrix(species.getDummy().scalarMultiply(bal), 0, pointer);
		pointer += species.getDummy().m_iCols;

		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}

	
}
