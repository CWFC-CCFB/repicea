package repicea.serial.xml;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class XmlSerializationTest {
	
	static {
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.serial.xml.XmlSerializationTest$OriginalFakeClass", "repicea.serial.xml.XmlSerializationTest$FakeClassForSerializationTest");
	}
	
	
	
	private static class FakeClass {
		
		private String[] arguments;
		
		public FakeClass(String[] args) {
			arguments = args;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FakeClass) {
				FakeClass ah = (FakeClass) obj;
				if (arguments == null && ah.arguments == null) {
					return true;
				} else {
					if (arguments.length == ah.arguments.length) {
						for (int i = 0; i < arguments.length; i++) {
							if (!arguments[i].equals(ah.arguments[i])) {
								return false;
							} 
						}
						return true;
					}
				}
			}
			return false;
		}

		
	}
	
	
	private static class FakeClassWithStaticField extends FakeClass {
		
		private transient Object transientObj;
		@SuppressWarnings("unused")
		private static Object staticObj;
		private Window win;
		
		public FakeClassWithStaticField(String[] args) {
			super(args);
			transientObj = new Object();
			staticObj = new Object();
			win = new Window(null);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FakeClassWithStaticField) {
				FakeClassWithStaticField ah = (FakeClassWithStaticField) obj;
				if (ah.transientObj == null) {
					if (ah.win == null) {
						return super.equals(obj);
					}
				}
			}
			return false;
		}
	}


	private static class FakeClassWithList extends FakeClassWithStaticField {
		
		private List<String> strings;
		
		public FakeClassWithList(String[] args) {
			super(args);
			strings = new ArrayList<String>();
			for (String str : args) {
				strings.add(str);
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				if (obj instanceof FakeClassWithList) {
					FakeClassWithList ah = (FakeClassWithList) obj;
					if (ah.strings == null && strings == null) {
						return true;
					} else {
						if (strings.size() == ah.strings.size()) {
							for (int i = 0; i < strings.size(); i++) {
								if (!strings.get(i).equals(ah.strings.get(i))) {
									return false;
								} 
							}
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	@SuppressWarnings("serial")
	private static class FakeClassDerivingFromMap extends HashMap<String, Double> {
		private double fakeField;
		
		public FakeClassDerivingFromMap() {
			super();
			put("Test1", 1d);
			put("Test2", 2d);
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof FakeClassDerivingFromMap) {
				FakeClassDerivingFromMap map = (FakeClassDerivingFromMap) obj;
				if (size() == map.size()) {
					for (String key : keySet()) {
						double thisDouble = get(key);
						double thatDouble = map.get(key);
						if (thisDouble != thatDouble) {
							return false;
						}
					}
					if (fakeField != map.fakeField) {
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		
	}
	
	private static class FakeClassDerivingFromMapAndOverringPut extends HashMap<String, Double> {
		private double fakeField;
		
		public FakeClassDerivingFromMapAndOverringPut() {
			super();
			innerPut("Test1", 1d);
			innerPut("Test2", 2d);
		}
		
		private void innerPut(String key, Double value) {
			super.put(key, value);
		}
		
		@Override
		public Double put(String key, Double value) {
			return super.put(key, value);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FakeClassDerivingFromMap) {
				FakeClassDerivingFromMap map = (FakeClassDerivingFromMap) obj;
				if (size() == map.size()) {
					for (String key : keySet()) {
						double thisDouble = get(key);
						double thatDouble = map.get(key);
						if (thisDouble != thatDouble) {
							return false;
						}
					}
					if (fakeField != map.fakeField) {
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		
	}

	static class FakeClassDerivingFromListAndOverringAdd extends ArrayList<String> {
		@SuppressWarnings("unused")
		private double fakeField;
		
		public FakeClassDerivingFromListAndOverringAdd() {
			super();
			innerAdd("Test1");
			innerAdd("Test2");
		}
		
		private void innerAdd(String key) {
			super.add(key);
		}
		
		@Override
		public boolean add(String key) {
			return super.add(key);
		}
	}

	private static class FakeClassWithEmptyList {
		private List<String> list;
		
		public FakeClassWithEmptyList() {
			list = new ArrayList<String>();
		}
	}
	
	@Test
	public void serializationOfProblematicCharacters() throws FileNotFoundException, XmlMarshallException {
		String[] problematicCharacters = new String[5];
		problematicCharacters[0] = "<";
		problematicCharacters[1] = ">";
		problematicCharacters[2] = "&";
		problematicCharacters[3] = "'";
		problematicCharacters[4] = "\"";
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(problematicCharacters);

		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object copy = deserializer.readObject();
		String[] copyArray = (String[]) copy;
	
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters[0], copyArray[0]);
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters[1], copyArray[1]);
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters[2], copyArray[2]);
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters[3], copyArray[3]);
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters[4], copyArray[4]);
	}
	
	@Test
	public void serializationOfProblematicCharacters2() throws FileNotFoundException, XmlMarshallException {
		String problematicCharacters = "<>>>&1DEZF3&D";
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(problematicCharacters);

		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object copy = deserializer.readObject();
		String copyString = (String) copy;
	
		Assert.assertEquals("Is the copy equal to the original?", problematicCharacters, copyString);
	}


	
	@Test
	public void serializationDeserializationOfASimpleObject() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClass ah = new FakeClass(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfObjectWithTransientFields() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithStaticField ah = new FakeClassWithStaticField(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfObjectWithList() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithList ah = new FakeClassWithList(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfExtendedMap() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassDerivingFromMap ah = new FakeClassDerivingFromMap();
		ah.fakeField = 3d;
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfEmptyLists() throws FileNotFoundException, XmlMarshallException {
		FakeClassWithEmptyList list1 = new FakeClassWithEmptyList();
		FakeClassWithEmptyList list2 = new FakeClassWithEmptyList();
		
		List<FakeClassWithEmptyList> toSerialize = new ArrayList<FakeClassWithEmptyList>();
		toSerialize.add(list1);
		toSerialize.add(list2);

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(toSerialize);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes different?", true, System.identityHashCode(list1b.list) != System.identityHashCode(list2b.list));
	}


	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfEmptyLists2() throws FileNotFoundException, XmlMarshallException {
		FakeClassWithEmptyList list1 = new FakeClassWithEmptyList();
		FakeClassWithEmptyList list2 = new FakeClassWithEmptyList();
		list2.list = list1.list;
		
		List<FakeClassWithEmptyList> toSerialize = new ArrayList<FakeClassWithEmptyList>();
		toSerialize.add(list1);
		toSerialize.add(list2);

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(toSerialize);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes the same?", true, System.identityHashCode(list1b.list) == System.identityHashCode(list2b.list));
	}


	
	
	
	@Test
	public void serializationDeserializationOfAnSimpleObjectWithInputStream() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClass ah = new FakeClass(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);
		
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfObjectWithTransientFieldsWithInputStream() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithStaticField ah = new FakeClassWithStaticField(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);
		
		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfObjectWithListWithInputStream() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithList ah = new FakeClassWithList(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);

		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void serializationDeserializationOfExtendedMapWithInputStream() throws FileNotFoundException, XmlMarshallException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassDerivingFromMap ah = new FakeClassDerivingFromMap();
		ah.fakeField = 3d;
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);

		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfEmptyListsWithInputStream() throws FileNotFoundException, XmlMarshallException {
		FakeClassWithEmptyList list1 = new FakeClassWithEmptyList();
		FakeClassWithEmptyList list2 = new FakeClassWithEmptyList();
		
		List<FakeClassWithEmptyList> toSerialize = new ArrayList<FakeClassWithEmptyList>();
		toSerialize.add(list1);
		toSerialize.add(list2);

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(toSerialize);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);

		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes different?", true, System.identityHashCode(list1b.list) != System.identityHashCode(list2b.list));
	}


	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfEmptyLists2WithInputStream() throws FileNotFoundException, XmlMarshallException {
		FakeClassWithEmptyList list1 = new FakeClassWithEmptyList();
		FakeClassWithEmptyList list2 = new FakeClassWithEmptyList();
		list2.list = list1.list;
		
		List<FakeClassWithEmptyList> toSerialize = new ArrayList<FakeClassWithEmptyList>();
		toSerialize.add(list1);
		toSerialize.add(list2);

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(toSerialize);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);
		
		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes the same?", true, System.identityHashCode(list1b.list) == System.identityHashCode(list2b.list));
	}


	
	private static class FakeClassForSerializationTest {}
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfClassObject() throws FileNotFoundException, XmlMarshallException {
		Class clazz = FakeClassForSerializationTest.class;

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(clazz);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);
		
		Class deserializedClass = (Class) deserializer.readObject();
		Assert.assertEquals("Are the unique hashcodes the same?", true, clazz.equals(deserializedClass));
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void serializationOfAFormerClassObject() throws FileNotFoundException, XmlMarshallException {
		Class clazz = FakeClassForSerializationTest.class;

		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "formerOriginalFakeClassObj.xml";
//		InputStream is = ClassLoader.getSystemResourceAsStream(relativePathname);
		InputStream is = getClass().getResourceAsStream("/" + relativePathname);
		XmlDeserializer deserializer = new XmlDeserializer(is);
		
		Class deserializedClass = (Class) deserializer.readObject();
		Assert.assertEquals("Are the unique hashcodes the same?", true, clazz.equals(deserializedClass));
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void serializationHashMap() throws FileNotFoundException, XmlMarshallException {
		HashMap<Integer, Double> originalHashMap = new HashMap<Integer, Double>();
		originalHashMap.put(1, 2d);
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalHashMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		HashMap<Integer, Double> deserializedMap = (HashMap) deserializer.readObject();
		
		Assert.assertTrue(deserializedMap.size() == originalHashMap.size());
		Assert.assertEquals("hashMap value", originalHashMap.get(1), deserializedMap.get(1), 1E-8);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void serializationConcurrentHashMap() throws FileNotFoundException, XmlMarshallException {
		ConcurrentHashMap<Integer, Double> originalConcurrentHashMap = new ConcurrentHashMap<Integer, Double>();
		originalConcurrentHashMap.put(1, 2d);
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalConcurrentHashMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		ConcurrentHashMap<Integer, Double> deserializedMap = (ConcurrentHashMap) deserializer.readObject();
		
		Assert.assertTrue(deserializedMap.size() == originalConcurrentHashMap.size());
		Assert.assertEquals("hashMap value", originalConcurrentHashMap.get(1), deserializedMap.get(1), 1E-8);
	}
	
	@Test
	public void serializationObject() throws FileNotFoundException, XmlMarshallException {
		Object obj = new Object();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(obj);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object deserializedObj = deserializer.readObject();
		
		Assert.assertTrue(deserializedObj.getClass().equals(Object.class));
	}

	@Test
	public void serializationOfPrimitive() throws FileNotFoundException, XmlMarshallException {
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(4d);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object deserializedObj = deserializer.readObject();
		
		Assert.assertTrue(deserializedObj instanceof Double);
		Assert.assertEquals("Testing values", 4d, (Double) deserializedObj, 1E-8);
	}

	@Test
	public void testListOfClasses() throws FileNotFoundException, XmlMarshallException {
		List<Class> myClasses = new ArrayList<Class>();
		myClasses.add(Double.class);
		myClasses.add(Double.class);
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(myClasses);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List deserializedObj = (List) deserializer.readObject();
		
		Assert.assertEquals("Testing list size", myClasses.size(), deserializedObj.size());
		Assert.assertEquals("Testing value 0", deserializedObj.get(0), Double.class);
		Assert.assertEquals("Testing value 1", deserializedObj.get(1), Double.class);
	}

	@Test
	public void testListOfArrays() throws FileNotFoundException, XmlMarshallException {
		List myClasses = new ArrayList();
		int[] myArray = new int[1];
		myArray[0] = 18;
		myClasses.add(myArray);
		myClasses.add(myArray);
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(myClasses);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List deserializedObj = (List) deserializer.readObject();
		
		Assert.assertEquals("Testing list size", myClasses.size(), deserializedObj.size());
		Assert.assertEquals("Testing list size", Array.get(deserializedObj.get(0),0), 18);
		Assert.assertEquals("Testing list size", Array.get(deserializedObj.get(1),0), 18);
		
	}

	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void deserializationOfHashMapInJava7() throws FileNotFoundException, XmlMarshallException {
		
		System.out.println("Java version : " + System.getProperty("java.version"));
		
		HashMap<Integer, Map> originalHashMap = new HashMap<Integer, Map>();
		originalHashMap.put(1, new HashMap<Integer, Double>());
		originalHashMap.put(2, new HashMap<Integer, Double>());
		originalHashMap.put(3, new HashMap<Integer, Double>());
		int j = 1;
		for (int i = 1; i <= 3; i++) {
			Map<Integer, Double> innerMap = originalHashMap.get(i);
			for (double d = 1; d <= 3; d++) {
				innerMap.put(j++, Math.pow(d, i));
			}
		}

		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedHashMapJava7.xml";
//		XmlSerializer serializer = new XmlSerializer(pathname);
//		serializer.writeObject(originalHashMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		HashMap deserializedMap = (HashMap) deserializer.readObject();
		
		for (int i = 1; i <= 3; i++) {
			Map<Integer, Double> innerMap = originalHashMap.get(i);
			Map<Integer, Double> serializedInnerMap = (HashMap) deserializedMap.get(i);
			for (Integer k : innerMap.keySet()) {
				Assert.assertEquals("Comparing values", innerMap.get(k), serializedInnerMap.get(k), 1E-8);
			}
		}
	}
	
	
	@Test
	public void deserializationOfDerivedHashMapClassWithOverridenPutMethod() throws FileNotFoundException, XmlMarshallException {
		
		Map<String, Double> originalHashMap = new FakeClassDerivingFromMapAndOverringPut();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedDerivedHashMapWithOverridenPutMethod.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalHashMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		FakeClassDerivingFromMapAndOverringPut deserializedMap = (FakeClassDerivingFromMapAndOverringPut) deserializer.readObject();

		Assert.assertEquals("Testing deserialized hash map size", 2, deserializedMap.size());
	}

	@Test
	public void deserializationOfDerivedArrayListClassWithOverridenAddMethod() throws FileNotFoundException, XmlMarshallException {
		
		List<String> originalList = new FakeClassDerivingFromListAndOverringAdd();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedDerivedArrayListWithOverridenAddMethod.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalList);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List<String> deserializedList = (FakeClassDerivingFromListAndOverringAdd) deserializer.readObject();

		Assert.assertEquals("Testing deserialized array list size", 2, deserializedList.size());
	}

	/*
	 * Just to make sure the error message is displayed only once.
	 */
	@Test
	public void deserializationOfDerivedArrayListClassWithOverridenAddMethod2() throws FileNotFoundException, XmlMarshallException {
		
		List<String> originalList = new FakeClassDerivingFromListAndOverringAdd();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedDerivedArrayListWithOverridenAddMethod.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalList);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List<String> deserializedList = (FakeClassDerivingFromListAndOverringAdd) deserializer.readObject();

		Assert.assertEquals("Testing deserialized array list size", 2, deserializedList.size());
	}
	
}
