/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.covariateproviders.treelevel.ABCDQualityProvider.ABCDQuality;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class WBirchProdVolPredictor extends ModelBasedSimulator {
	
	private final static Matrix LinearVector = new Matrix(5,1);
	static {
		LinearVector.m_afData[0][0] = 0d;
		LinearVector.m_afData[1][0] = 1d;
		LinearVector.m_afData[2][0] = -1d;
		LinearVector.m_afData[3][0] = -1d;
		LinearVector.m_afData[4][0] = -1d;
	}

	public static enum Version {
		/**
		 * For A,B,C qualities with dbh > 29 and h20Pred >= 5
		 */
		Full, 
		/**
		 * For A and B qualities whose dbh <= 29 or h20Pred < 5
		 */
		FullTruncated,
		/**
		 * For C quality whose dbh <= 29 or h20Pred < 5 and D quality
		 */
		DClass, 
		/**
		 * For no quality trees
		 */
		NCClass}
	
	private double sigma2Res;
	private Matrix weightExponentCoefficients;
	private Matrix corrMatrix;

	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled a boolean instance
	 */
	protected WBirchProdVolPredictor(boolean isParametersVariabilityEnabled) {		// the residual variability is switch to off at the moment
		super(isParametersVariabilityEnabled, false, false);
		init();
	}

	
	
	
	
	private void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_beta.csv";
			String omegaFilename = path + "0_omega.csv";
			String rMatrixFilename = path + "0_rMatrix.csv";
			String varParmsFilename = path + "0_varParams.csv";

			Matrix beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix omega = ParameterLoader.loadMatrixFromFile(omegaFilename);
			corrMatrix = ParameterLoader.loadMatrixFromFile(rMatrixFilename);
			Matrix varParms = ParameterLoader.loadVectorFromFile(varParmsFilename).get();
			sigma2Res = varParms.m_afData[varParms.m_iRows - 1][0];
			weightExponentCoefficients = varParms.getSubMatrix(0, varParms.m_iRows - 2, 0, 0); 

			defaultBeta = new GaussianEstimate(beta, omega);
		} catch (Exception e) {
			System.out.println("Unable to load parameters!");
		}
	}

	protected Matrix getVMatrixForThisTree(WBirchProdVolTree tree) {
		double dbhCm = tree.getDbhCm();
		Matrix weightMat = weightExponentCoefficients.powMatrix(dbhCm).matrixDiagonal();  
		Matrix vMat = weightMat.multiply(corrMatrix).multiply(weightMat).scalarMultiply(sigma2Res);
		
		Matrix outputMat = new Matrix(6,6);
		outputMat.setSubMatrix(vMat, 0, 0);
		Matrix linearCombination = LinearVector.transpose().multiply(vMat);
		outputMat.setSubMatrix(linearCombination, 5, 0);
		outputMat.setSubMatrix(linearCombination.transpose(), 0, 5);
		outputMat.setSubMatrix(linearCombination.multiply(LinearVector), 5, 5);
		
		return outputMat;
	}
	
	private double getH20Prediction(WBirchProdVolStand stand, WBirchProdVolTree tree, Matrix beta) {
		double dbhCm = tree.getDbhCm();
		double elevationM = stand.getElevationM();
		double isC = 0d;
		if (tree.getABCDQuality() == ABCDQuality.C) {
			isC = 1d;
		}
		double isD = 0d;
		if (tree.getABCDQuality() == ABCDQuality.D) {
			isD = 1d;
		}
		double isNC = 0d;
		if (tree.getABCDQuality() == null) {
			isNC = 1d;
		}
		double z1 = beta.m_afData[4][0];
		double z1C = beta.m_afData[5][0];
		double z1D = beta.m_afData[6][0];
		double z1NC = beta.m_afData[7][0];
		double z2 = beta.m_afData[8][0];
		double z3 = beta.m_afData[9][0];
		double z4 = beta.m_afData[10][0];
		
		double b1 = z1 + z1C * isC + z1D * isD + z1NC * isNC + z4 * elevationM;
		return b1 * Math.pow((1 - Math.exp(z2 * (dbhCm - 15d))), z3) * dbhCm;
	}

	
	private double getMerPrediction(WBirchProdVolStand stand, WBirchProdVolTree tree, Matrix beta, double h20Pred) {
		double dbhCm = tree.getDbhCm();
		
		double isAB = 0d;
		if (tree.getABCDQuality() == ABCDQuality.A || tree.getABCDQuality() == ABCDQuality.B) {
			isAB = 1d;
		}
		
		double a0 = beta.m_afData[11][0];
		double a1 = beta.m_afData[12][0];
		double a2 = beta.m_afData[13][0];
		double a3 = beta.m_afData[14][0];
		return a0 + a1 * Math.pow(dbhCm - 9d, a2) * h20Pred + a3 * isAB;
	}
	
	private double getPulpPrediction(WBirchProdVolStand stand, WBirchProdVolTree tree, Matrix beta, double h20Pred, double merVol) {
		double dbhCm = tree.getDbhCm();
		double isC = 0d;
		if (tree.getABCDQuality() == ABCDQuality.C) {
			isC = 1d;
		}
		double isD = 0d;
		if (tree.getABCDQuality() == ABCDQuality.D) {
			isD = 1d;
		}
		double isNC = 0d;
		if (tree.getABCDQuality() == null) {
			isNC = 1d;
		}
		double b1 = beta.m_afData[0][0];
		double b1C = beta.m_afData[1][0];
		double b1D = beta.m_afData[2][0];
		double b1NC = beta.m_afData[3][0];
		double b2 = beta.m_afData[15][0];
		
		return merVol / (1 + Math.exp(b1 + b1C * isC + b1D * isD + b1NC * isNC - b2 * h20Pred / dbhCm));
	}
	
	private double getSawlogPrediction(WBirchProdVolStand stand, WBirchProdVolTree tree, Matrix beta, double merVol, double pulpVol) {
		double dbhCm = tree.getDbhCm();
		double c1 = beta.m_afData[16][0];
		double c2 = beta.m_afData[17][0];
		
		return (merVol - pulpVol) / (1 + Math.exp(c1 - c2 * dbhCm));
	}
	
	private double getLowGradeVeneerPrediction(WBirchProdVolStand stand, WBirchProdVolTree tree, Matrix beta, double merVol, double pulpVol, double sawlogVol) {
		double dbhCm = tree.getDbhCm();
		double d2 = beta.m_afData[18][0];
		
		return (merVol - pulpVol - sawlogVol) / (1 + d2 * dbhCm);
	}

	/**
	 * This method returns the predictions for each log grade (dm3)
	 * @param stand a WBirchProdVolStand instance
	 * @param tree a WBirchProdVolTree instance
	 * @return a Matrix instance
	 */
	protected WBirchProdVolEstimate getLogGradeVolumePredictions(WBirchProdVolStand stand, WBirchProdVolTree tree) {
		Matrix modelParameters = getParametersForThisRealization(stand);
		double h20Pred = getH20Prediction(stand, tree, modelParameters);
		double merVol = getMerPrediction(stand, tree, modelParameters, h20Pred);
		double pulpVol = getPulpPrediction(stand, tree, modelParameters, h20Pred, merVol);
		
		Version version;
		if (tree.getClass().getName().equals("repicea.treelogger.wbirchprodvol.WBirchProdVolTreeImpl")) {
			try {
				Method methodH20 = tree.getClass().getDeclaredMethod("getH20Obs", new Class[]{});
				double h20Obs = (Double) methodH20.invoke(tree, new Object[]{});
				version = getVersion(tree, h20Obs);
			} catch (Exception e) {
				version = null;
				throw new InvalidParameterException("Unable to find the method getH20Obs in the WBirchProdVolTreeImpl instance");
			}
		} else  {
			version = getVersion(tree, h20Pred);
		}
		
		Matrix pred;
		if (version == Version.Full) {
			pred = new Matrix(6,1);
			double sawlogVol = getSawlogPrediction(stand, tree, modelParameters, merVol, pulpVol);
			double lowGradeVeneer = getLowGradeVeneerPrediction(stand, tree, modelParameters, merVol, pulpVol, sawlogVol);
			double veneer = merVol - pulpVol - sawlogVol - lowGradeVeneer;
			pred.m_afData[0][0] = h20Pred;
			pred.m_afData[1][0] = merVol;
			pred.m_afData[2][0] = pulpVol;
			pred.m_afData[3][0] = sawlogVol;
			pred.m_afData[4][0] = lowGradeVeneer;
			pred.m_afData[5][0] = veneer;
		} else {
			pred = new Matrix(4,1);
			pred.m_afData[0][0] = h20Pred;
			pred.m_afData[1][0] = merVol;
			if (version == Version.FullTruncated) {
				double sawlogVol = getSawlogPrediction(stand, tree, modelParameters, merVol, pulpVol);
				pred.m_afData[2][0] = pulpVol + (merVol - pulpVol - sawlogVol);
				pred.m_afData[3][0] = sawlogVol;
			} else {
				double lastProduct = merVol - pulpVol;
				pred.m_afData[2][0] = pulpVol;
				pred.m_afData[3][0] = lastProduct;
			}
		}
		
		WBirchProdVolEstimate estimate = new WBirchProdVolEstimate(version);
		estimate.setMean(pred);
		return estimate;
	}
	
	
	
	protected static Version getVersion(WBirchProdVolTree tree, double h20Pred) {
		if (tree.getABCDQuality() != null) {
			if (tree.getABCDQuality().ordinal() < 3) {
				if (h20Pred >= 5d && tree.getDbhCm() > 29d) {
					return Version.Full;
				} else {
					if (tree.getABCDQuality().ordinal() < 2) {
						return Version.FullTruncated;
					} else {
						return Version.DClass;
					}
				}
			} else {
				return Version.DClass;
			}
		} else {
			return Version.NCClass;
		}
		
	}
	
	
	
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		WBirchProdVolPredictor lll = new WBirchProdVolPredictor(false);
		int u = 0;
	}
}