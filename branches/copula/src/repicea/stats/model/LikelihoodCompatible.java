package repicea.stats.model;

import repicea.math.Matrix;


public interface LikelihoodCompatible {
	
	/**
	 * This method sets the vector of observed values.
	 * @param yVector a row vector (Matrix instance)
	 */
	public void setYVector(Matrix yVector);

	/**
	 * This method returns the vector of observed values.
	 * @return a Matrix instance
	 */
	public Matrix getYVector();

	/**
	 * This method returns the prediction associated with the observation.
	 * @return a Matrix instance
	 */
	public Matrix getPredictionVector();
	
}
