package repicea.math;

@SuppressWarnings("serial")
public class LogFunctionWrapper extends AbstractMathematicalFunctionWrapper {

	public LogFunctionWrapper(AbstractMathematicalFunction originalFunction) {
		super(originalFunction);
	}

	@Override
	public Double getValue() {
		return Math.log(getOriginalFunction().getValue());
	}

	@Override
	public Matrix getGradient() {
		return getOriginalFunction().getGradient().scalarMultiply(1d / getOriginalFunction().getValue());
	}

	@Override
	public Matrix getHessian() {
		double invValue = 1d/getOriginalFunction().getValue();
		Matrix originalGradient = getOriginalFunction().getGradient();
		Matrix part1 = originalGradient.multiply(originalGradient.transpose()).scalarMultiply(- invValue * invValue);
		Matrix part2 = getOriginalFunction().getHessian().scalarMultiply(invValue);
		return part1.add(part2);
	}

}
