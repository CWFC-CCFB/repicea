package repicea.math;

@SuppressWarnings("serial")
public class LogFunctionWrapper extends AbstractMathematicalFunctionWrapper {

	public LogFunctionWrapper(AbstractMathematicalFunction<Integer, Double, Integer, Double> originalFunction) {
		super(originalFunction);
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

}
