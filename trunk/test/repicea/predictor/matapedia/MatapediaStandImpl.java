package repicea.predictor.matapedia;

import java.util.ArrayList;
import java.util.List;

import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public class MatapediaStandImpl implements MatapediaStand {

	private boolean upcomingSBW;
	private boolean sbwPrevious;
	private int subjectID;
	private int monteCarloRealization;
	private List<MatapediaTree> trees;
	
	public MatapediaStandImpl(boolean upcomingSBW, boolean sbwPrevious, int subjectID, int monteCarloRealization) {
		this.upcomingSBW = upcomingSBW;
		this.sbwPrevious = sbwPrevious;
		this.subjectID = subjectID;
		setMonteCarloRealizationId(monteCarloRealization);
		trees = new ArrayList<MatapediaTree>();
	}
	
	@Override
	public int getSubjectId() {
		return subjectID;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.Plot;
	}

	@Override
	public void setMonteCarloRealizationId(int i) {
		monteCarloRealization = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealization;
	}

	@Override
	public List<MatapediaTree> getMatapediaTrees() {
		return trees;
	}

	public void addTree(MatapediaTree tree) {
		trees.add(tree);
	}


	@Override
	public boolean isSBWDefoliated() {
		return sbwPrevious;
	}

	@Override
	public boolean isGoingToBeDefoliated() {
		return upcomingSBW;
	}

	@Override
	public boolean isGoingToBeSprayed() {
		return false;
	}

	@Override
	public int getDateYr() {
		return 2000;
	}

}
