package repicea.simulation.metamodel;

import java.util.ArrayList;
import java.util.List;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.integral.GaussHermiteQuadrature;

/**
 * The AbstractDataBlockWrapper is a likelihood function. The getValue() method 
 * returns this likelihood. The model also implements features to compute 
 * marginal likelihood (over the random effect) in cases of mixed-effects model.
 * @author Mathieu Fortin - September 2021
 */
@SuppressWarnings("serial")
abstract class AbstractDataBlockWrapper extends AbstractMathematicalFunction {
	
	final Matrix vecY;
	final Matrix timeSinceBeginning;
	final Matrix timeToOrigin;
	final Matrix ageYr;
	Matrix parameters;
	final String blockId;
	final GaussHermiteQuadrature ghq;
	final List<Integer> ghqIndices;
	final List<Integer> indices;
	
	AbstractDataBlockWrapper(String blockId, 
			List<Integer> indices, 
			HierarchicalStatisticalDataStructure structure, 
			Matrix overallVarCov) {
		this.blockId = blockId;
		this.indices = indices;
		Matrix matX = structure.getMatrixX().getSubMatrix(indices, null);
		this.vecY = structure.getVectorY().getSubMatrix(indices, null);
		this.timeSinceBeginning = matX.getSubMatrix(0, matX.m_iRows - 1, 1, 1);
		this.timeToOrigin = matX.getSubMatrix(0, matX.m_iRows - 1, 0, 0).scalarMultiply(-1);
		this.ageYr = matX.getSubMatrix(0, matX.m_iRows - 1, 0, 0).add(timeSinceBeginning);
		setParameterValue(0, 0d); // potential random effect
		this.ghq = new GaussHermiteQuadrature();
		this.ghqIndices = new ArrayList<Integer>();
		ghqIndices.add(0);
	}
	
	/**
	 * Ensure null variances are set to 0.0001
	 * @param deepClone 
	 * @return a Matrix
	 */
	final Matrix correctVarCov(Matrix deepClone) {
		for (int i = 0; i < deepClone.m_iRows; i++) {
			if (deepClone.getValueAt(i, i) <= 0) {
				deepClone.setValueAt(i, i, .0001);
			}
		}
		return deepClone;
	}

	@Override
	public final Double getValue() {
		double llk = getLogLikelihood();
		double prob = Math.exp(llk);
		return prob;
	}

	/**
	 * Return the log likelihood. For mixed-effects model this is the log likelihood conditional
	 * on the random effect. To be used in the Gauss-Hermite quadrature.
	 * @return a double
	 */
	abstract double getLogLikelihood();

	abstract double getMarginalLogLikelihood();
	
	abstract void updateCovMat(Matrix parameters);

	@Override
	public final Matrix getGradient() {return null;}

	@Override
	public final Matrix getHessian() {return null;}

}


