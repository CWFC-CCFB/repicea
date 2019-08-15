package repicea.stats.data;

import java.util.HashMap;
import java.util.List;

public class DataPatternMap extends HashMap<DataPattern, List<DataGroup>> {

	protected final DataSetGroupMap dataSetGroupMap;
	
	protected DataPatternMap(DataSetGroupMap dataSetGroupMap) {
		this.dataSetGroupMap = dataSetGroupMap;
	}

//	/*
//	 * Make sure the key set is disconnected from the map to avoid concurrent changes.
//	 */
//	@Override
//	public Set<DataPattern> keySet() {
//		Set<DataPattern> dataPatterns = new HashSet<DataPattern>();
//		dataPatterns.addAll(super.keySet());
//		return dataPatterns;
//	}
	
	
}
