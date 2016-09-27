package repicea.predictor.wbirchloggrades;

import java.util.ArrayList;
import java.util.List;

class Plot {

	private final List<WBirchLogGradesTreeImpl> trees;
	private final int id;
	
	Plot(int id) {
		this.id = id;
		trees = new ArrayList<WBirchLogGradesTreeImpl>();
	}
	
	void addTree(WBirchLogGradesTreeImpl tree) {
		trees.add(tree);
	}
	
	List<WBirchLogGradesTreeImpl> getTrees() {
		return trees;
	}
	
	@Override
	public String toString() {
		return "Plot " + id;
	}
}
