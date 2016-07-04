package repicea.treelogger.maritimepine;

import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;

@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategory extends DiameterBasedTreeLogCategory {

	protected MaritimePineBasicTreeLogCategory(Enum<?> logGrade, String species, double smallEndDiameter, boolean isFromStump) {
		super(logGrade, species, smallEndDiameter, isFromStump);
	}

}
