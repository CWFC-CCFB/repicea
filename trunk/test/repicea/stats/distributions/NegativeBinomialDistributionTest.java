package repicea.stats.distributions;


import java.util.Random;

//import org.apache.commons.math.special.Gamma;

public class NegativeBinomialDistributionTest {
	
	public static Random RANDOM = new Random();

//	public static void main(String[] args) {
//		double fBinNeg = 0.10770264751659242;
//		double dDispersionBinNeg = 1.46512601239562;
//		int nbIter = 50000;
//		double mean = 0d;
//		
//		for (int i = 0; i < nbIter; i++) {			// to determine whether there is recruitment or not
//			double fTreeFreq = 0d;
//			double threshold = RANDOM.nextDouble();	// to determine how many recruits there are
//			double prob = 0.0;
//			double fTmp = dDispersionBinNeg*fBinNeg;
//			double fTmp2 = 1/dDispersionBinNeg;
//			double fTmp3 = 1.0;
//			double constant = 0.0;
//			try {
//				constant = Gamma.logGamma(fTmp2);
//				
//				while ((threshold > prob)&&(fTreeFreq<80)) {		// maximum number of recruits is set to 80
//					prob += Math.exp(Gamma.logGamma(fTreeFreq + fTmp2) 
//							- Gamma.logGamma(fTreeFreq + 1.0) - constant)* fTmp3 	// fTmp3 replaces : * Math.pow(fTmp,fTreeFreq)
//							/ (Math.pow(1+fTmp,fTreeFreq + fTmp2));
//					fTmp3 *= fTmp;
//					fTreeFreq ++;
//					if (fTreeFreq == 80) {
//						System.out.println("WARNING - Recruits threshold reached!");
//					}
//				}
//				mean += fTreeFreq / nbIter;
//			} catch (Exception e) {
//				System.out.println ("Unable to compute the negative binomial distribution - Recruitment module");
//			}
//			
//		}
//		System.out.println("Expected mean = " + fBinNeg + "; Simulated mean = " + mean);
//		System.exit(0);
//	}

}
