package repicea.treelogger.maritimepine;

import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;

@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategory extends DiameterBasedTreeLogCategory {

	protected MaritimePineBasicTreeLogCategory(Enum<?> logGrade, String species, double smallEndDiameter) {
		super(logGrade, species, smallEndDiameter);
	}

}
