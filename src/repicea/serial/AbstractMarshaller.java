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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * An abstract class for marshalling object.
 * 
 * @author Mathieu Fortin - December 2023
 *
 * @param <P> a class that implements the SerializableEntry interface
 * @param <L> a class that implements the SerializableList interface
 */
public abstract class AbstractMarshaller<P extends SerializableEntry, L extends SerializableList<P>> {

	private final Map<Class<?>, Set<Integer>> registeredObjects;

	protected AbstractMarshaller() {
		registeredObjects = new HashMap<Class<?>, Set<Integer>>();
	}

	private void registerObject(Object obj) {
		if (!registeredObjects.containsKey(obj.getClass())) {
			registeredObjects.put(obj.getClass(), new HashSet<Integer>());
		}
		Set<Integer> hashCodes = registeredObjects.get(obj.getClass());
		int hashCode = System.identityHashCode(obj);
		hashCodes.add(hashCode);
	}

	private boolean hasObjectBeenRegistered(Object obj) {
		if (registeredObjects.containsKey(obj.getClass())) {
			int hashCode = System.identityHashCode(obj);
			return registeredObjects.get(obj.getClass()).contains(hashCode); 
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public L marshall(Object obj) {
		L objToBeSerialized = createSerializableListObject(obj);
		if (!hasObjectBeenRegistered(obj)) {
			registerObject(obj);
			if (objToBeSerialized.isArray()) {
				int length = Array.getLength(obj);
				for (int i = 0; i < length; i++) {
					objToBeSerialized.add(createSerializableEntryObject(((Integer) i).toString(), Array.get(obj, i)));
				}
			} else if (obj instanceof Enum) {				// enum case: just the name is saved
				objToBeSerialized.add(createSerializableEntryObject("name", ((Enum<?>) obj).name()));
			} else if (obj instanceof Class) {
				objToBeSerialized.add(createSerializableEntryObject("class", ((Class) obj).getName()));
			} else if (obj instanceof Map) {				
				Set<Entry> entries = ((Map) obj).entrySet();
//				objToBeSerialized.add(createEntry(this, "entries", entries.toArray()));
				addMapEntriesToThisObject(objToBeSerialized, entries);
				List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
			} else if (obj instanceof Collection) {				
//				objToBeSerialized.add(createEntry(this, "entries", ((Collection) obj).toArray()));
				addCollectionEntriesToThisObject(objToBeSerialized, (Collection) obj);
				List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
			} else {
				List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
			}
		}
		return objToBeSerialized;
	}
	

	private List<P> formatToSerializableEntries(List<Field> fields, Object root) {
		List<P> entries = new ArrayList<P>();
		for (Field field : fields) {
			String fieldName = field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(root);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			entries.add(createSerializableEntryObject(fieldName, value));
		}
		return entries;
	}

	@SuppressWarnings("rawtypes")
	protected abstract void addCollectionEntriesToThisObject(L objToBeSerialized, Collection coll);

	/**
	 * Provide the tag to identify entries of collections or maps
	 * @return a String
	 */
	protected abstract String getEntriesTag();

	@SuppressWarnings("rawtypes")
	protected abstract void addMapEntriesToThisObject(L objToBeSerialized, Set<Entry> entries);
	
	protected abstract P createSerializableEntryObject(String fieldName, Object value);
	
	protected abstract L createSerializableListObject(Object o);
}
