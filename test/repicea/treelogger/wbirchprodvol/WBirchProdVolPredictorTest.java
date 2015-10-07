package repicea.treelogger.wbirchprodvol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class WBirchProdVolPredictorTest {

	@SuppressWarnings("unused")
	@Test
	public void testFixedEffectPredictions() {
		List<WBirchProdVolStandImpl> stands = readStands();
		WBirchProdVolPredictor predictor = new WBirchProdVolPredictor(false, false);
		predictor.isTestPurpose = true;
		int nbTrees = 0;
		int nbMatches = 0;
		int nbMatches2 = 0;
		for (WBirchProdVolStandImpl stand : stands) {
			for (WBirchProdVolTreeImpl tree : stand.getTrees().values()) {
				Matrix pred = predictor.getLogGradeVolumePredictions(stand, tree);
				//		Matrix variances = predictor.getVMatrixForThisTree(tree);
				Matrix predRef = tree.getPredRef();
				Assert.assertEquals("Number of elements", predRef.m_iRows, pred.m_iRows);
				for (int i = 0; i < pred.m_iRows; i++) {
					Assert.assertEquals("Comparing tree " + tree.getSubjectId() + " in plot " + stand.getSubjectId(), predRef.m_afData[i][0], pred.m_afData[i][0], 1E-6);
					nbMatches2++;
				}
				nbMatches += 5;
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}
	
	private static List<WBirchProdVolStandImpl> readStands() {
		List<WBirchProdVolStandImpl> standList = new ArrayList<WBirchProdVolStandImpl>();
		String filename = ObjectUtility.getPackagePath(WBirchProdVolPredictorTest.class) + "pred-simul.csv";
		Map<String, WBirchProdVolStandImpl> standMap = new HashMap<String, WBirchProdVolStandImpl>();
		try {
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			String plotID;
			int treeID;
			String quality;
			double dbhCm;
			double h20Obs;
			double h20Pred;
			double merVolPred;
			double pulpVolPred;
			double lowGradeSawlogVolPred;
			double sawlogVolPred;
			double lowGradeVeneerVolPred;
			double veneerVolPred;
			double elevation;
			WBirchProdVolStandImpl stand;
			while ((record = reader.nextRecord()) != null) {
				plotID = record[0].toString().trim();
				treeID = Integer.parseInt(record[1].toString());
				quality = record[2].toString().trim().toUpperCase();
				dbhCm = Double.parseDouble(record[4].toString());
				elevation = Double.parseDouble(record[14].toString()); 
				h20Obs = Double.parseDouble(record[5].toString());
				h20Pred = Double.parseDouble(record[18].toString());
				merVolPred = Double.parseDouble(record[19].toString());
				pulpVolPred = Double.parseDouble(record[20].toString());
				lowGradeSawlogVolPred = Double.parseDouble(record[21].toString());
				sawlogVolPred = Double.parseDouble(record[22].toString());
				lowGradeVeneerVolPred = Double.parseDouble(record[23].toString());		
				veneerVolPred = Double.parseDouble(record[24].toString());		

				if (!standMap.containsKey(plotID)) {
					standMap.put(plotID, new WBirchProdVolStandImpl(plotID, elevation));
				}
				
				stand = standMap.get(plotID);
				if (!stand.getTrees().containsKey(treeID)) {
					WBirchProdVolTreeImpl tree = new WBirchProdVolTreeImpl(treeID, 
							quality, 
							dbhCm, 
							stand,
							h20Obs,
							h20Pred, 
							merVolPred, 
							pulpVolPred, 
							lowGradeSawlogVolPred,
							sawlogVolPred,
							lowGradeVeneerVolPred,
							veneerVolPred);
					stand.getTrees().put(treeID, tree);
				}
			}
			standList.addAll(standMap.values());
			return standList;
		} catch (IOException e) {
			Assert.fail("Unable to load file " + filename);
			return null;
		}
	}

	
	@Test
	public void testMonteCarloPredictions() {
		Matrix meanRef = new Matrix(7,1);
		meanRef.m_afData[0][0] = 6.4262054572822125;
		meanRef.m_afData[1][0] = 358.8726980631341;
		meanRef.m_afData[2][0] = 212.9874983502981;
		meanRef.m_afData[3][0] = 145.88519971283398;
		meanRef.m_afData[4][0] = 0d;
		meanRef.m_afData[5][0] = 0d;
		meanRef.m_afData[6][0] = 0d;
		
		Matrix stdRef = new Matrix(7,1);
		stdRef.m_afData[0][0] = 0.14005679329726609;
		stdRef.m_afData[1][0] = 48.39343575836749;
		stdRef.m_afData[2][0] = 70.46323093748266;
		stdRef.m_afData[3][0] = 68.00699931625708;
		stdRef.m_afData[4][0] = 0d;
		stdRef.m_afData[5][0] = 0d;
		stdRef.m_afData[6][0] = 0d;
	
		int nbRealizations = 100000;
		List<WBirchProdVolStandImpl> stands = readStands();
		WBirchProdVolPredictor predictor = new WBirchProdVolPredictor(false, true);
		WBirchProdVolStand stand = stands.get(0);
		WBirchProdVolTree tree = ((WBirchProdVolStandImpl) stand).getTrees().values().iterator().next();
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix pred;
		for (int i = 0; i < nbRealizations; i++) {
			stand.setMonteCarloRealizationId(i);
			pred = predictor.getLogGradeVolumePredictions(stand, tree);
			estimate.addRealization(pred);
		}
		
		Matrix mean = estimate.getMean();
		Matrix relDiff = mean.subtract(meanRef).elementWiseDivide(meanRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of mean", !relDiff.anyElementLargerThan(5E-3));
		
		Matrix variance = estimate.getVariance();
		Matrix std = variance.diagonalVector().elementwisePower(0.5);
		
		relDiff = std.subtract(stdRef).elementWiseDivide(stdRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of std", !relDiff.anyElementLargerThan(5E-3));
	}

	
	
}
