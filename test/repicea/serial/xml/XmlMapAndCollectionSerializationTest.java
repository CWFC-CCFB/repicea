/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import repicea.serial.MarshallingException;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlSerializationTest.FakeClassWithStaticField;
import repicea.util.ObjectUtility;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({ "rawtypes", "unchecked" , "serial" })
public class XmlMapAndCollectionSerializationTest {

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
	public void test01serializationDeserializationOfObjectWithList() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test02serializationDeserializationOfExtendedMap() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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

	
	@Test
	public void test03serializationOfEmptyLists() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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


	@Test
	public void test04serializationOfEmptyLists2() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test05serializationDeserializationOfObjectWithListWithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithList ah = new FakeClassWithList(arguments);
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);

		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	@Test
	public void test06serializationDeserializationOfExtendedMapWithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassDerivingFromMap ah = new FakeClassDerivingFromMap();
		ah.fakeField = 3d;
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(ah);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);

		Object ahCopy = deserializer.readObject();
		
		new File(pathname).delete();
		
		boolean isEqual = ah.equals(ahCopy);
		Assert.assertEquals("Is the copy equal to the original?", true, isEqual);
	}

	
	@Test
	public void test07serializationOfEmptyListsWithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		FakeClassWithEmptyList list1 = new FakeClassWithEmptyList();
		FakeClassWithEmptyList list2 = new FakeClassWithEmptyList();
		
		List<FakeClassWithEmptyList> toSerialize = new ArrayList<FakeClassWithEmptyList>();
		toSerialize.add(list1);
		toSerialize.add(list2);

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(toSerialize);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);

		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes different?", true, System.identityHashCode(list1b.list) != System.identityHashCode(list2b.list));
	}


	@Test
	public void test08serializationOfEmptyLists2WithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);
		
		List ahCopy = (List) deserializer.readObject();
		FakeClassWithEmptyList list1b = (FakeClassWithEmptyList) ahCopy.get(0);
		FakeClassWithEmptyList list2b = (FakeClassWithEmptyList) ahCopy.get(1);
		Assert.assertEquals("Are the unique hashcodes the same?", true, System.identityHashCode(list1b.list) == System.identityHashCode(list2b.list));
	}

	@Test
	public void test09serializationHashMap() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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

	@Test
	public void test10serializationConcurrentHashMap() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test11ListOfArrays() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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

	@Test
	public void test12deserializationOfHashMapInJava7() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		
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
	public void test13deserializationOfDerivedHashMapClassWithOverridenPutMethod() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		
		Map<String, Double> originalHashMap = new FakeClassDerivingFromMapAndOverringPut();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedDerivedHashMapWithOverridenPutMethod.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalHashMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		FakeClassDerivingFromMapAndOverringPut deserializedMap = (FakeClassDerivingFromMapAndOverringPut) deserializer.readObject();

		Assert.assertEquals("Testing deserialized hash map size", 2, deserializedMap.size());
	}

	@Test
	public void test14deserializationOfDerivedArrayListClassWithOverridenAddMethod() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		
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
	public void test15deserializationOfDerivedArrayListClassWithOverridenAddMethod2() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		
		List<String> originalList = new FakeClassDerivingFromListAndOverringAdd();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serializedDerivedArrayListWithOverridenAddMethod.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(originalList);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		List<String> deserializedList = (FakeClassDerivingFromListAndOverringAdd) deserializer.readObject();

		Assert.assertEquals("Testing deserialized array list size", 2, deserializedList.size());
	}
		
	@Test
	public void test16serializationLinkedHashMap() throws MarshallingException, UnmarshallingException {
		Map<String, Double> linkedHashMap = new LinkedHashMap<String, Double>();
		linkedHashMap.put("entry1", 2d);
		linkedHashMap.put("entry2", 4d);
		String filename1 = ObjectUtility.getPackagePath(getClass()) + "serializedHashMap.zml";
		XmlSerializer ser1 = new XmlSerializer(filename1);
		ser1.writeObject(linkedHashMap);
		XmlDeserializer deserializer = new XmlDeserializer(filename1);
		Map<String,Double> desLinkedHashMap = (LinkedHashMap) deserializer.readObject();
		Assert.assertEquals("Testing entry1", linkedHashMap.get("entry1"),  desLinkedHashMap.get("entry1"), 1E-8);
		Assert.assertEquals("Testing entry2", linkedHashMap.get("entry2"),  desLinkedHashMap.get("entry2"), 1E-8);
	}

	@Test
	public void test17serializationArraysArrayList() throws MarshallingException, UnmarshallingException {
		List<String> myList = Arrays.asList(new String[] {"patate", "chou", "carotte"});
		String filename1 = ObjectUtility.getPackagePath(getClass()) + "serializedArraysArrayList.zml";
		XmlSerializer ser1 = new XmlSerializer(filename1);
		ser1.writeObject(myList);
		XmlDeserializer deserializer = new XmlDeserializer(filename1);
		List<String> desList = (List) deserializer.readObject();
		for (int i = 0; i < myList.size(); i++) {
			Assert.assertEquals("Testing entry " + i, myList.get(i),  desList.get(i));
		}
	}

	
	static class FakeTreeLoggerParameters {
		Map<String, List<Double>> selectedDouble;
	}
	
	
	@Test
	public void test18TestConcurrentExceptionOnMap() throws MarshallingException, UnmarshallingException {
		
		FakeTreeLoggerParameters logParams = new FakeTreeLoggerParameters();
		logParams.selectedDouble = new HashMap<String, List<Double>>();
		logParams.selectedDouble.put("species1", Arrays.asList(new Double[] {2.0,3.4,5.2}));
		logParams.selectedDouble.put("species2", Arrays.asList(new Double[] {1.0,-3.4}));

		String filename = ObjectUtility.getPackagePath(getClass()) + "testFakeTreeLogger.zml";
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(logParams);
		
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		FakeTreeLoggerParameters logParamsCopy = (FakeTreeLoggerParameters) deserializer.readObject();
		for (String k : logParamsCopy.selectedDouble.keySet()) {
			for (Double d : logParamsCopy.selectedDouble.get(k)) {
				int u = 0;
			}
		}
	}
	
}
