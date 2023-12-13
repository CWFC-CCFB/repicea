package repicea.serial.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import repicea.serial.MarshallingException;
import repicea.util.ObjectUtility;

public class JSONSerializationTestOLD {

	@Test
	public void simpleTest() throws MarshallingException, ReflectiveOperationException {
		Map<String, Object> myMap = new LinkedHashMap<String, Object>();
		myMap.put("entry1", "patate");
		myMap.put("entry2", "carotte");
		List<String> stringList = new ArrayList<String>();
		myMap.put("entry3", stringList);
		stringList.add("Saturne");
		stringList.add("Uranus");
		stringList.add("Neptune");
		JSONSerializer serializer = new JSONSerializer(ObjectUtility.getPackagePath(getClass()) + "testJSON.json", false);
		serializer.writeObject(myMap);
		
		int u = 0;
	}
}
