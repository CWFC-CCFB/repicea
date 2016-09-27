package repicea.predictor.wbirchloggrades;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.estimates.HorvitzThompsonTauEstimate;
import repicea.stats.estimates.HybridMonteCarloHorvitzThompsonEstimate;
import repicea.stats.estimates.HybridMonteCarloHorvitzThompsonEstimate.VariancePointEstimate;
import repicea.util.ObjectUtility;

public class Population {

	final PlotList sampleUnits;
	Random random = new Random();
	final WBirchLogGradesPredictor superModel = new WBirchLogGradesPredictor(false, true);	// no randomness on parameters since this is the superpopulation model
	
	Population(int populationSize) {
		sampleUnits = new PlotList();
		Plot p;
		for (int i = 0; i < populationSize; i++) {
			int nbTrees = (int) (2 + Math.floor(random.nextDouble() * 21d));
			p = new Plot(i);
			sampleUnits.add(p);
			double dbhCm;
			for (int j = 0; j < nbTrees; j++) {
				dbhCm = 24 + Math.floor(random.nextDouble() * 27d);
				p.addTree(new WBirchLogGradesTreeImpl(PetroGradeSpecies.ERS, dbhCm));
			}
		}
	}
	
	PlotList getSample(int sampleSize) {
		List<Integer> sampleIndex = new ArrayList<Integer>();
		int index;
		while (sampleIndex.size() < sampleSize) {
			index = (int) Math.floor(random.nextDouble() * 1000);
			if (!sampleIndex.contains(index)) {
				sampleIndex.add(index);
			}
		}
		PlotList sample = new PlotList();
		for (Integer ind : sampleIndex) {
			sample.add(sampleUnits.get(ind));
		}
		return sample;
	}
	
	static void setRealizedValues(List<Plot> plots, WBirchLogGradesPredictor model) {
		for (Plot plot : plots) {
			for (WBirchLogGradesTreeImpl tree : plot.getTrees()) {
				tree.setRealizedValues(model.getLogGradeVolumePredictions(stand, tree));
			}
		}
	}
	
	Matrix getTotal() {
		Matrix total = new Matrix(5,1);
		for (Plot plot : sampleUnits) {
			for (WBirchLogGradesTreeImpl tree : plot.getTrees()) {
				total = total.add(tree.getRealizedValues());
			}
		}
		return total;
	}
	
	
	public static void main(String[] args) throws IOException {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		long start = System.currentTimeMillis();
		int populationSize = 1000;
		Population pop = new Population(populationSize);
		int nbRealizations = 10000;
		int nbInternalReal = 1000;
		int sampleSize = 10;
		String filename = ObjectUtility.getPackagePath(Population.class) + "simulation" + sampleSize + ".csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		for (int i = 1; i <= 5; i++) {
			fields.add(new CSVField("TrueTau" + i));
			fields.add(new CSVField("EstTau" + i));
			fields.add(new CSVField("UncorrVar" + i));
			fields.add(new CSVField("CorrVar" + i));
			fields.add(new CSVField("Samp" + i));
			fields.add(new CSVField("Model" + i));
		}

		writer.setFields(fields);

		List<Realization> realizations = new ArrayList<Realization>();
		long timeDiff;
		for (int real = 0; real < nbRealizations; real++) {
			setRealizedValues(pop.sampleUnits, pop.superModel);
			Matrix total = pop.getTotal();
			WBirchLogGradesPredictor currentModel = new WBirchLogGradesPredictor(true, true); // the current model must account for the errors in the parameter estimates
			currentModel.replaceBeta();	// the parameter estimates are drawn at random in the distribution
			PlotList sample = pop.getSample(sampleSize);
			HybridMonteCarloHorvitzThompsonEstimate hybHTEstimate = new HybridMonteCarloHorvitzThompsonEstimate();
			for (int internalReal = 0; internalReal < nbInternalReal; internalReal++) {
				sample.setRealization(internalReal);
				setRealizedValues(sample, currentModel);
				HorvitzThompsonTauEstimate htEstimator = sample.getHorvitzThompsonEstimate(populationSize);
//				Matrix tauHat = htEstimator.getTotal();
//				Matrix varTau = htEstimator.getVarianceOfTotalEstimate();
				hybHTEstimate.addHTEstimate(htEstimator);
			}
			VariancePointEstimate correctedVarEstimate = hybHTEstimate.getVarianceOfTotalEstimate();
			Realization thisRealization = new Realization(total, 
					hybHTEstimate.getTotal(), 
					hybHTEstimate.getTotalVarianceUncorrected(), 
					correctedVarEstimate.getTotalVariance(), 
					correctedVarEstimate.getSamplingRelatedVariance(), 
					correctedVarEstimate.getModelRelatedVariance());
			realizations.add(thisRealization);
			writer.addRecord(thisRealization.getRecord());
			timeDiff = System.currentTimeMillis() - start;
		    double timeByReal = timeDiff / (real + 1);
		    double remainingTime = timeByReal * (nbRealizations - (real + 1)) * 0.001 / 60;
			System.out.println("Running realization " + real +"; Remaining time " + nf.format(remainingTime) + " min.");
		}
		writer.close();
	}
	
}
