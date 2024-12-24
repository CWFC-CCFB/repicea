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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.reflect.ReflectionFactory;

/**
 * An abstract class for unmarshalling object.
 * 
 * @author Mathieu Fortin - December 2023
 *
 * @param <P> a class that implements the SerializableEntry interface
 * @param <L> a class that implements the SerializableList interface
 */
public abstract class AbstractUnmarshaller<P extends SerializableEntry, L extends SerializableList<P>> {
	
	@SuppressWarnings("rawtypes")
	private static Map<String, List<Class>> Warnings = new HashMap<String, List<Class>>();
	
	private final Map<Class<?>, Map<Integer, Object>> registeredObjects;
	
	protected  AbstractUnmarshaller() {
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
	public Object unmarshall(L objToBeDeserialized) throws ReflectiveOperationException, UnmarshallingException {
		Class<?> clazz = MarshallingUtilities.getClass(objToBeDeserialized);
		int referenceHashCode = objToBeDeserialized.getRefHashCode();
		if (hasObjectBeenRegistered(clazz, referenceHashCode)) {		// if the object has already been registered it is returned
			return retrieveObject(clazz, referenceHashCode);
		} else {
			if (objToBeDeserialized.isArray()) {											// array case
				if (hasObjectBeenRegistered(clazz, referenceHashCode)) {		// if the object has already been registered it is returned
					return retrieveObject(clazz, referenceHashCode);
				}
				List<Object> list = new ArrayList<Object>();
				List<Object> mapEntriesPossiblyLost = new ArrayList<Object>();
				for (P entry : objToBeDeserialized.getEntries()) {
					if (entry.getValue() != null && entry.getValue() instanceof SerializableList) {
						L listValue = (L) entry.getValue();
						Object obj = unmarshall(listValue); 
						if (mapEntriesPossiblyLost.contains(obj)) {
							mapEntriesPossiblyLost.remove(obj);
						}
						list.add(obj);
						
						L nextXmlListForCompatibility = listValue;
						while ((nextXmlListForCompatibility = MarshallingUtilities.getNextEntryFromJava7MapEntry(nextXmlListForCompatibility)) != null) {	// patch for the change in Map$Entry in Java 8
							mapEntriesPossiblyLost.add(unmarshall(nextXmlListForCompatibility)); // instantiate the instance in the former next member, otherwise it is lost
						}
					} else {
						list.add(entry.getValue());
					}
				}

				if (!mapEntriesPossiblyLost.isEmpty()) {		// we add the entries that were in the next member and possibly forgotten in the map since version 8 does not include the next member anymore
					list.addAll(mapEntriesPossiblyLost);
				}
				
				Object arrayObject = Array.newInstance(clazz, list.size());
				for (int i = 0; i < list.size(); i++) {
					Array.set(arrayObject, i, list.get(i));
				}
				registerObject(clazz, objToBeDeserialized.getRefHashCode(), arrayObject);
				return arrayObject;
			} else if (Enum.class.isAssignableFrom(clazz)) {			// enum case
				String enumName = MarshallingUtilities.getEnumName(clazz.getName(), objToBeDeserialized.getEntries().get(0).getValue().toString());
				Object newInstance = Enum.valueOf((Class<Enum>) clazz, enumName);
				registerObject(clazz, objToBeDeserialized.getRefHashCode(), newInstance);
				return newInstance;
			} else if (clazz.equals(Class.class)) {
				SerializableEntry fieldEntry = MarshallingUtilities.findClassField(objToBeDeserialized);
				if (fieldEntry == null) {
					throw new UnmarshallingException("The class field cannot be found!");
				}
				String className = MarshallingUtilities.getClassName(fieldEntry.getValue().toString());
				Object newInstance = Class.forName(className);
				registerObject(clazz, objToBeDeserialized.getRefHashCode(), newInstance);
				return newInstance;
			} else {													// any other case
				Constructor<?> emptyCstor = MarshallingUtilities.getEmptyConstructor(clazz);
				Constructor<?> cstor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, emptyCstor);
				cstor.setAccessible(true);
				Object newInstance = cstor.newInstance(new Object[]{});
				registerObject(clazz, objToBeDeserialized.getRefHashCode(), newInstance);
				Map<String, Field> fieldsToFill = MarshallingUtilities.getFieldMapFromClass(clazz);
				Object mapOrCollectionEntries = null;
				for (P entry : objToBeDeserialized.getEntries()) {
					if (Map.class.isAssignableFrom(clazz) && entry.getFieldName().equals(getEntriesTag())) {
//						mapEntries = unmarshall((L) entry.getValue());
						mapOrCollectionEntries = unmarshalMapOrCollectionEntries(entry);
					} else if (Collection.class.isAssignableFrom(clazz) && entry.getFieldName().equals(getEntriesTag())) {
						mapOrCollectionEntries = unmarshalMapOrCollectionEntries(entry);
//						mapEntries = unmarshall((L) entry.getValue());
					} else {
						Field field = fieldsToFill.get(entry.getFieldName());
						if (field != null && !isExceptionField(newInstance, field)) {			// means the field has been deleted since then
							field.setAccessible(true);
							Object value = entry.getValue();
							if (value instanceof SerializableList) {
								value = unmarshall((L) value);
							}
							field.set(newInstance, value);
						}
					}							
				}
				if (mapOrCollectionEntries != null) {  // map filling must come after setting other fields
					if (Map.class.isAssignableFrom(clazz)) {
						((Map) newInstance).clear();
						putEntriesIntoMap(clazz, newInstance, mapOrCollectionEntries);
					} else if (Collection.class.isAssignableFrom(clazz)) {
						if (!clazz.getName().equals("java.util.Arrays$ArrayList")) {	 // this class is immutable
							try {
								Field sizeField = findSizeField(clazz);
								if (sizeField != null) {
									sizeField.setAccessible(true);
									sizeField.set(newInstance, (Integer) 0);	// force the size to be 0 otherwise the clear method may exceed the array length
								}
							} catch (Exception e) {
								throw new UnmarshallingException("The deserializer did not manage to set the size in the Collection-derived " + clazz);
							}
							addEntriesToCollection(clazz, newInstance, mapOrCollectionEntries);
						}
					}
				}
				performPostMarshallingActionIfAny(newInstance);
				return newInstance;
			}
		}
	}
	
	/**
	 * Provide the tag to identify entries of collections or maps
	 * @return a String
	 */
	protected abstract String getEntriesTag();
	
	protected void performPostMarshallingActionIfAny(Object newInstance) {
		if (newInstance instanceof PostUnmarshalling) {
			((PostUnmarshalling) newInstance).postUnmarshallingAction();
		}
	}
	
	protected abstract Object unmarshalMapOrCollectionEntries(P entry) throws ReflectiveOperationException, UnmarshallingException;
	
	@SuppressWarnings({ "rawtypes" })
	protected abstract void putEntriesIntoMap(Class clazz, Object newInstance, Object entries) throws UnmarshallingException;

	@SuppressWarnings({ "rawtypes" })
	protected abstract void addEntriesToCollection(Class clazz, Object newInstance, Object entries) throws UnmarshallingException;
	
	@SuppressWarnings("rawtypes")
	protected final void issueWarning(Class clazz, String methodName) {
		if (!Warnings.containsKey(methodName)) {
			Warnings.put(methodName, new ArrayList<Class>());
		}
		List<Class> classes = Warnings.get(methodName);
		if (!classes.contains(clazz)) {
			System.err.println("WARNING: The original " + methodName + " method has been overriden in " + clazz + ".");
			System.err.println("This may lead to an unpredictable behaviour when deserializing.");
			classes.add(clazz);
		}
	}

	private boolean isExceptionField(Object newInstance, Field field) {
		if (Map.class.isAssignableFrom(newInstance.getClass())) {
			if (field.getName().equals("loadFactor")) {
				return true;
			} 
			if (field.getName().equals("threshold")) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private final Field findSizeField(Class clazz) {
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
