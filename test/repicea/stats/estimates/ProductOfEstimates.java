package repicea.stats.estimates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.util.ObjectUtility;

public class ProductOfEstimates {

	private static double LowFactor = 0.05;
	private static double HighFactor = 0.20;
	private static enum Method {Naive, Propagation}
	
	
	private static SimpleEstimate getEstimate(List<Estimate> estimates, Method method) {
		Estimate currentEstimate = null;
		for (int i = 1; i < estimates.size(); i++) {
			if (i == 1) {
				currentEstimate = estimates.get(i - 1);
			}
			
			Matrix alphaMean = currentEstimate.getMean();
			Matrix betaMean = estimates.get(i).getMean();
			Matrix alphaVariance = currentEstimate.getVariance();
			Matrix betaVariance = estimates.get(i).getVariance();
			Matrix newMean = alphaMean.multiply(betaMean);
			Matrix newVariance = alphaMean.elementWisePower(2d).multiply(betaVariance).
					add(betaMean.elementWisePower(2d).multiply(alphaVariance));
			if (method == Method.Naive) {
				newVariance = newVariance.add(alphaVariance.multiply(betaVariance));
			}
			if (newVariance.m_afData[0][0] < 0) {
				int u = 0;
			}
			currentEstimate = new SimpleEstimate(newMean, newVariance);
		}
		return (SimpleEstimate) currentEstimate;
	}

	private static MonteCarloEstimate getMonteCarloEstimate(List<Estimate> estimates, int nbRealizations) {
		MonteCarloEstimate est = new MonteCarloEstimate(); 
		for (int real = 0; real < nbRealizations; real++) {
			double currentProduct = -1;
			for (int i = 1; i < estimates.size(); i++) {
				if (i == 1) {
					currentProduct = estimates.get(i - 1).getRandomDeviate().m_afData[0][0];
				}
				
				currentProduct *= estimates.get(i).getRandomDeviate().m_afData[0][0];
			}
			Matrix obs = new Matrix(1,1);
			obs.m_afData[0][0] = currentProduct;
			est.addRealization(obs);
		}
		return est;
	}

	private static GaussianEstimate getEstimate(GaussianEstimate trueMean, VarianceEstimate trueVariance) {
		return new GaussianEstimate(trueMean.getRandomDeviate(), trueVariance.getRandomDeviate());
	}

	private static VarianceEstimate getTrueVariance(int df, double mean, boolean lowVariability) {
		if (lowVariability) {
			return new VarianceEstimate(df, LowFactor * LowFactor * mean * mean);
		} else {
			return new VarianceEstimate(df, HighFactor * HighFactor * mean * mean);
		}
	}
	
	public static void runSimulation(int nbMaxRealization, boolean lowAlpha, boolean lowBeta, boolean lowGamma, String simulationName) throws Exception {
		System.out.println("Running simulation with lowAlpha = " + ((Boolean) lowAlpha).toString() + " lowBeta = " + ((Boolean) lowBeta).toString() + " lowGamma = " + ((Boolean) lowGamma).toString() + " ...");
		int df = 300;
		double alpha = 20d;
		double beta = 10d;
		double gamma = 2d;
		double trueMean = alpha * beta * gamma;

		VarianceEstimate trueVarAlpha = ProductOfEstimates.getTrueVariance(df, alpha, lowAlpha);
		VarianceEstimate trueVarBeta = ProductOfEstimates.getTrueVariance(df, beta, lowBeta);
		VarianceEstimate trueVarGamma = ProductOfEstimates.getTrueVariance(df, gamma, lowGamma);
		double varAlpha = trueVarAlpha.getMean().m_afData[0][0];
		double varBeta = trueVarBeta.getMean().m_afData[0][0];
		double varGamma = trueVarGamma.getMean().m_afData[0][0];
		
		GaussianEstimate trueAlphaMean = new GaussianEstimate(alpha, varAlpha);
		GaussianEstimate trueBetaMean = new GaussianEstimate(beta, varBeta);
		GaussianEstimate trueGammaMean = new GaussianEstimate(gamma, varGamma);

		
		
		MonteCarloEstimate muGoodman = new MonteCarloEstimate();		
		MonteCarloEstimate varGoodman = new MonteCarloEstimate();		
		MonteCarloEstimate muNaive = new MonteCarloEstimate();		
		MonteCarloEstimate varNaive = new MonteCarloEstimate();		
		MonteCarloEstimate muPropagation = new MonteCarloEstimate();		
		MonteCarloEstimate varPropagation = new MonteCarloEstimate();		
//		MonteCarloEstimate aMonteCarlo = new MonteCarloEstimate();		
//		MonteCarloEstimate bMonteCarlo = new MonteCarloEstimate();		
//		MonteCarloEstimate cMonteCarlo = new MonteCarloEstimate();
		MonteCarloEstimate muMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate varMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate coverage = new MonteCarloEstimate();
		MonteCarloEstimate muRescaledMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate varRescaledMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate rescaledCoverage = new MonteCarloEstimate();
		
		for (int real = 0; real < nbMaxRealization; real++) {
			if (real%1000 == 0) {
				System.out.println("Realization " + real);
			}
			List<Estimate> estimates = new ArrayList<Estimate>();
			estimates.add(getEstimate(trueAlphaMean, trueVarAlpha)); 
			estimates.add(getEstimate(trueBetaMean, trueVarBeta)); 
			estimates.add(getEstimate(trueGammaMean, trueVarGamma)); 

//			Matrix a = estimates.get(0).getMean().matrixStack(estimates.get(0).getVariance(), true);
//			Matrix b = estimates.get(1).getMean().matrixStack(estimates.get(1).getVariance(), true);
//			Matrix c = estimates.get(2).getMean().matrixStack(estimates.get(2).getVariance(), true);
//			
//			aMonteCarlo.addRealization(a);
//			bMonteCarlo.addRealization(b);
//			cMonteCarlo.addRealization(c);
			
			SimpleEstimate productGoodman = Estimate.getProductOfManyEstimates(estimates);
			muGoodman.addRealization(productGoodman.getMean());
			varGoodman.addRealization(productGoodman.getVariance());

			SimpleEstimate productNaive = getEstimate(estimates, Method.Naive);
			muNaive.addRealization(productNaive.getMean());
			varNaive.addRealization(productNaive.getVariance());

			SimpleEstimate productPropagation = getEstimate(estimates, Method.Propagation);
			muPropagation.addRealization(productPropagation.getMean());
			varPropagation.addRealization(productPropagation.getVariance());
			
			MonteCarloEstimate productMC = getMonteCarloEstimate(estimates, 5000);
			muMonteCarlo.addRealization(productMC.getMean());
			varMonteCarlo.addRealization(productMC.getVariance());
			ConfidenceInterval ci = productMC.getConfidenceIntervalBounds(.95);
			Matrix in = new Matrix(1,1);
			if (ci.getLowerLimit().m_afData[0][0] <= trueMean && trueMean <= ci.getUpperLimit().m_afData[0][0]) {
				in.m_afData[0][0] = 1d;
			}
			coverage.addRealization(in);

			
			MonteCarloEstimate scaledProductMC = new MonteCarloEstimate();
			double scalingFactor = Math.sqrt(productGoodman.getVariance().m_afData[0][0] / productNaive.getVariance().m_afData[0][0]);
			if (Double.isNaN(scalingFactor)) {
				int u = 0;
			}
			double mean = productMC.getMean().m_afData[0][0];
			Matrix newReal;
			for (Matrix r : productMC.getRealizations()) {
				newReal = new Matrix(1,1);
				double rValue = r.m_afData[0][0];
				newReal.m_afData[0][0] = scalingFactor * (rValue - mean) + mean;
				scaledProductMC.addRealization(newReal);
			}
			muRescaledMonteCarlo.addRealization(scaledProductMC.getMean());
			varRescaledMonteCarlo.addRealization(scaledProductMC.getVariance());
			
			
			ci = scaledProductMC.getConfidenceIntervalBounds(.95);
			in = new Matrix(1,1);
			if (ci.getLowerLimit().m_afData[0][0] <= trueMean && trueMean <= ci.getUpperLimit().m_afData[0][0]) {
				in.m_afData[0][0] = 1d;
			}
			rescaledCoverage.addRealization(in);
			
		}

		String filename = ObjectUtility.getPackagePath(ProductOfEstimates.class).replace("bin", "test").concat("simulation" + simulationName + ".csv");
		CSVWriter writer = new CSVWriter(new File(filename), false, ","); // splitter is now "," and not ";"
		List<FormatField> formatFields = new ArrayList<FormatField>();
		formatFields.add(new CSVField("sampleSize"));
		formatFields.add(new CSVField("trueMean"));
		formatFields.add(new CSVField("empMean"));
		formatFields.add(new CSVField("trueVariance"));
		formatFields.add(new CSVField("empVariance"));
		formatFields.add(new CSVField("goodmanVariance"));
		formatFields.add(new CSVField("naiveVariance"));
		formatFields.add(new CSVField("propagationVariance"));
		formatFields.add(new CSVField("MeanMC"));
		formatFields.add(new CSVField("VarMC"));
		formatFields.add(new CSVField("coverage"));
		formatFields.add(new CSVField("MeanRescaledMC"));
		formatFields.add(new CSVField("VarRescaledMC"));
		formatFields.add(new CSVField("rescaledCoverage"));
		
		writer.setFields(formatFields);
		
		
		Object[] record = new Object[14];
		record[0] = simulationName;
		record[1] = trueMean;
		record[2] = muGoodman.getMean().m_afData[0][0];
		double trueMeanAB = alpha * beta;
		double trueVarianceAB = alpha * alpha * varBeta + varAlpha * beta * beta + varAlpha * varBeta; 
		double trueVariance = trueMeanAB * trueMeanAB * varGamma + trueVarianceAB * gamma * gamma + trueVarianceAB * varGamma; 
		record[3] = trueVariance;
		record[4] = muGoodman.getVariance().m_afData[0][0];
		record[5] = varGoodman.getMean().m_afData[0][0];
		record[6] = varNaive.getMean().m_afData[0][0];
		record[7] = varPropagation.getMean().m_afData[0][0];
		record[8] = muMonteCarlo.getMean().m_afData[0][0];
		record[9] = varMonteCarlo.getMean().m_afData[0][0];
		record[10] = coverage.getMean().m_afData[0][0];
		record[11] = muRescaledMonteCarlo.getMean().m_afData[0][0];
		record[12] = varRescaledMonteCarlo.getMean().m_afData[0][0];
		record[13] = rescaledCoverage.getMean().m_afData[0][0];
		
		writer.addRecord(record);
		writer.close();

	}
	
	public static void main(String[] args) throws Exception {
		int nbRealizations = 50000;
		runSimulation(nbRealizations, true, true, true, "LLL");
		runSimulation(nbRealizations, false, true, true, "HLL");
		runSimulation(nbRealizations, true, false, true, "LHL");
		runSimulation(nbRealizations, true, true, false, "LLH");
//		runSimulation(nbRealizations, false, false, true, "HHL");
//		runSimulation(nbRealizations, false, true, false, "HLH");
//		runSimulation(nbRealizations, true, false, false, "LHH");
//		runSimulation(nbRealizations, false, false, false, "HHH");
	}
	
}
