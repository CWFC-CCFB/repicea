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
package repicea.serial.json;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import repicea.lang.reflect.ReflectUtility;
import repicea.serial.AbstractUnmarshaller;
import repicea.serial.MarshallingUtilities;
import repicea.serial.UnmarshallingException;

/**
 * A Unmarshaller class for JSON file
 * @author Mathieu Fortin - December 2023
 */
public final class JSONUnmarshaller extends AbstractUnmarshaller<JSONEntry, JSONList> {

	@Override
	protected void performPostMarshallingActionIfAny(Object newInstance) {}

	@Override
	protected String getEntriesTag() {return JSONMarshaller.EntriesTag;}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void putEntriesIntoMap(Class clazz, Object newInstance, Object entries) throws UnmarshallingException {
		if (entries != null && entries instanceof LinkedHashMap) {
			LinkedHashMap mapEntries = (LinkedHashMap) entries;
			if (!mapEntries.isEmpty()) {
				try {
					Method putMethod = clazz.getMethod("put", Object.class, Object.class);
					Class declaringClass = putMethod.getDeclaringClass();
					if (declaringClass.getClassLoader() != null) {
						issueWarning(clazz, "put");
					}
					for (Object key : mapEntries.keySet()) {
						((Map) newInstance).put(key, mapEntries.get(key)); 
					} 
				} catch (Exception e) {
					throw new UnmarshallingException(e);
				}
			}
		} else {
			throw new UnmarshallingException("The JSONDeserializer expects the map entries to come as a LinkedHashMap instance!");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void addEntriesToCollection(Class clazz, Object newInstance, Object entries) throws UnmarshallingException {
		if (entries != null && entries instanceof ArrayList) {
			ArrayList listEntries = (ArrayList) entries;
			if (!listEntries.isEmpty()) {
				try {
					Method addMethod = clazz.getMethod("add", Object.class);
					Class declaringClass = addMethod.getDeclaringClass();
					if (declaringClass.getClassLoader() != null) {
						issueWarning(clazz, "add");
					};
					for (Object listEntry : listEntries) {
						((Collection) newInstance).add(listEntry);  
					}
				} catch (Exception e) {
					throw new UnmarshallingException(e);
				}
			}
		} else {
			throw new UnmarshallingException("The XmlDeserializer expects map entries to come as an array!");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Object unmarshalMapOrCollectionEntries(JSONEntry entry) throws ReflectiveOperationException, UnmarshallingException {
		if (entry.getValue() instanceof LinkedHashMap) {	// it was a map
			LinkedHashMap currentLinkedHashMap = (LinkedHashMap) entry.getValue();
			LinkedHashMap<Object, Object> formattedLinkedHashMap = new LinkedHashMap<Object, Object>();
			for (Entry mapEntry : (Set<Entry>) currentLinkedHashMap.entrySet()) {
				Object key = mapEntry.getKey();
				Object value = mapEntry.getValue();
				Object newKey = MarshallingUtilities.isStringOrPrimitive(key) || ReflectUtility.PrimitiveWrappers.contains(key.getClass()) ? 
						key : 
							unmarshall((JSONList) key);
				Object newValue = MarshallingUtilities.isStringOrPrimitive(value) || ReflectUtility.PrimitiveWrappers.contains(value.getClass())
						? value : 
							unmarshall((JSONList) value);
				formattedLinkedHashMap.put(newKey, newValue);
			}
			return formattedLinkedHashMap;
		} else if (entry.getValue() instanceof ArrayList) {
			ArrayList currentList = (ArrayList) entry.getValue();
			ArrayList<Object> formattedList = new ArrayList<Object>();
			for (Object o : currentList) {
				Object newObject = MarshallingUtilities.isStringOrPrimitive(o) ? o : unmarshall((JSONList) o);
				formattedList.add(newObject);
			}
			return formattedList;
		} else {
			throw new UnmarshallingException("The JSONDeserializer expects the map or collection entries to come as a LinkedHashMap instance!");
		}
	}


}
