package repicea.stats.data;

import java.util.List;

class DataHomogeneousSequence extends DataSequence {

	
	DataHomogeneousSequence(String name, ActionOnPattern action) {
		super("", false, Mode.Partial, action);
	}

	@Override
	protected Object doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
		return null;
	}
	
}
