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
import repicea.util.ObjectUtility;

public class WBirchProdVolPredictorTest {

	@SuppressWarnings("unused")
	@Test
	public void testFixedEffectPredictions() {
		List<WBirchProdVolStandImpl> stands = readStands();
		WBirchProdVolPredictor predictor = new WBirchProdVolPredictor(false);
		int nbTrees = 0;
		int nbMatches = 0;
		int nbMatches2 = 0;
		for (WBirchProdVolStandImpl stand : stands) {
			for (WBirchProdVolTreeImpl tree : stand.getTrees().values()) {
				Matrix pred = predictor.getLogGradeVolumePredictions(stand, tree).getMean();
				//		Matrix variances = predictor.getVMatrixForThisTree(tree);
				Matrix predRef = tree.getPredRef();
				Assert.assertEquals("Number of elements", predRef.m_iRows, pred.m_iRows);
				for (int i = 0; i < pred.m_iRows; i++) {
					Assert.assertEquals("Comparing tree " + tree.getSubjectId() + " in plot " + stand.getSubjectId(), predRef.m_afData[i][0], pred.m_afData[i][0], 1E-6);
					nbMatches2++;
				}
				if (predRef.m_iRows == 4) {
					nbMatches += 3;
				} else {
					nbMatches += 5;
				}
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

	
	
	
	
}