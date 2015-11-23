package repicea.math;

@SuppressWarnings("serial")
public class LogFunctionEmbedder extends AbstractMathematicalFunction<Integer, Double, Integer, Double> {

	private final AbstractMathematicalFunction<Integer, Double, Integer, Double> originalFunction;

	public LogFunctionEmbedder(AbstractMathematicalFunction<Integer, Double, Integer, Double> originalFunction) {
		this.originalFunction = originalFunction;
	}

	/**
	 * This method returns the original function.
	 * @return an AbstractMathematicalFunction instance
	 */
	public AbstractMathematicalFunction<Integer, Double, Integer, Double> getOriginalFunction() {
		return originalFunction;
	}
	
	@Override
	public Double getValue() {
		return Math.log(originalFunction.getValue());
	}

	@Override
	public Matrix getGradient() {
		return originalFunction.getGradient().scalarMultiply(1d / originalFunction.getValue());
	}

	@Override
	public Matrix getHessian() {
		double invValue = 1d/originalFunction.getValue();
		Matrix originalGradient = originalFunction.getGradient();
		Matrix part1 = originalGradient.multiply(originalGradient.transpose()).scalarMultiply(- invValue * invValue);
		Matrix part2 = originalFunction.getHessian().scalarMultiply(invValue);
		return part1.add(part2);
	}
	

	@Override
	public void setParameterValue(Integer parameterName, Double parameterValue) {
		originalFunction.setParameterValue(parameterName, parameterValue);
	}

	@Override
	public Double getParameterValue(Integer parameterName) {
		return originalFunction.getParameterValue(parameterName);
	}

	@Override
	public void setVariableValue(Integer variableName, Double variableValue) {
		originalFunction.setVariableValue(variableName, variableValue);
	}

	@Override
	public Double getVariableValue(Integer variableName) {
		return originalFunction.getVariableValue(variableName);
	}

	@Override
	public int getNumberOfParameters() {return originalFunction.getNumberOfParameters();}

	@Override
	public int getNumberOfVariables() {return originalFunction.getNumberOfVariables();}

}
