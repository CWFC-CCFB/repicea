package repicea.math;


@SuppressWarnings("serial")
public abstract class AbstractMathematicalFunctionWrapper extends AbstractMathematicalFunction {

	private final AbstractMathematicalFunction originalFunction;

	public AbstractMathematicalFunctionWrapper(AbstractMathematicalFunction originalFunction) {
		this.originalFunction = originalFunction;
	}

	/**
	 * This method returns the original function.
	 * @return an AbstractMathematicalFunction instance
	 */
	public AbstractMathematicalFunction getOriginalFunction() {return originalFunction;}
	
	@Override
	public abstract Double getValue();

	@Override
	public abstract Matrix getGradient();

	@Override
	public abstract Matrix getHessian();
	

	@Override
	public void setParameterValue(int parameterIndex, double parameterValue) {
		getOriginalFunction().setParameterValue(parameterIndex, parameterValue);
	}

	@Override
	public double getParameterValue(int parameterIndex) {
		return getOriginalFunction().getParameterValue(parameterIndex);
	}

	@Override
	public void setVariableValue(int variableIndex, double variableValue) {
		getOriginalFunction().setVariableValue(variableIndex, variableValue);
	}

	@Override
	public double getVariableValue(int variableIndex) {
		return getOriginalFunction().getVariableValue(variableIndex);
	}

	@Override
	public int getNumberOfParameters() {return getOriginalFunction().getNumberOfParameters();}

	@Override
	public int getNumberOfVariables() {return getOriginalFunction().getNumberOfVariables();}

	
	@Override
	public void setX(Matrix x) {getOriginalFunction().setX(x);}
	
	@Override
	public void setBeta(Matrix beta) {getOriginalFunction().setBeta(beta);}
	
	@Override
	public Matrix getBeta() {return getOriginalFunction().getBeta();}

	@Override
	public void setBounds(int parameterIndex, ParameterBound bound) {
		getOriginalFunction().setBounds(parameterIndex, bound);
	}

}
