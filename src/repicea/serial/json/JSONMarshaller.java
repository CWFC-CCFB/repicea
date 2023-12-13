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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import repicea.serial.AbstractMarshaller;

/**
 * A Marshaller class for JSON file
 * @author Mathieu Fortin - December 2023
 */
public final class JSONMarshaller extends AbstractMarshaller<JSONEntry, JSONList> {

	static final String EntriesTag = "entries";
	
//	@SuppressWarnings({ "serial", "rawtypes" })
//	static class MapEntryWrapper extends ArrayList<Entry> {	// TODO Fix the serialization of this class in the marshal method below
//		MapEntryWrapper(Set<Entry> entries) {
//			addAll(entries);
//		}
//	}
//
//	@SuppressWarnings({ "serial" })
//	static class ListEntryWrapper extends ArrayList<Object> {	// TODO Fix the serialization of this class in the marshal method below
//		ListEntryWrapper(Collection<Object> collection) {
//			addAll(collection);
//		}
//	}

	@Override
	protected JSONEntry createSerializableEntryObject(String fieldName, Object value) {
		return new JSONEntry(this, fieldName, value);
	}

	@Override
	protected JSONList createSerializableListObject(Object o) {
		return new JSONList(o);
	}

	@Override
	protected String getEntriesTag() {return EntriesTag;}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addCollectionEntriesToThisObject(JSONList objToBeSerialized, Collection coll) {
		List<Object> listedEntries = new ArrayList<Object>();
		for (Object entry : coll) {
			JSONEntry value = this.createSerializableEntryObject("value", entry);
			listedEntries.add(value.getValue());
		}
		objToBeSerialized.put(getEntriesTag(), listedEntries);
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addMapEntriesToThisObject(JSONList objToBeSerialized, Set<Entry> entries) {
		LinkedHashMap<Object, Object> mappedEntries = new LinkedHashMap<Object, Object>();
		for (Entry entry : entries) {
			JSONEntry key = createSerializableEntryObject("key", entry.getKey());
			JSONEntry value = createSerializableEntryObject("value", entry.getValue());
			mappedEntries.put(key.getValue(), value.getValue());
		}
		objToBeSerialized.put(getEntriesTag(), mappedEntries);
	}
	
	
	/*
	 * public JSONList marshall(Object obj) { if (obj instanceof MapEntryWrapper ||
	 * obj instanceof ListEntryWrapper) { JSONList objToBeSerialized =
	 * createSerializableListObject(obj); } }
	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public JSONList marshall(Object obj) {
//		JSONList objToBeSerialized = createList(obj);
//		if (!hasObjectBeenRegistered(obj)) {
//			if (obj instanceof MapEntryWrapper) {
//				for (Entry ent : (MapEntryWrapper) obj) {
//					objToBeSerialized.add(createEntry(this, ent.getKey().toString(), ent.getValue())); // could be something else than a string in key
//				}
//			} else {
//				registerObject(obj);
//
//				if (objToBeSerialized.isArray()) {
//					int length = Array.getLength(obj);
//					for (int i = 0; i < length; i++) {
//						objToBeSerialized.add(createEntry(this, ((Integer) i).toString(), Array.get(obj, i)));
//					}
//				} else if (obj instanceof Enum) {				// enum case: just the name is saved
//					objToBeSerialized.add(createEntry(this, "name", ((Enum<?>) obj).name()));
//				} else if (obj instanceof Class) {
//					objToBeSerialized.add(createEntry(this, "class", ((Class) obj).getName()));
//				} else if (obj instanceof Map) {				
//					Set<Entry> entries = ((Map) obj).entrySet();
//					objToBeSerialized.add(createEntry(this, EntriesTag, new MapEntryWrapper(entries)));
//					List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
//					objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
//				} else if (obj instanceof Collection) {				
//					objToBeSerialized.add(createEntry(this, EntriesTag, ((Collection) obj).toArray()));
//					List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
//					objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
//				} else {
//					List<Field> selectedObjectFields = MarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
//					objToBeSerialized.addAll(formatToSerializableEntries(selectedObjectFields, obj));
//				}
//			}
//		}
//		return objToBeSerialized;
//	}

}