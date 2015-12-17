package repicea.stats.integral;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.LogFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.optimizer.AbstractOptimizer.OptimizationException;
import repicea.math.optimizer.NewtonRaphsonOptimizer;

@SuppressWarnings("serial")
public class LaplaceApproximation  {

	private static class CustomizedLogWrapperFunction extends LogFunctionWrapper {

		private final double mPart;
		private final Matrix invG;
		private final Matrix originalParameterValues;
		private final List<Integer> parameterIndices;

		/**
		 * Constructor
		 * @param originalFunction the nested function
		 * @param gMatrix the variance-covariance matrix of the deviate
		 */
		private CustomizedLogWrapperFunction(AbstractMathematicalFunction originalFunction, List<Integer> parameterIndices, Matrix gMatrix) {
			super(originalFunction);
			if (!gMatrix.isSymmetric()) {
				throw new InvalidParameterException("Matrix G is supposed to be symmetric!");
			}
			//				this.gMatrix = gMatrix;
			this.invG = gMatrix.getInverseMatrix();
			double n = gMatrix.m_iRows;
			double gDeterminant = gMatrix.getDeterminant();
			mPart = - 0.5 * n * Math.log(2d * Math.PI) - 0.5 * Math.log(gDeterminant);
			this.parameterIndices = parameterIndices;
			originalParameterValues = getParametersFromNestedFunction();
		}

		@Override
		public Double getValue() {
			Matrix u = getParametersFromNestedFunction().subtract(originalParameterValues);
			return super.getValue() + mPart - 0.5 * u.transpose().multiply(invG).multiply(u).m_afData[0][0]; 
		}


		@Override
		public Matrix getGradient() {
			Matrix u = getParametersFromNestedFunction().subtract(originalParameterValues);
			return super.getGradient().subtract(u.transpose().multiply(invG));
		}


		@Override
		public Matrix getHessian() {
			return super.getHessian().subtract(invG);
		}

		private Matrix getParametersFromNestedFunction() {
			Matrix parameterValues = new Matrix(parameterIndices.size(), 1);
			for (int i = 0; i < parameterIndices.size(); i++) {
				parameterValues.m_afData[i][0] = getOriginalFunction().getParameterValue(parameterIndices.get(i));
			}
			return parameterValues;
		}

	}



	/**
	 * Constructor.
	 */
	public LaplaceApproximation() {
		super();
	}

	/**
	 * This method returns the value of a multi-dimension integral
	 * @param functionToEvaluate an EvaluableFunction instance that returns Double 
	 * @param parameterIndices the indices of the parameters over which the integration is made
	 * @param lowerCholeskyTriangle the lower triangle of the Cholesky factorization of the variance-covariance matrix
	 * @return the approximation of the integral
	 */
	public double getIntegralApproximation(AbstractMathematicalFunction functionToEvaluate,
			List<Integer> parameterIndices, 
			Matrix lowerCholeskyTriangle) {
		if (!lowerCholeskyTriangle.isSquare() || parameterIndices.size() != lowerCholeskyTriangle.m_iRows) {
			throw new InvalidParameterException("The indices are not compatible with the lower Cholesky triangle!");
		} else {
			for (Integer index : parameterIndices) {
				if (index < 0 || index >= functionToEvaluate.getNumberOfParameters()) {
					throw new InvalidParameterException("One index is either negative or it exceeds the number of parameters in the function!");
				}
			}
			Matrix matrixG = lowerCholeskyTriangle.multiply(lowerCholeskyTriangle.transpose());
			CustomizedLogWrapperFunction functionToBeOptimized = new CustomizedLogWrapperFunction(functionToEvaluate, parameterIndices, matrixG);
			NewtonRaphsonOptimizer nro = new NewtonRaphsonOptimizer();
			try {
				nro.optimize(functionToBeOptimized, parameterIndices);
			} catch (OptimizationException e) {
				e.printStackTrace();
			}
			Matrix newHessian = nro.getHessianAtMaximum();
			double fOptimal = functionToBeOptimized.getValue();
			Matrix varCov = newHessian.getInverseMatrix().scalarMultiply(-1d);
			double approximation = Math.sqrt(2d * Math.PI * varCov.getDeterminant()) * Math.exp(fOptimal); 
			return approximation;
		}
	}


//	public static void main(String[] args) {
//		Random random = new Random();
//		LinkFunction logit = new LinkFunction(LinkFunction.Type.Logit);
//		double xBeta = -1.5;
//		logit.setParameterValue(0, xBeta);
//		logit.setVariableValue(0, 1d);
//		double mean = 0;
//		int nbIter = 1000000;
//		double factor = 1d / nbIter;
//		double stdDev = 1d;
//		for (int i = 0; i < nbIter; i++) {
//			logit.setParameterValue(0, xBeta + random.nextGaussian() * stdDev);
//			mean += logit.getValue() * factor;
//		}
//
//		Matrix lowerCholeskyTriangle = new Matrix(1,1);
//		lowerCholeskyTriangle.m_afData[0][0] = 1d;
//		
//		System.out.println("Simulated mean =  " + mean);
//
//		logit.setParameterValue(0, xBeta);
//		
//		
//		List<Integer> parameterIndices = new ArrayList<Integer>();
//		parameterIndices.add(0);
//
//		LaplaceApproximation la = new LaplaceApproximation();
//		double sum = la.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
//		int u = 0;
//	}


}
