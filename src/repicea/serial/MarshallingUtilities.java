/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2023 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.lang.REpiceaSystem;

/**
 * A class of static methods for marshalling.
 * @author Mathieu Fortin - 2012
 */
public class MarshallingUtilities {

	/**
	 * This method drops all the component, static or transient fields. 
	 * @param fields the original list of fields
	 * @return a List of fields
	 */
	private static List<Field> dropOutStaticTransientAndComponentFields(List<Field> fields) {
		List<Field> selectedFields = new ArrayList<Field>();
		for (Field field : fields) {
			int fieldModifier = field.getModifiers();
			if (!Modifier.isStatic(fieldModifier) && !Modifier.isTransient(field.getModifiers())) { 
				Class<?> clazz = field.getType();
				if (!Component.class.isAssignableFrom(clazz)) {
					selectedFields.add(field);
				}
			}
		}
		return selectedFields;
	}
	
//	private static List<Field> retrieveAllNonStaticFieldsFromClass(Class<?> clazz) {
//	}

	/**
	 * Retrieve all non static and non transient fields from a class. <p>
	 * This method is called during the marshalling.
	 * @param clazz a Class object
	 * @return a List of Field instances
	 */
	static List<Field> retrieveAllNonStaticAndNonTransientFieldFromClass(Class<?> clazz) {
		return dropOutStaticTransientAndComponentFields(retrieveAllFieldsFromClass(clazz));
	}
	
	/**
	 * This static method returns all the fields from a class including those
	 * inherited.<p>
	 * However, it omits private and protected fields from class in base packages.
	 * @param clazz a Class object
	 * @return a List of Field instances
	 */
	static List<Field> retrieveAllFieldsFromClass(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		do {
			Field[] fieldFromThisClass = clazz.getDeclaredFields();
			fields.addAll(Arrays.asList(fieldFromThisClass));
		} while ((clazz = clazz.getSuperclass()) != null);
		return fields;
	}

	
	
	/**
	 * Retrieve all non static and non transient fields from a class. <p>
	 * This method is called during the unmarshalling.
	 * @param clazz a Class object
	 * @return a Map whose field names are the keys and the fields are the values
	 */
	static Map<String, Field> getFieldMapFromClass(Class<?> clazz) {
		List<Field> fields = retrieveAllNonStaticAndNonTransientFieldFromClass(clazz);
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

	/**
	 * Retrieve the class of the object underlying the Serializable list instance.
	 * @param list the SerializableList instance
	 * @return a Class instance
	 * @throws ClassNotFoundException if the class cannot be found
	 */
	static Class<?> getClass(SerializableList<?> list) throws ClassNotFoundException {
		if (list.isPrimitive()) {
			return ReflectUtility.PrimitiveTypeMap.get(list.getClassName());
		} else {
			return Class.forName(getClassName(list.getClassName()));
		}
	}
	
	/**
	 * This method returns the new class name if it has been changed.
	 * @param originalClassName the original class name
	 * @return a String
	 */
	static String getClassName(String originalClassName) {
		String className = originalClassName;
		String changedName = SerializerChangeMonitor.ClassNameChangeMap.get(className);
		if (changedName != null) {
			className = changedName;
		} 
		return className;
	}

	/**
	 * Find the entry that correspond to the class field.
	 * @param list a SerializableList instance
	 * @return a SerializableEntry instance or null if the class field does not exist in the list
	 */
	static SerializableEntry findClassField(SerializableList<?> list) {
		for (SerializableEntry entry : list.getEntries()) {
			if (entry.getFieldName().equals("class")) {
				return entry;
			}
		}
		return null;
	}
	
	/**
	 * This method returns the new enum name if it has been changed.
	 * @param enumClass the name of the enum class
	 * @param originalEnumName the original name of the enum variable
	 * @return a String
	 */
	static String getEnumName(String enumClass, String originalEnumName) {
		String enumName = originalEnumName;
		if (SerializerChangeMonitor.EnumNameChangeMap.containsKey(enumClass)) {
			String changedName = SerializerChangeMonitor.EnumNameChangeMap.get(enumClass).get(enumName);
			if (changedName != null) {
				enumName = changedName;
			} 
		}
		return enumName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <L extends SerializableList> L getNextEntryFromJava7MapEntry(L list) {
		if (list.getClassName().equals("java.util.HashMap$Entry") && REpiceaSystem.isCurrentJVMLaterThanThisVersion("1.7")) {
			for (Object ent : list.getEntries()) {
				SerializableEntry entry = (SerializableEntry) ent;
				if (entry.getFieldName().equals("next") && entry.getValue() instanceof SerializableList) {
					return (L) entry.getValue();
				}
			}
		} 
		return null;
	}

	/**
	 * This method returns true if the object is either a String or a primitive type
	 * @param obj the instance to be checked
	 * @return a boolean
	 */
	public static boolean isStringOrPrimitive(Object obj) {
		return obj.getClass().equals(String.class) || obj.getClass().isPrimitive();
	}

}
