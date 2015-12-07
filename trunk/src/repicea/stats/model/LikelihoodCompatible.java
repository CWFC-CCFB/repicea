package repicea.stats.model;


public interface LikelihoodCompatible<P extends Number> {
	
	/**
	 * This method sets the vector of observed values.
	 * @param y a Matrix or a Double instance
	 */
	public void setY(P y);
	
}
