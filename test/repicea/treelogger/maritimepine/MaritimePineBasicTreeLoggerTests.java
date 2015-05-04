package repicea.treelogger.maritimepine;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.Grade;

public class MaritimePineBasicTreeLoggerTests {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void TestWithSimpleTree() {
		MaritimePineBasicTreeLogger treeLogger = new MaritimePineBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<MaritimePineTree>();
		MaritimePineTree tree = new MaritimePineTree();
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		double sum = 0;
		for (WoodPiece piece : treeLogger.getWoodPieces().get(tree)) {
			MaritimePineBasicTreeLogCategory logCategory = (MaritimePineBasicTreeLogCategory) piece.getLogCategory();
			double volumeM3 = piece.getVolumeM3();
			if (logCategory.logGrade == Grade.Stump) {
				Assert.assertEquals("Comparing stump volume", 3d, volumeM3, 1E-8); 
			} else if (logCategory.logGrade == Grade.Crown) {
				Assert.assertEquals("Comparing crown volume", 2d, volumeM3, 1E-8); 
			} else {
				sum += volumeM3;
			}
		}
		Assert.assertEquals("Comparing bole volume", 1d, sum, 1E-8); 
	}
}
