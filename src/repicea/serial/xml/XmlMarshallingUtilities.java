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

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.lang.reflect.ReflectUtility;
import repicea.util.REpiceaSystem;

/**
 * The XmlMarshallingUtilities class provides static methods for marshalling and unmarshalling.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("rawtypes")
public class XmlMarshallingUtilities {

	protected static Class[] boundedClasses;
	static {
		List<Class> classes = new ArrayList<Class>();
		classes.add(XmlEntry.class);
		classes.add(XmlList.class);
		boundedClasses = classes.toArray(new Class[]{});
	}

		
	@SuppressWarnings("serial")
	static final class FakeList extends ArrayList {}
	
	
	/**
	 * This method drops all the component, static or transient fields. If the mother class is a Collection or
	 * a Map then the transient fields are allowed.
	 * @param fields the original list of fields
	 * @return a List of fields
	 */
	private static List<Field> dropOutStaticAndComponentFields(Class<?> motherClazz, List<Field> fields) {
		List<Field> selectedFields = new ArrayList<Field>();
		for (Field field : fields) {
			int fieldModifier = field.getModifiers();
			if (!Modifier.isStatic(fieldModifier)) { 
				field.setAccessible(true);
				Class<?> clazz = field.getType();
				if (!Component.class.isAssignableFrom(clazz)) {
					selectedFields.add(field);
				}
			}
		}
		return selectedFields;
	}
	
	
	
	private static List<Field> retrieveAllNonStaticFieldsFromClass(Class<?> clazz) {
		List<Field> fields = ReflectUtility.retrieveAllFieldsFromClass(clazz);
		return dropOutStaticAndComponentFields(clazz, fields);
	}

	
	static List<Field> retrieveAllNonStaticAndNonTransientFieldFromClass(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		for (Field field : retrieveAllNonStaticFieldsFromClass(clazz)){
			if (!Modifier.isTransient(field.getModifiers())) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	
	static Map<String, Field> getFieldMapFromClass(Class<?> clazz) {
		List<Field> fields = retrieveAllNonStaticFieldsFromClass(clazz);
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		for (Field field : fields) {
			fieldMap.put(field.getName(), field);
		}
		return fieldMap;
	}
	
	/**
	 * This method retrieves the closest constructor with no argument in this class.
	 * @param clazz the Class instance
	 * @return a Contructor instance
	 */
	static Constructor<?> getEmptyConstructor(Class<?> clazz) {
		do {
			try {
				Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[]{});
				return constructor;
			} catch (Exception e) {}
		} while ((clazz = clazz.getSuperclass()) != null);
		return null;
	}

	static Class<?> getClass(XmlList xmlList) throws ClassNotFoundException {
		if (xmlList.isPrimitive) {
			return ReflectUtility.PrimitiveTypeMap.get(xmlList.className);
		} else {
			return Class.forName(getClassName(xmlList.className));
		}
	}
	
	/**
	 * This method returns the new class name if it has been changed.
	 * @param originalClassName
	 * @return a String
	 */
	public static String getClassName(String originalClassName) {
		String className = originalClassName;
		String changedName = XmlSerializerChangeMonitor.ClassNameChangeMap.get(className);
		if (changedName != null) {
			className = changedName;
		} 
		return className;
	}
	
	/**
	 * This method returns the new enum name if it has been changed.
	 * @param originalEnumName
	 * @return a String
	 */
	protected static String getEnumName(String originalEnumName) {
		String enumName = originalEnumName;
		String changedName = XmlSerializerChangeMonitor.EnumNameChangeMap.get(enumName);
		if (changedName != null) {
			enumName = changedName;
		} 
		return enumName;
	}

	
	/**
	 * This method creates a deep clone of an object. Transient fields and graphics components
	 * are not serialized though.
	 * @param obj an Object instance
	 * @return a deep copy of the object
	 * @throws Exception
	 */
	public static Object createDeepCopyOf(Object obj) throws Exception {
		XmlMarshaller xmlMarshaller = new XmlMarshaller();
		XmlList marshalledObject = xmlMarshaller.marshall(obj);
		XmlUnmarshaller xmlUnmarshaller = new XmlUnmarshaller();
		Object objCopy = xmlUnmarshaller.unmarshall(marshalledObject);
		return objCopy;
	}
	

	/**
	 * This method marshalles and compares two instances. If the two instances have the same values in their parameters, the method returns true. 
	 * If both objects are null, the method returns true. 
	 * @param obj1
	 * @param obj2
	 * @return a boolean
	 */
	public static boolean areTheseTwoObjectsComparable(Object obj1, Object obj2) {
		return new XmlMarshallComparator().compareTheseTwoObjects(obj1, obj2);
	}
	
	/**
	 * This method returns true if the object is either a String or a simple Object instance
	 * @param obj the instance to be checked
	 * @return a boolean
	 */
	static boolean isStringOrSimpleObject(Object obj) {
		return obj.getClass().equals(String.class) || obj.getClass().getSuperclass() == null;
	}
	
	static XmlList getNextEntryFromJava7MapEntry(XmlList list) {
		if (list.className.equals("java.util.HashMap$Entry") && REpiceaSystem.isCurrentJVMGreaterThanThisVersion("1.7")) {
			for (XmlEntry entry : list.list) {
				if (entry.fieldName.equals("next") && entry.value instanceof XmlList) {
					return (XmlList) entry.value;
				}
			}
		} 
		return null;
	}
	
}
