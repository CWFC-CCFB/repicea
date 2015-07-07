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
	public void TestWithSimpleTreeWithStandardDeviation() {
		MaritimePineBasicTreeLogger treeLogger = new MaritimePineBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<MaritimePineTree>();
		MaritimePineTree tree = new MaritimePineTree(30,10);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		double sum = 0;
		for (WoodPiece piece : treeLogger.getWoodPieces().get(tree)) {
			double volumeM3 = piece.getVolumeM3();
			sum += volumeM3;
		}
		Assert.assertEquals("Comparing bole volume", 1d, sum, 1E-8); 
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void TestWithSimpleTreeWithNoStandardDeviation() {
		MaritimePineBasicTreeLogger treeLogger = new MaritimePineBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<MaritimePineTree>();
		MaritimePineTree tree = new MaritimePineTree(29,0);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
		Assert.assertTrue(woodPieces.size() == 1);
		WoodPiece woodPiece = woodPieces.iterator().next();
		MaritimePineBasicTreeLogCategory logCategory = (MaritimePineBasicTreeLogCategory) woodPiece.getLogCategory();
		Assert.assertTrue(logCategory.getGrade() == Grade.SawlogLowQuality);
	}

}
