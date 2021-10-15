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
	class DataBlockWrapper extends RichardsChapmanModelImplementation.DataBlockWrapper {
		
		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
		}
		
		
		@Override
		double getMarginalLogLikelihood() {
			Matrix lowerCholeskyTriangle = RichardsChapmanModelWithRandomEffectImplementation.this.getVarianceRandomEffect().getLowerCholTriangle();
			double integratedLikelihood = ghq.getIntegralApproximation(this, ghqIndices, lowerCholeskyTriangle);
			return Math.log(integratedLikelihood);
		}
		
	}

	int indexRandomEffectVariance;
	
	public RichardsChapmanModelWithRandomEffectImplementation(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		super(structure, varCov);
	}

	
	Matrix getVarianceRandomEffect() {
		return getParameters().getSubMatrix(indexRandomEffectVariance, indexRandomEffectVariance, 0, 0);
	}

	protected double getCorrelationParameter() {
		return getParameters().getValueAt(indexCorrelationParameter, 0);
	}

	@Override
	AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new DataBlockWrapper(k, indices, structure, varCov);
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
		
		fixedEffectsParameterIndices = new ArrayList<Integer>();
		fixedEffectsParameterIndices.add(0);
		fixedEffectsParameterIndices.add(1);
		fixedEffectsParameterIndices.add(2);
		
		indexRandomEffectVariance = 3;
		indexCorrelationParameter = 4;
		
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
