package repicea.stats.data;

import java.util.HashMap;
import java.util.List;

class DataPatternMap extends HashMap<DataPattern, List<DataGroup>> {

	protected final DataSetGroupMap dataSetGroupMap;
	
	protected DataPatternMap(DataSetGroupMap dataSetGroupMap) {
		this.dataSetGroupMap = dataSetGroupMap;
	}

	
	
}
