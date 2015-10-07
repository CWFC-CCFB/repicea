package repicea.treelogger.wbirchprodvol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;

public class WBirchProdVolTreeLoggerTest {

	@Test
	public void testTreeLoggerWithDeterministicPred() {
		List<WBirchProdVolStandImpl> stands = WBirchProdVolPredictorTest.readStands();
		WBirchProdVolTreeLogger treeLogger = new WBirchProdVolTreeLogger(false, false);
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection<WBirchProdVolLoggableTree> trees = new ArrayList<WBirchProdVolLoggableTree>();
		
		for (WBirchProdVolStandImpl stand : stands) {
			trees.addAll(stand.getTrees().values());
		}
		
		treeLogger.init(trees);
		treeLogger.run();
		
		Map<String, Double> refMap = new HashMap<String, Double>();
		for (Collection<WoodPiece> woodPieces : treeLogger.getWoodPieces().values()) {
			for (WoodPiece piece : woodPieces) {
				String name = piece.getLogCategory().getName();
				if (!refMap.containsKey(name)) {
					refMap.put(name, 0d);
				}
				refMap.put(name, refMap.get(name) + piece.getVolumeM3());
			}
		}

		Matrix pred = new Matrix(7,1);
		WBirchProdVolPredictor predictor = new WBirchProdVolPredictor(false, false);
		for (WBirchProdVolLoggableTree tree : trees) {
			if (treeLogger.getEligible(tree) != null) {
				pred = pred.add(predictor.getLogGradeVolumePredictions(tree.getStand(), tree));
			}
		}
		
		double observed;
		double expected;
		for (ProductID product : ProductID.values()) {
			observed = refMap.get(product.toString());
			expected = pred.m_afData[product.getIndex()][0] * .001;
			Assert.assertEquals("Comparing product " + product.name(), expected, observed, 1E-6);
		}
	}
	
}