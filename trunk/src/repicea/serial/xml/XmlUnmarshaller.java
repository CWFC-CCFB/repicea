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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sun.reflect.ReflectionFactory;

/**
 * The XmlUnmarshaller class handles the deserialization from XmlEntry and XmlList classes.
 * @author Mathieu Fortin - November 2012
 */
public final class XmlUnmarshaller {

	private Map<Class<?>, Map<Integer, Object>> registeredObjects;
	
	public XmlUnmarshaller() {
		registeredObjects = new HashMap<Class<?>, Map<Integer, Object>>();
	}
	
	private void registerObject(Class<?> clazz, int hashCode, Object obj) {
		if (!registeredObjects.containsKey(clazz)) {
			registeredObjects.put(clazz, new HashMap<Integer, Object>());
		}
		registeredObjects.get(clazz).put(hashCode, obj);
	}
	
	private boolean hasObjectBeenRegistered(Class<?> clazz, int hashCode) {
		if (registeredObjects.containsKey(clazz)) {
			return registeredObjects.get(clazz).containsKey(hashCode);
		} else {
			return false;
		}
	}
	
	private Object retrieveObject(Class<?> clazz, int hashCode) {
		return registeredObjects.get(clazz).get(hashCode);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object unmarshall(XmlList xmlList) throws Exception {
		Class<?> clazz = XmlMarshallingUtilities.getClass(xmlList);
		int referenceHashCode = xmlList.refHashCode;
		if (hasObjectBeenRegistered(clazz, referenceHashCode)) {		// if the object has already been registered it is returned
			return retrieveObject(clazz, referenceHashCode);
		} else {
			if (xmlList.isArray) {											// array case
				List<Object> list = new ArrayList<Object>();
				List<Object> mapEntriesPossiblyLost = new ArrayList<Object>();
				for (XmlEntry entry : xmlList.getEntries()) {
					if (entry.value != null && entry.value instanceof XmlList) {
						XmlList xmlListvalue = (XmlList) entry.value;
						Object obj = unmarshall(xmlListvalue); 
						if (mapEntriesPossiblyLost.contains(obj)) {
							mapEntriesPossiblyLost.remove(obj);
						}
						list.add(obj);
						
						XmlList nextXmlListForCompatibility = xmlListvalue;
						while ((nextXmlListForCompatibility = XmlMarshallingUtilities.getNextEntryFromJava7MapEntry(nextXmlListForCompatibility)) != null) {	// patch for the change in Map$Entry in Java 8
							mapEntriesPossiblyLost.add(unmarshall(nextXmlListForCompatibility)); // instantiate the instance in the former next member, otherwise it is lost
						}
					} else {
						list.add(entry.value);
					}
				}

				if (!mapEntriesPossiblyLost.isEmpty()) {		// we add the entries that were in the next member and possibly forgotten in the map since version 8 does not include the next member anymore
					list.addAll(mapEntriesPossiblyLost);
				}
				
				Object arrayObject = Array.newInstance(clazz, list.size());
				for (int i = 0; i < list.size(); i++) {
					Array.set(arrayObject, i, list.get(i));
				}
				
				return arrayObject;
			} else if (Enum.class.isAssignableFrom(clazz)) {			// enum case
				String enumName = xmlList.getEntries().get(0).value.toString();
				Object newInstance = Enum.valueOf((Class<Enum>) clazz, enumName);
				registerObject(clazz, xmlList.refHashCode, newInstance);
				return newInstance;
			} else if (clazz.equals(Class.class)) {
				String className = XmlMarshallingUtilities.getClassName(xmlList.getEntries().get(0).value.toString());
				Object newInstance = Class.forName(className);
				return newInstance;
			} else {													// any other case
				Constructor<?> emptyCstor = XmlMarshallingUtilities.getEmptyConstructor(clazz);
				Constructor<?> cstor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, emptyCstor);
				cstor.setAccessible(true);
				Object newInstance = cstor.newInstance(new Object[]{});
				registerObject(clazz, xmlList.refHashCode, newInstance);
//				if (Map.class.isAssignableFrom(clazz)) {				// map case
//					Object[] entries = (Object[]) unmarshall((XmlList) xmlList.list.get(0).value);
//					for (Object entry : entries) {
//						((Map) newInstance).put(((Entry) entry).getKey(), ((Entry) entry).getValue());
//					}
//					return newInstance;
//				} else {
				Map<String, Field> fieldsToFill = XmlMarshallingUtilities.getFieldMapFromClass(clazz);
				Object[] mapEntries = null;
				for (XmlEntry entry : xmlList.getEntries()) {
					if (Map.class.isAssignableFrom(clazz) && entry.fieldName.equals("entries")) {
						mapEntries = (Object[]) unmarshall((XmlList) entry.value);
					} else if (Collection.class.isAssignableFrom(clazz) && entry.fieldName.equals("entries")) {
						mapEntries = (Object[]) unmarshall((XmlList) entry.value);
					} else {
						Field field = fieldsToFill.get(entry.fieldName);
						if (field != null) {			// means the field has been deleted since then
							field.setAccessible(true);
							Object value = entry.value;
							if (value instanceof XmlList) {
								value = unmarshall((XmlList) value);
							}
							field.set(newInstance, value);
						}
					}							
				}
				if (mapEntries != null) {
					if (Map.class.isAssignableFrom(clazz)) {
						((Map) newInstance).clear();
						for (Object mapEntry : mapEntries) {
							((Map) newInstance).put(((Entry) mapEntry).getKey(), ((Entry) mapEntry).getValue());
						}
					} else if (Collection.class.isAssignableFrom(clazz)) {
						try {
//							Field sizeField = clazz.getDeclaredField("size");
							Field sizeField = this.findSizeField(clazz);
							sizeField.setAccessible(true);
							sizeField.set(newInstance, (Integer) 0);	// force the size to be 0 otherwise the clear method may exceed the array length
						} catch (Exception e) {
							e.printStackTrace();
						}
//						((Collection) newInstance).clear();
						for (Object listEntry : mapEntries) {
							((Collection) newInstance).add(listEntry);
						}
					}
				}
				if (newInstance instanceof PostXmlUnmarshalling) {
					((PostXmlUnmarshalling) newInstance).postUnmarshallingAction();
				}
				return newInstance;
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Field findSizeField(Class clazz) {
		List<Class> classes = new ArrayList<Class>();
		while (!clazz.equals(Object.class)) {
			classes.add(0, clazz);
			clazz = clazz.getSuperclass();
		}
		Field sizeField = null;
		for (Class superClazz : classes) {
			try {
				sizeField = superClazz.getDeclaredField("size");
			} catch (Exception e) {}
			if (sizeField != null) {
				break;
			}
		}
		return sizeField;
	}

}
