package repicea.simulation.metamodel;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.distributions.GaussianDistribution;

public class RichardsChapmanModelWithRandomEffectImplementation extends RichardsChapmanModelImplementation {

	/**
	 * The likelihood implementation for this model implementation.
	 * @author Mathieu Fortin - September 2021
	 */
	@SuppressWarnings("serial")
	class DataBlockWrapper extends AbstractDataBlockWrapper {
		
		final Matrix varCovFullCorr;
		final Matrix distances;
		Matrix invVarCov;
		double lnConstant;
		
		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
			Matrix varCovTmp = overallVarCov.getSubMatrix(indices, indices);
			Matrix stdDiag = correctVarCov(varCovTmp).diagonalVector().elementWisePower(0.5);
			this.varCovFullCorr = stdDiag.multiply(stdDiag.transpose());
			distances = new Matrix(varCovFullCorr.m_iRows, 1, 1, 1);
		}
		
		
		@Override
		double getMarginalLogLikelihood() {
			Matrix lowerCholeskyTriangle = RichardsChapmanModelWithRandomEffectImplementation.this.getVarianceRandomEffect().getLowerCholTriangle();
			double integratedLikelihood = ghq.getIntegralApproximation(this, ghqIndices, lowerCholeskyTriangle);
			return Math.log(integratedLikelihood);
		}
		
		void updateCovMat(Matrix parameters) {
			double rhoParm = getCorrelationParameter();	
			Matrix corrMat = StatisticalUtility.constructRMatrix(distances, 1d, rhoParm, TypeMatrixR.POWER);
			Matrix varCov = varCovFullCorr.elementWiseMultiply(corrMat);
			invVarCov = varCov.getInverseMatrix();
			double determinant = varCov.getDeterminant();
			int k = this.vecY.m_iRows;
			this.lnConstant = -.5 * k * Math.log(2 * Math.PI) - Math.log(determinant) * .5;
		}


		@Override
		double getLogLikelihood() {
			Matrix pred = generatePredictions(this, getParameterValue(0));
			Matrix residuals = vecY.subtract(pred);
			Matrix rVr = residuals.transpose().multiply(invVarCov).multiply(residuals);
			double rVrValue = rVr.getSumOfElements();
			if (rVrValue < 0) {
				throw new UnsupportedOperationException("The sum of squared errors is negative!");
			} else {
				double llk = - 0.5 * rVrValue + lnConstant; 
				return llk;
			}
		}

	}

	int indexRandomEffectVariance;
	int indexCorrelationParameter;
	
	public RichardsChapmanModelWithRandomEffectImplementation(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		super(structure, varCov);
	}

	
	Matrix getVarianceRandomEffect() {
		return parameters.getSubMatrix(indexRandomEffectVariance, indexRandomEffectVariance, 0, 0);
	}

	protected double getCorrelationParameter() {
		return parameters.getValueAt(indexCorrelationParameter, 0);
	}

	@Override
	AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new RichardsChapmanModelWithRandomEffectImplementation.DataBlockWrapper(k, indices, structure, varCov);
	}

	@Override
	void setParameters(Matrix parameters) {
		super.setParameters(parameters);
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			((DataBlockWrapper) dbw).updateCovMat(this.parameters);
		}
	}

	@Override
	double getLogLikelihood(Matrix parameters) {
		setParameters(parameters);
		double logLikelihood = 0d;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			double marginalLogLikelihoodForThisBlock = dbw.getMarginalLogLikelihood();
			logLikelihood += marginalLogLikelihoodForThisBlock;
		}
		return logLikelihood;
	}

	
	@Override
	GaussianDistribution getStartingParmEst(double coefVar) {
		Matrix parmEst = new Matrix(5,1);
		parmEst.setValueAt(0, 0, 100d);
		parmEst.setValueAt(1, 0, 0.02);
		parmEst.setValueAt(2, 0, 2d);
		parmEst.setValueAt(3, 0, 200d);
		parmEst.setValueAt(4, 0, .92);
		
		this.indexRandomEffectVariance = 3;
		this.indexCorrelationParameter = 4;
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound(0,400));
		bounds.add(new Bound(0.0001, 0.1));
		bounds.add(new Bound(1,6));
		bounds.add(new Bound(0,350));
		bounds.add(new Bound(.9,.99));

		return gd;
	}

	
}
