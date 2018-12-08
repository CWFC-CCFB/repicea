package repicea.predictor.matapedia;

import java.util.ArrayList;
import java.util.List;

public class MatapediaStandImpl implements MatapediaStand {

	private boolean upcomingSBW;
	private boolean sbwPrevious;
	private String subjectID;
	private int monteCarloRealization;
	private List<MatapediaTree> trees;
	
	public MatapediaStandImpl(boolean upcomingSBW, boolean sbwPrevious, int subjectID, int monteCarloRealization) {
		this.upcomingSBW = upcomingSBW;
		this.sbwPrevious = sbwPrevious;
		this.subjectID = ((Integer) subjectID).toString();
		setMonteCarloRealizationId(monteCarloRealization);
		trees = new ArrayList<MatapediaTree>();
	}
	
	@Override
	public String getSubjectId() {
		return subjectID;
	}

	protected void setMonteCarloRealizationId(int i) {
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
