/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import repicea.lang.REpiceaSystem;
import repicea.serial.MarshallingException;
import repicea.serial.SerializerChangeMonitor;
import repicea.serial.UnmarshallingException;
import repicea.util.ObjectUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class XmlSerializationTest {
	
	static {
		SerializerChangeMonitor.registerClassNameChange("repicea.serial.xml.XmlSerializationTest$OriginalFakeClass", "repicea.serial.xml.XmlSerializationTest$FakeClassForSerializationTest");
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
	
	
	static class FakeClassWithStaticField extends FakeClass {
		
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


	
	@Test
	public void test01serializationOfProblematicCharacters() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test02serializationOfProblematicCharacters2() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test03serializationDeserializationOfASimpleObject() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test04serializationDeserializationOfObjectWithTransientFields() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test05serializationDeserializationOfAnSimpleObjectWithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClass ah = new FakeClass(arguments);
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
	public void test06serializationDeserializationOfObjectWithTransientFieldsWithInputStream() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		String[] arguments = new String[1];
		arguments[0] = "Test"; 
		FakeClassWithStaticField ah = new FakeClassWithStaticField(arguments);
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


	private static class FakeClassForSerializationTest {}
	
	
	@Test
	public void test07serializationOfClassObject() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		Class clazz = FakeClassForSerializationTest.class;

		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(clazz);
		
		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "serObj.xml";
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);
		
		Class deserializedClass = (Class) deserializer.readObject();
		Assert.assertEquals("Are the unique hashcodes the same?", true, clazz.equals(deserializedClass));
	}

	
	@Test
	public void test08serializationOfAFormerClassObject() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		Class clazz = FakeClassForSerializationTest.class;

		String relativePathname = ObjectUtility.getRelativePackagePath(getClass()) + "formerOriginalFakeClassObj.xml";
		XmlDeserializer deserializer = new XmlDeserializer(relativePathname);
		
		Class deserializedClass = (Class) deserializer.readObject();
		Assert.assertEquals("Are the unique hashcodes the same?", true, clazz.equals(deserializedClass));
	}
	
	
	
	
	@Test
	public void test09serializationObject() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		Object obj = new Object();
		
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(obj);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object deserializedObj = deserializer.readObject();
		
		Assert.assertTrue(deserializedObj.getClass().equals(Object.class));
	}

	@Test
	public void test10serializationOfPrimitive() throws FileNotFoundException, MarshallingException, UnmarshallingException {
		String pathname = ObjectUtility.getPackagePath(getClass()) + "serObj.xml";
		XmlSerializer serializer = new XmlSerializer(pathname);
		serializer.writeObject(4d);
		
		XmlDeserializer deserializer = new XmlDeserializer(pathname);
		Object deserializedObj = deserializer.readObject();
		
		Assert.assertTrue(deserializedObj instanceof Double);
		Assert.assertEquals("Testing values", 4d, (Double) deserializedObj, 1E-8);
	}

	@Test
	public void test11testListOfClasses() throws FileNotFoundException, MarshallingException, UnmarshallingException {
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
	public void test12serializationOfPrimitiveTypes() throws MarshallingException, UnmarshallingException {
		List<Object> myList = Arrays.asList(new Object[] {(byte) 1, (short) 2, (int) 3, (long) 4, (float) 4.2, (char) 125, (double) 4.5, true, "patate"});
		String filename1 = ObjectUtility.getPackagePath(getClass()) + "serializedPrimitives.zml";
		XmlSerializer ser1 = new XmlSerializer(filename1);
		ser1.writeObject(myList);
		XmlDeserializer deserializer = new XmlDeserializer(filename1);
		List<String> desList = (List) deserializer.readObject();
		for (int i = 0; i < myList.size(); i++) {
			Assert.assertEquals("Testing entry " + i, myList.get(i),  desList.get(i));
		}
	}

	@Test
	public void test13serializationDeserializationTime() throws MarshallingException, UnmarshallingException {
		List<Double> myList = new ArrayList<Double>();
		Random r = new Random();
		for (int i = 0; i < 1000000; i++) {
			myList.add(r.nextDouble());
		}
		String filename = ObjectUtility.getPackagePath(getClass()) + "serializationDeserializationTest.zml";
		XmlSerializer ser = new XmlSerializer(filename);
		long initTime = System.currentTimeMillis();
		ser.writeObject(myList);
		long elapsedTime = System.currentTimeMillis() - initTime;
		System.out.println("Serialization time = " + elapsedTime + " ms.");

		XmlDeserializer deser = new XmlDeserializer(filename);
		initTime = System.currentTimeMillis();
		@SuppressWarnings("unused")
		List<Double> o = (List) deser.readObject();
		elapsedTime = System.currentTimeMillis() - initTime;
		System.out.println("Deserialization time = " + elapsedTime + " ms.");

	}

	@Test
	public void test14serializationOfSpecialCharacters() throws MarshallingException, UnmarshallingException {
		List<Object> myList = Arrays.asList("tête", "épaule", "flûte");
		String filename1 = ObjectUtility.getPackagePath(getClass()) + "serializedSpecialChars.zml";
		XmlSerializer ser1 = new XmlSerializer(filename1);
		ser1.writeObject(myList);
		XmlDeserializer deserializer = new XmlDeserializer(filename1);
		List<String> desList = (List) deserializer.readObject();
		for (int i = 0; i < myList.size(); i++) {
			Assert.assertEquals("Testing entry " + i, myList.get(i),  desList.get(i));
		}
	}

	@Test
	public void test15deserializationOfInfiniteValuesUnderWeirdFormat() throws MarshallingException, UnmarshallingException {
		List<Object> myList = Arrays.asList(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		String filename1 = ObjectUtility.getPackagePath(getClass()) + "serializedSpecialDoubles.xml";
		XmlDeserializer deserializer = new XmlDeserializer(filename1);
		List<String> desList = (List) deserializer.readObject();
		for (int i = 0; i < myList.size(); i++) {
			Assert.assertEquals("Testing entry " + i, myList.get(i),  desList.get(i));
		}
	}
	
	@Test
	public void test16SerializerMemoryHeap() throws MarshallingException, UnmarshallingException {
		Random r = new Random();
		List<Double[]> collection = new ArrayList<Double[]>();
		for (int i = 0; i < 100000; i++) {
			Double[] record = new Double[10];
			for (int j = 0; j < record.length; j++) {
				record[j] = r.nextDouble();
				collection.add(record);
			}
		}
		System.out.println("Current memory load " + REpiceaSystem.getCurrentMemoryLoadMb() + " Mb.");
		String filename = ObjectUtility.getPackagePath(getClass()) + "testMemoryLoad.zml";
		System.out.println("Saving copy list...");
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(collection);
		System.out.println("Done.");
		
		
		System.out.println("Reading copy list...");
		XmlDeserializer deser = new XmlDeserializer(filename);
		List<Double[]> copyList = (List) deser.readObject();
		System.out.println("Done.");
		int ii = 0;
		for (int i = 0; i < 100; i++) {
			Double[] refRecord = collection.get(i);
			Double[] actualRecord = copyList.get(i);
			Assert.assertTrue("Testing the record is not empty" , refRecord.length > 0);
			Assert.assertEquals("Testing record length", 
					refRecord.length,
					actualRecord.length);
			for (int j = 0; j < refRecord.length; j++) {
				Assert.assertEquals("Testing record value at i=" + i + " and j=" + j,
						refRecord[j],
						actualRecord[j],
						1E-8);
				ii++;
			}
		}
		System.out.println("Tested " + ii + " double values!");
	}

	
}
