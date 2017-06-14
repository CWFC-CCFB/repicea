/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.thinners.melothinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.standlevel.LandOwnershipProvider;
import repicea.simulation.covariateproviders.standlevel.LandOwnershipProvider.LandOwnership;
import repicea.simulation.covariateproviders.standlevel.SlopeMRNFClassProvider.SlopeMRNFClass;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MeloThinnerPredictor extends REpiceaLogisticPredictor<MeloThinnerPlot, Object> {


	class EmbeddedFunction extends AbstractMathematicalFunction {
		@Override
		public Double getValue() {
			double conditionalSurvival = getParameterValue(0) * getVariableValue(0);
			double u = getParameterValue(1) * getVariableValue(1);
			return Math.pow(conditionalSurvival, Math.exp(u));
		}

		@Override
		public Matrix getGradient() {return null;}

		@Override
		public Matrix getHessian() {return null;}
	}
	
	private static final List<Integer> ParametersToIntegrate = new ArrayList<Integer>();
	static {
		ParametersToIntegrate.add(1);
	}
	
	private boolean quadratureEnabled = true;
	
	private Map<SlopeMRNFClass, Matrix> slopeClassDummy;
	private Map<String, Matrix> dynamicTypeDummy;
	private final GaussHermiteQuadrature ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
	private final EmbeddedFunction embeddedFunction;
	
	public MeloThinnerPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
		embeddedFunction = new EmbeddedFunction();
		embeddedFunction.setVariableValue(0, 1);
		embeddedFunction.setVariableValue(1, 1);
		embeddedFunction.setParameterValue(0, 0);
		embeddedFunction.setParameterValue(1, 0);
		cruiseLineMap = new HashMap<String, CruiseLine>();
	}

	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_HarvestBeta.csv";
			String omegaFilename = path + "0_HarvestOmega.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix randomEffectVariance = defaultBetaMean.getSubMatrix(11, 11, 0, 0);
			defaultBetaMean = defaultBetaMean.getSubMatrix(0, 10, 0, 0);
			
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			defaultBetaVariance = defaultBetaVariance.getSubMatrix(0, 10, 0, 10);
			Matrix meanRandomEffect = new Matrix(1,1);
			setDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE, new GaussianEstimate(meanRandomEffect, randomEffectVariance));
			GaussianEstimate estimate = new SASParameterEstimates(defaultBetaMean, defaultBetaVariance);
			setParameterEstimates(estimate); 
			oXVector = new Matrix(1, estimate.getMean().m_iRows);
			
		} catch (Exception e) {
			System.out.println("MeloThinnerPredictor.init() : Unable to initialize the mortality module!");
		}

	}

	@Override
	public synchronized double predictEventProbability(MeloThinnerPlot stand, Object tree, Object... parms) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		double proportionalPart = getProportionalPart(stand, beta);
		double[] aac;
		if (parms[0] instanceof double[]) {
			aac = (double[]) parms[0];
		} else {
			int year0 = (Integer) parms[0];
			int year1 = (Integer) parms[1];
			LandOwnership ownership;
			if (stand instanceof LandOwnershipProvider) {
				ownership = ((LandOwnershipProvider) stand).getLandOwnership();
			} else {
				ownership = LandOwnership.Public;
			}
			aac = MeloThinnerAACProvider.getInstance().getAACValues(stand.getQuebecForestRegion(),
					ownership, 
					year0,
					year1);
		}
		double baseline = getBaseline(beta, aac);

		double conditionalSurvival = Math.exp(-proportionalPart * baseline);
		embeddedFunction.setParameterValue(0, conditionalSurvival);
		
		double survival;
		if (isRandomEffectsVariabilityEnabled) {
			String cruiseLineID = stand.getCruiseLineID();
			if (cruiseLineID == null) {
				cruiseLineID = stand.getSubjectId();
			}
			CruiseLine cruiseLine = getCruiseLineForThisSubject(cruiseLineID, stand);
			Matrix cruiseLineRandomEffect = getRandomEffectsForThisSubject(cruiseLine);
			double u = cruiseLineRandomEffect.m_afData[0][0];
			embeddedFunction.setParameterValue(1, u);
			survival = embeddedFunction.getValue();
		} else {
			if (quadratureEnabled) {
				Matrix lowerCholeskyTriangle = getDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE).getVariance().getLowerCholTriangle();
				survival = ghq.getIntegralApproximation(embeddedFunction, ParametersToIntegrate, lowerCholeskyTriangle);
			} else {
				embeddedFunction.setParameterValue(1, 0);
				survival = embeddedFunction.getValue();
			}
		}
		double harvestProb = 1 - survival;
		return harvestProb;
	}

	private double getBaseline(Matrix beta, double[] aac) {
		
		double gamma0 = beta.m_afData[9][0];
		double gamma1 = beta.m_afData[10][0];
		
		double baselineResult = 0;
		for (double v : aac) {
			baselineResult += Math.exp(gamma0 + gamma1 * v);
		}
		
		return baselineResult;
	}

	private double getProportionalPart(MeloThinnerPlot stand, Matrix beta) {
		int index = 0;
		oXVector.m_afData[0][index] = Math.log(stand.getBasalAreaM2Ha());
		index++;
		
		oXVector.m_afData[0][index] = stand.getNumberOfStemsHa();
		index++;
		
		Matrix slopeClassDummy = getDummySlopeClass(stand.getSlopeClass());
		oXVector.setSubMatrix(slopeClassDummy, 0, index);
		index += slopeClassDummy.m_iCols;
		
		Matrix dynamicTypeDummy = getDynamicTypeDummy(stand.getEcologicalType());
		oXVector.setSubMatrix(dynamicTypeDummy, 0, index);
		index += dynamicTypeDummy.m_iCols;
		
		Matrix xBeta = oXVector.multiply(beta);
		return Math.exp(xBeta.m_afData[0][0]);
	}
	
	
	private Matrix getDummySlopeClass(SlopeMRNFClass slopeClass) {
		if (slopeClassDummy == null) {
			slopeClassDummy = new HashMap<SlopeMRNFClass, Matrix>();
			Matrix dummy;
			for (SlopeMRNFClass sc : SlopeMRNFClass.values()) {
				dummy = new Matrix(1,5);
				if (sc.ordinal() > 0) {
					dummy.m_afData[0][sc.ordinal() - 1] = 1d;
				}
				slopeClassDummy.put(sc, dummy);
			}
		}
		return slopeClassDummy.get(slopeClass);
	}
	
	private Matrix getDynamicTypeDummy(String ecologicalType) {
		if (dynamicTypeDummy == null) {
			dynamicTypeDummy = new HashMap<String, Matrix>();
			Matrix dummy = new Matrix(1,2);
			dummy.m_afData[0][0] = 1d;
			dynamicTypeDummy.put("F", dummy);
			
			dummy = new Matrix(1,2);
			dummy.m_afData[0][1] = 1d;
			dynamicTypeDummy.put("M", dummy);
			
			dummy = new Matrix(1,2);
			dynamicTypeDummy.put("R", dummy);
		}
		return dynamicTypeDummy.get(ecologicalType.substring(0, 1));
	}

	/*
	 * For test purpuse. Not to be disabled.
	 */
	void setGaussianQuadrature(boolean quadEnabled) {
		this.quadratureEnabled = quadEnabled;
	}
	
	
//	public static void main(String[] args) {
//		new MeloThinnerPredictor(false);
//	}
}
