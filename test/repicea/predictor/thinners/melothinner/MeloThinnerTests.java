package repicea.predictor.thinners.melothinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.simulation.covariateproviders.standlevel.SlopeMRNFClassProvider.SlopeMRNFClass;
import repicea.util.ObjectUtility;

public class MeloThinnerTests {

	private static List<MeloThinnerPlotImpl> Plots;
	
	
	
	
	
	private static void ReadPlots() throws IOException {
		if (Plots == null) {
			Plots = new ArrayList<MeloThinnerPlotImpl>();
			String testFile = ObjectUtility.getRelativePackagePath(MeloThinnerTests.class) + "HarvestPred.csv";
			CSVReader reader = new CSVReader(testFile);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String plotId = record[1].toString();
				String slopeClass = record[2].toString();
				SlopeMRNFClass sc = SlopeMRNFClass.valueOf(slopeClass);
				double basalAreaM2Ha = Double.parseDouble(record[3].toString());
				double stemDensityHa = Double.parseDouble(record[4].toString());
				String ecologicalType = record[5].toString();
				List<Integer> possIndex = new ArrayList<Integer>();
				for (int i = 7; i <=33; i++) {
					if (record[i].toString().equals("1")) {
						possIndex.add( i + 27 );
					}
				}
				double[] aac = new double[possIndex.size()];
				for (int i = 0; i < possIndex.size(); i++) {
					aac[i] = Double.parseDouble(record[possIndex.get(i)].toString());
				}
				double pred = Double.parseDouble(record[71].toString());
				double meanPA = Double.parseDouble(record[72].toString());
				
				MeloThinnerPlotImpl plot = new MeloThinnerPlotImpl(plotId, 
						basalAreaM2Ha, 
						stemDensityHa, 
						ecologicalType, 
						sc,
						aac,
						pred,
						meanPA);
				Plots.add(plot);
			}
			reader.close();
		}
	}

	@Test
	public void testModelAgainstConditionalSASPrediction() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		predictor.setGaussianQuadrature(false);
		for (MeloThinnerPlotImpl plot : Plots) {
			double actual = 1 - predictor.predictEventProbability(plot, null, plot.getAAC()); // 1 - probability of harvesting to get the survival
			double expected = plot.getPredSurvival();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-8);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}

	@Test
	public void testModelAgainstMarginalSASPrediction() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		for (MeloThinnerPlotImpl plot : Plots) {
			double actual = 1 - predictor.predictEventProbability(plot, null, plot.getAAC()); // 1 - probability of harvesting to get the survival
			double expected = plot.getMeanPA();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-6);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}

	
	
}
