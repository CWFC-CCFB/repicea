package repicea.treelogger.europeanbeech;

import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters.Grade;

@SuppressWarnings("serial")
public class EuropeanBeechBasicTreeLogCategory extends DiameterBasedTreeLogCategory {

	
	/**
	 * Constructor.
	 * @param str the name of the category
	 * @param species the species name
	 * @param merchantableVolumeProportion the proportion of the merchantable volume that falls into this category
	 */
	protected EuropeanBeechBasicTreeLogCategory(Grade logGrade, String species, double smallEndDiameter) {
		super(logGrade, species, smallEndDiameter);
	}

}
