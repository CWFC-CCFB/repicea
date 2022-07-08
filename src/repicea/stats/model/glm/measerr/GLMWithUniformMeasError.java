package repicea.stats.model.glm.measerr;

import java.security.InvalidParameterException;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.EvaluableFunction;
import repicea.math.ExponentialIntegralFunction;
import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataSet;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LikelihoodGLM;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

public class GLMWithUniformMeasError extends GeneralizedLinearModel {

	@SuppressWarnings("serial")
	private class LinkFunctionWithMeasError extends LinkFunction {

		private final LinkFunction linkFunctionErrorFreeObs;
		private final LinkFunction logLinkFunction;
		
		public LinkFunctionWithMeasError() {
			super(Type.CLogLog);
			linkFunctionErrorFreeObs = new LinkFunction(Type.CLogLog, getOriginalFunction());
			logLinkFunction = new LinkFunction(Type.Log, getOriginalFunction());
		}
		
		private boolean isObservationErrorFree() {
			return getOriginalFunction().getVariableValue(indexEffectWithMeasError) == minEffectWithMeasError;
		}
		
		@Override
		public Double getValue() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getValue();
			} else {
//				double predictedWithoutError = linkFunctionErrorFreeObs.getValue();
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double betaValue = getOriginalFunction().getParameterValue(indexEffectWithMeasError);
				if (betaValue == 0d) {	// exact zero values must be avoided. Otherwise the above result is NaN
					betaValue += StatisticalUtility.getRandom().nextDouble() * .001;
					getOriginalFunction().setParameterValue(indexEffectWithMeasError, betaValue);
				}
				double density = 1d/(maxValue - minEffectWithMeasError);
				double upperBound = (maxValue - ExponentialIntegralFunction.getEi(- logLinkFunction.getValue(), logLinkFunction.getOriginalFunction().getValue()) / betaValue) * density;
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, minEffectWithMeasError);
				double lowerBound = (minEffectWithMeasError - ExponentialIntegralFunction.getEi(- logLinkFunction.getValue(), logLinkFunction.getOriginalFunction().getValue()) / betaValue) * density;
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, maxValue);
				double marginalProbability = upperBound - lowerBound;
				if (marginalProbability == 0d || marginalProbability == 1d || Double.isNaN(marginalProbability)) {
					int u = 0;
				}
				return marginalProbability;
			}
		}

		@Override
		public Matrix getGradient() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getGradient();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double betaValue = getOriginalFunction().getParameterValue(indexEffectWithMeasError);
				double invdensity = maxValue - minEffectWithMeasError;
				Matrix originalFunctionGradient = getOriginalFunction().getGradient();
				Matrix upperBound = getGradientBound(originalFunctionGradient, betaValue, invdensity);
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, minEffectWithMeasError);
				Matrix lowerBound = getGradientBound(originalFunctionGradient, betaValue, invdensity);
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, maxValue);
				Matrix gradient = upperBound.subtract(lowerBound);
				return gradient;
			}
		}
		
		private Matrix getGradientBound(Matrix originalFunctionGradient, double betaValue, double invdensity) {
			double minusExpXBeta = - logLinkFunction.getValue();
			Matrix bound = originalFunctionGradient.scalarMultiply(- Math.exp(minusExpXBeta) / (betaValue*invdensity) );
			double gradientValueForParameterWithMeasErr = bound.getValueAt(indexEffectWithMeasError, 0);
			gradientValueForParameterWithMeasErr += ExponentialIntegralFunction.getEi(minusExpXBeta, logLinkFunction.getOriginalFunction().getValue()) / (betaValue * betaValue * invdensity);
			bound.setValueAt(indexEffectWithMeasError, 0, gradientValueForParameterWithMeasErr);
			return bound;
		}


		@Override
		public Matrix getHessian() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getHessian();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double betaValue = getOriginalFunction().getParameterValue(indexEffectWithMeasError);
				double invdensity = maxValue - minEffectWithMeasError;
				Matrix originalFunctionGradient = getOriginalFunction().getGradient();
				Matrix upperBound = getHessianBound(originalFunctionGradient, betaValue, invdensity);
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, minEffectWithMeasError);
				Matrix lowerBound = getHessianBound(originalFunctionGradient, betaValue, invdensity);
				getOriginalFunction().setVariableValue(indexEffectWithMeasError, maxValue);
				Matrix hessian = upperBound.subtract(lowerBound);
				return hessian;
			}
		}
		
		private Matrix getHessianBound(Matrix originalFunctionGradient, double betaValue, double invdensity) {
			Matrix originalGradient = linkFunctionErrorFreeObs.getGradient();
			Matrix bound = linkFunctionErrorFreeObs.getGradient().multiply(originalFunctionGradient.transpose()).scalarMultiply(1d / (betaValue * invdensity)); 
			double minusExpXBeta = -logLinkFunction.getValue();
			Matrix additionalTerm1 = originalFunctionGradient.scalarMultiply(Math.exp(minusExpXBeta) / (betaValue * betaValue * invdensity));
			for (int i = 0; i < bound.m_iCols; i++) {
				double value = bound.getValueAt(indexEffectWithMeasError, i);
				if (i == indexEffectWithMeasError) {
					value += 2 * additionalTerm1.getValueAt(i, 0);
					value += -2 * ExponentialIntegralFunction.getEi(minusExpXBeta, logLinkFunction.getOriginalFunction().getValue()) / (betaValue * betaValue * betaValue * invdensity);
					bound.setValueAt(i, i, value);
				} else {
					value += additionalTerm1.getValueAt(i, 0);
					bound.setValueAt(indexEffectWithMeasError, i, value);
					bound.setValueAt(i, indexEffectWithMeasError, value);
				}
			}
			return bound;
		}

	}
	
	
	static class GradientHessianProvider implements EvaluableFunction<Matrix> {

		private final AbstractMathematicalFunction originalFunction;
		private final boolean isForGradient;
		
		GradientHessianProvider(AbstractMathematicalFunction originalFunction, boolean isForGradient) {
			this.originalFunction = originalFunction;
			this.isForGradient = isForGradient;
		}
		
		@Override
		public Matrix getValue() {
			return isForGradient ? originalFunction.getGradient() : originalFunction.getHessian();
		}

		@Override
		public void setVariableValue(int variableIndex, double variableValue) {
			originalFunction.setVariableValue(variableIndex, variableValue);
		}

		@Override
		public void setParameterValue(int parameterIndex, double parameterValue) {
			originalFunction.setParameterValue(parameterIndex, parameterValue);
		}

		@Override
		public double getVariableValue(int variableIndex) {
			return originalFunction.getVariableValue(variableIndex);
		}

		@Override
		public double getParameterValue(int parameterIndex) {
			return originalFunction.getParameterValue(parameterIndex);
		}
		
	}
		
	@SuppressWarnings("serial")
	private class LinkFunctionWithMeasError2 extends LinkFunction {

		private final LinkFunction linkFunctionErrorFreeObs;
//		private final LinkFunction logLinkFunction;
		private final GradientHessianProvider gradientProvider;
		private final GradientHessianProvider hessianProvider;
		
		private TrapezoidalRule tr;
		
		public LinkFunctionWithMeasError2() {
			super(Type.CLogLog);
			linkFunctionErrorFreeObs = new LinkFunction(Type.CLogLog, getOriginalFunction());
//			logLinkFunction = new LinkFunction(Type.Log, getOriginalFunction());
			gradientProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, true);
			hessianProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, false);
			tr = new TrapezoidalRule(0.1);
		}
		
		private boolean isObservationErrorFree() {
			return getOriginalFunction().getVariableValue(indexEffectWithMeasError) == minEffectWithMeasError;
		}
		
		@Override
		public Double getValue() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getValue();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - minEffectWithMeasError;
				tr.setLowerBound(minEffectWithMeasError);
				tr.setUpperBound(maxValue);
				double marginalProbability = tr.getIntegralApproximation(linkFunctionErrorFreeObs, indexEffectWithMeasError, false) / invdensity; // false: it is a variable.
				return marginalProbability;
			}
		}

		@Override
		public Matrix getGradient() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getGradient();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - minEffectWithMeasError;
				tr.setLowerBound(minEffectWithMeasError);
				tr.setUpperBound(maxValue);
				Matrix marginalGradient = tr.getIntegralApproximationForMatrixFunction(gradientProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalGradient;
			}
		}

		@Override
		public Matrix getHessian() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getHessian();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - minEffectWithMeasError;
				tr.setLowerBound(minEffectWithMeasError);
				tr.setUpperBound(maxValue);
				Matrix marginalHessian = tr.getIntegralApproximationForMatrixFunction(hessianProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalHessian;
			}
		}


	}

	
	
	
	protected final int indexEffectWithMeasError;
	protected final double minEffectWithMeasError = 0d;
	
	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, String effectWithMeasError, Matrix startingValues) {
		super(dataSet, Type.CLogLog, modelDefinition, startingValues);
		indexEffectWithMeasError = getDataStructure().indexOfThisEffect(effectWithMeasError);
		if (indexEffectWithMeasError == -1) {
			throw new InvalidParameterException("The effect with measurement error " + effectWithMeasError + " is not part of the model definition!");
		} 
	}

	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, String effectWithMeasError) {
		this(dataSet, modelDefinition, effectWithMeasError, null);
	}
	
	@Override
	protected void initializeLinkFunction(Type linkFunctionType) {
//		LinkFunction lf = new LinkFunction(linkFunctionType);
		LinkFunction lf = new LinkFunctionWithMeasError2();
		individualLLK = new IndividualLogLikelihood(new LikelihoodGLM(lf));
		setCompleteLLK();
	}

	public void setVerbose(boolean enabled) {
		
	}
}
