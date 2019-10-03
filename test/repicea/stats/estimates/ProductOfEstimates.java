package repicea.stats.estimates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;
import repicea.util.ObjectUtility;

public class ProductOfEstimates {

	private static final Random R = new Random();

	private static SimpleEstimate getNaiveEstimate(List<Estimate> estimates) {
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
					add(betaMean.elementWisePower(2d).multiply(alphaVariance)).
					add(alphaVariance.multiply(betaVariance));
			currentEstimate = new SimpleEstimate(newMean, newVariance);
		}
		return (SimpleEstimate) currentEstimate;
	}

	private static SimpleEstimate getPropagationEstimate(List<Estimate> estimates) {
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

	private static PopulationMeanEstimate getEstimate(int sampleSize, double mean, double variance) {
		PopulationMeanEstimate estimate = new PopulationMeanEstimate();
		double stdDev = Math.sqrt(variance);
		Matrix obs;
		for (int i = 0; i < sampleSize; i++) {
			obs = new Matrix(1,1);
			obs.m_afData[0][0] = R.nextGaussian() * stdDev + mean;
			estimate.addObservation(new PopulationUnitWithEqualInclusionProbability(obs));
		}
		return estimate;
	}
	
	
	public static void runSimulation(int sampleSize, int nbMaxRealization) throws Exception {
		System.out.println("Running simulation for sample size = " + sampleSize + " ...");
		double alpha = 20d;
		double beta = 10d;
		double gamma = 2d;
		double trueMean = alpha * beta * gamma;
		
		double aVar = 30;
		double bVar = 9;
		double cVar = 1;
		
		double varMuA = aVar / sampleSize;
		double varMuB = bVar / sampleSize;
		double varMuC = cVar / sampleSize;

		MonteCarloEstimate muGoodman = new MonteCarloEstimate();		
		MonteCarloEstimate varGoodman = new MonteCarloEstimate();		
		MonteCarloEstimate muNaive = new MonteCarloEstimate();		
		MonteCarloEstimate varNaive = new MonteCarloEstimate();		
		MonteCarloEstimate muPropagation = new MonteCarloEstimate();		
		MonteCarloEstimate varPropagation = new MonteCarloEstimate();		
		MonteCarloEstimate aMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate bMonteCarlo = new MonteCarloEstimate();		
		MonteCarloEstimate cMonteCarlo = new MonteCarloEstimate();
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
			estimates.add(getEstimate(sampleSize, alpha, aVar)); 
			estimates.add(getEstimate(sampleSize, beta, bVar)); 
			estimates.add(getEstimate(sampleSize, gamma, cVar)); 

			Matrix a = estimates.get(0).getMean().matrixStack(estimates.get(0).getVariance(), true);
			Matrix b = estimates.get(1).getMean().matrixStack(estimates.get(1).getVariance(), true);
			Matrix c = estimates.get(2).getMean().matrixStack(estimates.get(2).getVariance(), true);
			
			aMonteCarlo.addRealization(a);
			bMonteCarlo.addRealization(b);
			cMonteCarlo.addRealization(c);
			
			SimpleEstimate productGoodman = Estimate.getProductOfManyEstimates(estimates);
			muGoodman.addRealization(productGoodman.getMean());
			varGoodman.addRealization(productGoodman.getVariance());

			SimpleEstimate productNaive = getNaiveEstimate(estimates);
			muNaive.addRealization(productNaive.getMean());
			varNaive.addRealization(productNaive.getVariance());

			SimpleEstimate productPropagation = getPropagationEstimate(estimates);
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

		String filename = ObjectUtility.getPackagePath(ProductOfEstimates.class).replace("bin", "test").concat("simulation" + sampleSize + ".csv");
		CSVWriter writer = new CSVWriter(new File(filename), false);
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
		record[0] = sampleSize;
		record[1] = trueMean;
		record[2] = muGoodman.getMean().m_afData[0][0];
		double trueMeanAB = alpha * beta;
		double trueVarianceAB = alpha * alpha * varMuB + varMuA * beta * beta + varMuA * varMuB; 
		double trueVariance = trueMeanAB * trueMeanAB * varMuC + trueVarianceAB * gamma * gamma + trueVarianceAB * varMuC; 
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
//		runSimulation(10, 100000);
		runSimulation(25, 100000);
//		runSimulation(50, 50000);
//		runSimulation(100, 50000);
	}
	
}
