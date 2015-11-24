package repicea.stats.model;

import repicea.math.Matrix;

public interface LikelihoodCompatible<P extends Number> {

	/**
	 * This method sets the vector of explanatory variables. The method essentially
	 * relies on the setVariableValue() of the AbstractMathematicalFunction class.
	 * @param x a Matrix instance 
	 */
	public void setX(Matrix x);
	
	/**
	 * This method sets the vector of parameters. The method essentially relies on
	 * the setParameterValue() of the AbstractMathematicalFunction class.
	 * @param beta a Matrix instance
	 */
	public void setBeta(Matrix beta);
	
	/**
	 * This method returns the vector of parameters.
	 * @return a Matrix instance
	 */
	public Matrix getBeta();
	
	/**
	 * This method sets the vector of observed values.
	 * @param y a Matrix or a Double instance
	 */
	public void setY(P y);
	
}
