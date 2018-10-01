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
		Collection trees = new ArrayList<MaritimePineBasicLoggableTreeImpl>();
		MaritimePineBasicLoggableTreeImpl tree = new MaritimePineBasicLoggableTreeImpl(30,10,0,0);
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
	public void TestWithSimpleTreeWithStandardDeviationBelowDiameterLimitOfLargeLumberWood() {
		MaritimePineBasicTreeLogger treeLogger = new MaritimePineBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<MaritimePineBasicLoggableTreeImpl>();
		MaritimePineBasicLoggableTreeImpl tree = new MaritimePineBasicLoggableTreeImpl(24,10,0,0);
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
		Collection trees = new ArrayList<MaritimePineBasicLoggableTreeImpl>();
		MaritimePineBasicLoggableTreeImpl tree = new MaritimePineBasicLoggableTreeImpl(29,0,0,0);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
		Assert.assertTrue(woodPieces.size() == 1);
		WoodPiece woodPiece = woodPieces.iterator().next();
		MaritimePineBasicTreeLogCategory logCategory = (MaritimePineBasicTreeLogCategory) woodPiece.getLogCategory();
		Assert.assertTrue(logCategory.getGrade() == Grade.SawlogLowQuality);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void TestWithStumpBranchAndStandardDeviation() {
		MaritimePineBasicTreeLogger treeLogger = new MaritimePineBasicTreeLogger();
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection trees = new ArrayList<MaritimePineBasicLoggableTreeImpl>();
		MaritimePineBasicLoggableTreeImpl tree = new MaritimePineBasicLoggableTreeImpl(30,10,0.5,0.75);
		trees.add(tree);
		treeLogger.init(trees);
		treeLogger.run();
		double sum = 0;
		for (WoodPiece piece : treeLogger.getWoodPieces().get(tree)) {
			double volumeM3 = piece.getVolumeM3();
			sum += volumeM3;
		}
		Assert.assertEquals("Comparing bole volume", 1d + .5 + .75, sum, 1E-8); 
	}

}
