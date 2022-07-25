package repicea.math;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.utility.MatrixUtility;

@SuppressWarnings("serial")
public class ProductFunctionWrapper extends AbstractMathematicalFunction {

	private final List<InternalMathematicalFunctionWrapper> originalFunctions;
	private final Map<Integer, List<InternalMathematicalFunctionWrapper>> parameterMap;
	private final Map<Integer, List<InternalMathematicalFunctionWrapper>> variableMap;
	
	/**
	 * Constructor.
	 * @param wrappedOriginalFunctions a series of InternalMathematicalFunctionWrapper instances 
	 */
	public ProductFunctionWrapper(InternalMathematicalFunctionWrapper... wrappedOriginalFunctions) {
		if (wrappedOriginalFunctions == null || wrappedOriginalFunctions.length < 2) {
			throw new InvalidParameterException("There must be at least two instances of InternalMathematicalFunctionWrapper in the arguments of the constructor!");
		}
		parameterMap = new HashMap<Integer, List<InternalMathematicalFunctionWrapper>>();
		variableMap = new HashMap<Integer, List<InternalMathematicalFunctionWrapper>>();
		List<Integer> newParameterIndex = new ArrayList<Integer>();
		List<Integer> newVariableIndex = new ArrayList<Integer>();

		originalFunctions = new ArrayList<InternalMathematicalFunctionWrapper>();
		for (InternalMathematicalFunctionWrapper originalFunction : wrappedOriginalFunctions) {
			originalFunctions.add(originalFunction);
		}

		for (InternalMathematicalFunctionWrapper w : originalFunctions) {
			for (Integer i : w.getNewParameterIndices()) {
				if (!newParameterIndex.contains(i)) {
					newParameterIndex.add(i);
				}
			}
			for (Integer i : w.getNewVariableIndices()) {
				if (!newVariableIndex.contains(i)) {
					newVariableIndex.add(i);
				}
			}
		}
		
		Collections.sort(newParameterIndex);
		if (newParameterIndex.get(newParameterIndex.size() - 1) != newParameterIndex.size() - 1) {
			throw new InvalidParameterException("The new parameter indices are inconsistent!");
		}
		Collections.sort(newVariableIndex);
		if (newVariableIndex.get(newVariableIndex.size() - 1) != newVariableIndex.size() - 1) {
			throw new InvalidParameterException("The new variable indices are inconsistent!");
		}
		for (Integer i : newParameterIndex) {
			for (InternalMathematicalFunctionWrapper f : originalFunctions) {
				if (f.getNewParameterIndices().contains(i)) {
					if (!parameterMap.containsKey(i)) {
						parameterMap.put(i, new ArrayList<InternalMathematicalFunctionWrapper>());
					}
					parameterMap.get(i).add(f);
				}
			}
		}

		for (Integer i : newVariableIndex) {
			for (InternalMathematicalFunctionWrapper f : originalFunctions) {
				if (f.getNewVariableIndices().contains(i)) {
					if (!variableMap.containsKey(i)) {
						variableMap.put(i, new ArrayList<InternalMathematicalFunctionWrapper>());
					}
					variableMap.get(i).add(f);
				}
			}
		}

	}
	
	@Override
	public Double getValue() {
		double value = 1d;
		for (InternalMathematicalFunctionWrapper w : originalFunctions) {
			value *= w.getValue();
		}
		return value;
	}

	
	@Override
	public Matrix getGradient() {
		return getGradientFromTheseInternalWrapper(originalFunctions);
	}
	
	private Matrix getGradientFromTheseInternalWrapper(List<InternalMathematicalFunctionWrapper> wrappers) {
		Matrix gradient = new Matrix(getNumberOfParameters(), 1);
		for (InternalMathematicalFunctionWrapper w : wrappers) {
			double multiplier = 1d;
			for (InternalMathematicalFunctionWrapper w2 : wrappers) {
				if (!w2.equals(w)) {
					multiplier *= w2.getValue();
				}
			}
			Matrix wGradient = reformateGradient(w);
			MatrixUtility.add(gradient, wGradient.scalarMultiply(multiplier));
		}
		return gradient;
	}

	private Matrix reformateGradient(InternalMathematicalFunctionWrapper w) {
		Matrix gradientTmp = new Matrix(getNumberOfParameters(), 1);
		Matrix wGradient = w.getGradient();
		for (int i = 0; i < wGradient.m_iRows; i++) {
			gradientTmp.setValueAt(w.reverseParmMap.get(i), 0, wGradient.getValueAt(i, 0));
		}
		return gradientTmp;
		
	}
	
	private Matrix reformateHessian(InternalMathematicalFunctionWrapper w) {
		Matrix hessianTmp = new Matrix(getNumberOfParameters(), getNumberOfParameters());
		Matrix wHessian = w.getHessian();
		for (int i = 0; i < wHessian.m_iRows; i++) {
			for (int j = i; j < wHessian.m_iRows; j++) {
				hessianTmp.setValueAt(w.reverseParmMap.get(i), w.reverseParmMap.get(j), wHessian.getValueAt(i, j));
				if (i !=  j) {
					hessianTmp.setValueAt(w.reverseParmMap.get(j), w.reverseParmMap.get(i), wHessian.getValueAt(j, i));
				}
			}
		}
		return hessianTmp;
	}

	
	@Override
	public Matrix getHessian() {
		Matrix hessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
		for (InternalMathematicalFunctionWrapper w : originalFunctions) {
			List<InternalMathematicalFunctionWrapper> wrappersOtherThanW = getWrapperListWithoutThisOne(w);
			double multiplier = 1d;
			for (InternalMathematicalFunctionWrapper w2 : wrappersOtherThanW) {
				multiplier *= w2.getValue();
			}
			Matrix theirGradient = getGradientFromTheseInternalWrapper(wrappersOtherThanW);
			Matrix gradientPart = reformateGradient(w).multiply(theirGradient.transpose());
			Matrix hessianPart = reformateHessian(w).scalarMultiply(multiplier);
			MatrixUtility.add(hessian, hessianPart.add(gradientPart));
		}
		return hessian;
	}
	
	private List<InternalMathematicalFunctionWrapper> getWrapperListWithoutThisOne(InternalMathematicalFunctionWrapper w) {
		List<InternalMathematicalFunctionWrapper> wrappers = new ArrayList<InternalMathematicalFunctionWrapper>(originalFunctions);
		wrappers.remove(w);
		return wrappers;
	}

	
	
	@Override
	public final void setParameterValue(int parameterIndex, double parameterValue) {
		if (!parameterMap.containsKey(parameterIndex)) {
			throw new InvalidParameterException("The parameter index is invalid!");
		}
		for (InternalMathematicalFunctionWrapper w : parameterMap.get(parameterIndex)) {
			w.setParameterValue(parameterIndex, parameterValue);
		}
	}

	@Override
	public final double getParameterValue(int parameterIndex) {
		if (!parameterMap.containsKey(parameterIndex)) {
			throw new InvalidParameterException("The parameter index is invalid!");
		}
		return parameterMap.get(parameterIndex).get(0).getParameterValue(parameterIndex);
	}

	@Override
	public final void setVariableValue(int variableIndex, double variableValue) {
		if (!variableMap.containsKey(variableIndex)) {
			throw new InvalidParameterException("The variable index is invalid!");
		}
		for (InternalMathematicalFunctionWrapper w : variableMap.get(variableIndex)) {
			w.setVariableValue(variableIndex, variableValue);
		}
	}

	@Override
	public final double getVariableValue(int variableIndex) {
		if (!variableMap.containsKey(variableIndex)) {
			throw new InvalidParameterException("The variable index is invalid!");
		}
		return variableMap.get(variableIndex).get(0).getParameterValue(variableIndex);
	}

	@Override
	public final int getNumberOfParameters() {return parameterMap.size();}

	@Override
	public final int getNumberOfVariables() {return variableMap.size();}

	
	@Override
	public final void setVariables(Matrix xVector) {
		super.setVariables(xVector);
	}
	
	@Override
	public final void setParameters(Matrix beta) {
		super.setParameters(beta);
	}
	
	@Override
	public final Matrix getParameters() {
		return super.getParameters();
	}

	@Override
	public void setBounds(int parameterIndex, ParameterBound bound) {
		throw new UnsupportedOperationException("The bounds have not been implemented in the ProductFunctionWrapper class!");
	}

	
}
