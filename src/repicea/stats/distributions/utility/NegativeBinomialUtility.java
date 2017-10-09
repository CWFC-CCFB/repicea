package repicea.stats.distributions.utility;

public class NegativeBinomialUtility {
	
	public static double getMassProbability(int k, double mean, double dispersion) {
		double prob = 0.0;
		double fTmp = dispersion * mean;
		double fTmp2 = 1/dispersion;
		double constant = 0.0;
		
		constant = GammaUtility.logGamma(fTmp2);

		prob = Math.exp(GammaUtility.logGamma(k + fTmp2) 
				- GammaUtility.logGamma(k + 1.0) - constant) *  Math.pow(fTmp,k)
				/ (Math.pow(1+fTmp,k + fTmp2));
		return prob;
	}
}
