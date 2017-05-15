package repicea.predictor.wbirchloggrades.simplelinearmodel;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	final SimpleLinearModel superModel = new SimpleLinearModel(false, true);	// no randomness on parameters since this is the superpopulation model
	
	Population(int populationSize) {
		sampleUnits = new PlotList();
		SamplePlot p;
		for (int i = 0; i < populationSize; i++) {
			double x = (2 + random.nextDouble() * 6d);
			p = new SamplePlot(i + "", x);
			sampleUnits.add(p);
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
	
	static void setRealizedValues(List<SamplePlot> plots, SimpleLinearModel model) {
		for (SamplePlot plot : plots) {
			plot.setY(model.predictY(plot));
		}
	}
	
	Matrix getTotal() {
		Matrix total = new Matrix(1,1);
		for (SamplePlot plot : sampleUnits) {
			total = total.add(plot.getY());
		}
		return total;
	}
	
	
	public static void main(String[] args) throws IOException {
		boolean simpleReplacement = true;		// default is true
		SimpleLinearModel.R2_95Version = false;	// default is false
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		long start = System.currentTimeMillis();
		int populationSize = 1000;
		Population pop = new Population(populationSize);
		int nbRealizations = 10000; 
		int nbInternalReal = 1000;
		int sampleSize = 50;
		String filename;
		if (SimpleLinearModel.R2_95Version) {
			filename = ObjectUtility.getPackagePath(Population.class) + "simulationR2_95_" + sampleSize + ".csv";
		} else if (!simpleReplacement) {
			filename = ObjectUtility.getPackagePath(Population.class) + "fromDataset_" + sampleSize + ".csv";
		} else {
			filename = ObjectUtility.getPackagePath(Population.class) + "simulation" + sampleSize + ".csv";
		}
		filename = filename.replace("bin", "manuscripts");
		
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("TrueTau"));
		fields.add(new CSVField("EstTau"));
		fields.add(new CSVField("UncorrVar"));
		fields.add(new CSVField("CorrVar"));
		fields.add(new CSVField("Samp"));
		fields.add(new CSVField("Model"));
		writer.setFields(fields);

		String filenameSingleSimulation = ObjectUtility.getPackagePath(Population.class) + "stabilize" + sampleSize + ".csv";
		filenameSingleSimulation = filenameSingleSimulation.replace("bin", "manuscripts");
		CSVWriter writerStabilizer = new CSVWriter(new File(filenameSingleSimulation), false);
		List<FormatField> fieldsStabilizer = new ArrayList<FormatField>();
		fieldsStabilizer.add(new CSVField("RealID"));
		fieldsStabilizer.add(new CSVField("Mean"));
		fieldsStabilizer.add(new CSVField("Var"));
		writerStabilizer.setFields(fieldsStabilizer);
		boolean isWriterStabilizerOpen = true;

		Object[] recordStabilizer = new Object[3];
		List<Realization> realizations = new ArrayList<Realization>();
		long timeDiff;
		for (int real = 0; real < nbRealizations; real++) {
			setRealizedValues(pop.sampleUnits, pop.superModel);
			Matrix total = pop.getTotal();
			SimpleLinearModel currentModel = new SimpleLinearModel(true, true); // the current model must account for the errors in the parameter estimates
			PlotList sample;;
			if (simpleReplacement) {
				currentModel.replaceModelParameters();	// the parameter estimates are drawn at random in the distribution
				sample = pop.getSample(sampleSize);
			} else {
				sample = pop.getSample(sampleSize + 100);
				PlotList dataSet = new PlotList();
				for (int i = 0; i < 100; i++) {
					dataSet.add(sample.remove(0));
				}
				currentModel.replaceModelParameters(dataSet);
			}
			HybridMonteCarloHorvitzThompsonEstimate hybHTEstimate = new HybridMonteCarloHorvitzThompsonEstimate();
			for (int internalReal = 0; internalReal < nbInternalReal; internalReal++) {
				sample.setRealization(internalReal);
				setRealizedValues(sample, currentModel);
				HorvitzThompsonTauEstimate htEstimator = sample.getHorvitzThompsonEstimate(populationSize);
				hybHTEstimate.addHTEstimate(htEstimator);
				if (!SimpleLinearModel.R2_95Version && real == 0 && internalReal >= 1) {
					recordStabilizer[0] = internalReal;
					recordStabilizer[1] = hybHTEstimate.getTotal().m_afData[0][0];
					recordStabilizer[2] = hybHTEstimate.getVarianceOfTotalEstimate().getTotalVariance().m_afData[0][0];
					writerStabilizer.addRecord(recordStabilizer);
				} else if (real > 0 && isWriterStabilizerOpen) {
					writerStabilizer.close();
					isWriterStabilizerOpen = false;
				}
			}
			VariancePointEstimate correctedVarEstimate = hybHTEstimate.getVarianceOfTotalEstimate();
			Realization thisRealization = new Realization(total.m_afData[0][0], 
					hybHTEstimate.getTotal().m_afData[0][0], 
					hybHTEstimate.getTotalVarianceUncorrected().m_afData[0][0], 
					correctedVarEstimate.getTotalVariance().m_afData[0][0], 
					correctedVarEstimate.getSamplingRelatedVariance().m_afData[0][0], 
					correctedVarEstimate.getModelRelatedVariance().m_afData[0][0]);
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
