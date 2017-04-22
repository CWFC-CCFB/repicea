package repicea.predictor.wbirchloggrades;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.predictor.wbirchloggrades.WBirchLogGradesPredictor;
import repicea.predictor.wbirchloggrades.WBirchLogGradesStand;
import repicea.predictor.wbirchloggrades.WBirchLogGradesTree;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;
import repicea.util.ObjectUtility;

public class WBirchLogGradesPredictorTest {

	@SuppressWarnings("unused")
	@Test
	public void testFixedEffectPredictions() {
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(false);
		predictor.isTestPurpose = true;
		int nbTrees = 0;
		int nbMatches = 0;
		int nbMatches2 = 0;
		for (WBirchLogGradesStandImpl stand : stands.values()) {
			for (WBirchLogGradesTreeImpl tree : stand.getTrees().values()) {
				if (tree.getSubjectId().equals("113")) {
					int u = 0;
				}
				Matrix pred = predictor.getLogGradeVolumePredictions(stand, tree);
				//		Matrix variances = predictor.getVMatrixForThisTree(tree);
				Matrix predRef = tree.getRealizedValues();
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
	
	public static Map<String, WBirchLogGradesStandImpl> readStands() {
//		List<WBirchProdVolStandImpl> standList = new ArrayList<WBirchProdVolStandImpl>();
		String filename = ObjectUtility.getPackagePath(WBirchLogGradesPredictorTest.class) + "pred-simul.csv";
		Map<String, WBirchLogGradesStandImpl> standMap = new HashMap<String, WBirchLogGradesStandImpl>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
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
			WBirchLogGradesStandImpl stand;
			while ((record = reader.nextRecord()) != null) {
				plotID = record[0].toString().trim();
				treeID = Integer.parseInt(record[1].toString());
				quality = record[2].toString().trim().toUpperCase();
				dbhCm = Double.parseDouble(record[4].toString());
				elevation = Double.parseDouble(record[14].toString()); 
				h20Obs = Double.parseDouble(record[5].toString());
				h20Pred = Double.parseDouble(record[18].toString());
				merVolPred = Double.parseDouble(record[19].toString()) * .001;
				pulpVolPred = Double.parseDouble(record[20].toString()) * .001;
				lowGradeSawlogVolPred = Double.parseDouble(record[21].toString()) * .001;
				sawlogVolPred = Double.parseDouble(record[22].toString()) * .001;
				lowGradeVeneerVolPred = Double.parseDouble(record[23].toString()) * .001;		
				veneerVolPred = Double.parseDouble(record[24].toString()) * .001;		

				if (!standMap.containsKey(plotID)) {
					standMap.put(plotID, new WBirchLogGradesStandImpl(plotID, elevation));
				}
				Matrix predRef = new Matrix(7,1);
				predRef.m_afData[0][0] = h20Pred;
				predRef.m_afData[1][0] = merVolPred;
				predRef.m_afData[2][0] = pulpVolPred;
				predRef.m_afData[3][0] = sawlogVolPred;
				predRef.m_afData[4][0] = lowGradeVeneerVolPred;
				predRef.m_afData[5][0] = veneerVolPred;
				predRef.m_afData[6][0] = lowGradeSawlogVolPred;

				stand = standMap.get(plotID);
				if (!stand.getTrees().containsKey(treeID)) {
					new WBirchLogGradesTreeImpl(treeID, quality, dbhCm, stand, h20Obs, predRef);
				}
			}
//			standList.addAll(standMap.values());
//			return standList;
			return standMap;
		} catch (IOException e) {
			Assert.fail("Unable to load file " + filename);
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	
	@Test
	public void testMonteCarloPredictions() {
		Matrix meanRef = new Matrix(7,1);
		meanRef.m_afData[0][0] = 6.4262054572822125;
		meanRef.m_afData[1][0] = 358.8726980631341 * .001;
		meanRef.m_afData[2][0] = 212.9874983502981 * .001;
		meanRef.m_afData[3][0] = 145.88519971283398 * .001;
		meanRef.m_afData[4][0] = 0d;
		meanRef.m_afData[5][0] = 0d;
		meanRef.m_afData[6][0] = 0d;
		
		Matrix stdRef = new Matrix(7,1);
		stdRef.m_afData[0][0] = 0.13280569488806207;
		stdRef.m_afData[1][0] = 0.048334578844355075;
		stdRef.m_afData[2][0] = 0.07043375186265777;
		stdRef.m_afData[3][0] = 0.0679108872730103; 
		stdRef.m_afData[4][0] = 0d;
		stdRef.m_afData[5][0] = 0d;
		stdRef.m_afData[6][0] = 0d;
	
		int nbRealizations = 100000;
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(false, true);
		WBirchLogGradesStand stand = stands.get("49");
		WBirchLogGradesTree tree = ((WBirchLogGradesStandImpl) stand).getTrees().get(2);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix pred;
		for (int i = 0; i < nbRealizations; i++) {
			((WBirchLogGradesStandImpl) stand).setMonteCarloRealizationId(i);
			pred = predictor.getLogGradeVolumePredictions(stand, tree);
			estimate.addRealization(pred);
		}
		
		Matrix mean = estimate.getMean();
		Matrix relDiff = mean.subtract(meanRef).elementWiseDivide(meanRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of mean", !relDiff.anyElementLargerThan(5E-3));
		
		Matrix variance = estimate.getVariance();
		Matrix std = variance.diagonalVector().elementWisePower(0.5);
		
		relDiff = std.subtract(stdRef).elementWiseDivide(stdRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of std", !relDiff.anyElementLargerThan(1E-2));
	}

	
	// Unchecked since the switch to version Java 8
	public void testMonteCarloPredictions2() throws IOException {
		String filePath = ObjectUtility.getPackagePath(getClass()) + "MCSimul.csv";
		int nbRealizations = 10000;
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		WBirchLogGradesStandImpl stand = stands.get(0);
		for (int i = 0; i < nbRealizations; i++) {
			Matrix sumProd = new Matrix(7,1);
			stand.setMonteCarloRealizationId(i);
			for (WBirchLogGradesTree tree : stand.getTrees().values()) {
				sumProd = sumProd.add(predictor.getLogGradeVolumePredictions(stand, tree).scalarMultiply(0.001));
			}
			estimate.addRealization(sumProd);
		}
		CSVWriter writer = new CSVWriter(new File(filePath), false);
		
		
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("h20"));
		fields.add(new CSVField("merch"));
		fields.add(new CSVField(ProductID.PulpAndPaper.name()));
		fields.add(new CSVField(ProductID.Sawlog.name()));
		fields.add(new CSVField(ProductID.LowGradeVeneer.name()));
		fields.add(new CSVField(ProductID.Veneer.name()));
		fields.add(new CSVField(ProductID.LowGradeSawlog.name()));
		
		writer.setFields(fields);
		Object[] record;
		for (Matrix mat : estimate.getRealizations()) {
			record = new Object[mat.m_iRows];
			for (int i = 0; i < mat.m_iRows; i++) {
				record[i] = mat.m_afData[i][0];
			}
			writer.addRecord(record);
		}
		writer.close();
		
	}

	
}
