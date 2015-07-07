package repicea.treelogger.diameterbasedtreelogger;

import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.treelogger.maritimepine.MaritimePineBasicTree;

public abstract class DiameterBasedTreeLogger extends TreeLogger<DiameterBasedTreeLoggerParameters, DiameterBasedTree> {


	@Override
	protected void logThisTree(DiameterBasedTree tree) {
		List<DiameterBasedTreeLogCategory> logCategories = params.getSpeciesLogCategories(MaritimePineBasicTree.Species.MaritimePine.toString());
		DiameterBasedWoodPiece piece;
		for (DiameterBasedTreeLogCategory logCategory : logCategories) {
			piece = producePiece(tree, logCategory);
			if (piece != null) {
				addWoodPiece(tree, piece);	
			} 
		}
	}

	@Override
	public void setTreeLoggerParameters() {}

	@Override
	public abstract DiameterBasedTreeLoggerParameters createDefaultTreeLoggerParameters();
	
	@Override
	public abstract DiameterBasedTree getEligible(LoggableTree t);

	
	protected abstract DiameterBasedWoodPiece producePiece(DiameterBasedTree tree, DiameterBasedTreeLogCategory logCategory);
}

