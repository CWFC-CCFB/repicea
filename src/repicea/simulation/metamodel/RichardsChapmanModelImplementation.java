package repicea.simulation.metamodel;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.distributions.GaussianDistribution;

public class RichardsChapmanModelImplementation extends AbstractModelImplementation {

	@SuppressWarnings("serial")
	class DataBlockWrapper extends AbstractDataBlockWrapper {
		
		final Matrix invVarCov;
		final double lnConstant;
		
		DataBlockWrapper(String blockId, 
				List<Integer> indices, 
				HierarchicalStatisticalDataStructure structure, 
				Matrix overallVarCov) {
			super(blockId, indices, structure, overallVarCov);
			Matrix varCov = correctVarCov(overallVarCov.getSubMatrix(indices, indices));
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
		
		@Override
		double getMarginalLogLikelihood() {
			throw new UnsupportedOperationException("This model implementation " + getClass().getSimpleName() + " does not implement random effects!");
		}
		
	}

	public RichardsChapmanModelImplementation(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		super(structure, varCov);
	}
	
	@Override
	final Matrix generatePredictions(AbstractDataBlockWrapper dbw, double randomEffect) {
		Matrix mu = new Matrix(dbw.vecY.m_iRows, 1);
		for (int i = 0; i < mu.m_iRows; i++) {
			mu.setValueAt(i, 0, getPrediction(dbw.ageYr.getValueAt(i, 0), dbw.timeSinceBeginning.getValueAt(i, 0), randomEffect));
		}
		return mu;
	}
	
	@Override
	double getPrediction(double ageYr, double timeSinceBeginning, double r1) {
		double b1 = parameters.getValueAt(0, 0);
		double b2 = parameters.getValueAt(1, 0);
		double b3 = parameters.getValueAt(2, 0);
		double pred = (b1 + r1) * Math.pow(1 - Math.exp(-b2 * ageYr), b3);
		return pred;
	}


	@Override
	AbstractDataBlockWrapper createDataBlockWrapper(String k, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		return new RichardsChapmanModelImplementation.DataBlockWrapper(k, indices, structure, varCov);
	}
	
	@Override
	double getLogLikelihood(Matrix parameters) {
		setParameters(parameters);
		double logLikelihood = 0d;
		for (AbstractDataBlockWrapper dbw : dataBlockWrappers) {
			double logLikelihoodForThisBlock = dbw.getLogLikelihood();
			logLikelihood += logLikelihoodForThisBlock;
		}
		return logLikelihood;
	}

	
	@Override
	GaussianDistribution getStartingParmEst(double coefVar) {
		Matrix parmEst = new Matrix(4,1);
		parmEst.setValueAt(0, 0, 100d);
		parmEst.setValueAt(1, 0, 0.02);
		parmEst.setValueAt(2, 0, 2d);
		parmEst.setValueAt(3, 0, .92);
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound(0,400));
		bounds.add(new Bound(0.0001, 0.1));
		bounds.add(new Bound(1,6));
		bounds.add(new Bound(.90,.99));

		return gd;
	}

	
}
