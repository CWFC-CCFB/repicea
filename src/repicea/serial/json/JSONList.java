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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import repicea.serial.SerializableList;

/**
 * A class for JSON type list.
 * @author Mathieu Fortin - December 2023
 */
@SuppressWarnings("serial")
public final class JSONList extends LinkedHashMap<String, Object> implements SerializableList<JSONEntry> {

	static final String IsArrayTag = "@isArray";
	static final String ClassNameTag = "@className";
	static final String IsPrimitiveTag = "@isPrimitive";
	static final String RefHashCodeTag = "@refHashCode";
	static final List<String> InformationFields = Arrays.asList(new String[] {IsArrayTag, ClassNameTag, IsPrimitiveTag, RefHashCodeTag});

	JSONList(Object root) {
		boolean isArray = root.getClass().isArray();
		put(IsArrayTag, isArray);
		if (isArray) {
			put(ClassNameTag, root.getClass().getComponentType().getName());
			put(IsPrimitiveTag, root.getClass().getComponentType().isPrimitive());
		} else {
			put(ClassNameTag, root.getClass().getName());
			put(IsPrimitiveTag, root.getClass().isPrimitive());
		}
		put(RefHashCodeTag, System.identityHashCode(root));
	}
	
	@Override
	public void add(JSONEntry entry) {
		put(entry.fieldName, entry.value);
	}

	@Override
	public void addAll(List<JSONEntry> entries) {
		for(JSONEntry entry : entries) {
			add(entry);
		}
	}

	@Override
	public List<JSONEntry> getEntries() {
		List<JSONEntry> entries = new ArrayList<JSONEntry>();
		for (String k : keySet()) {
			if (!InformationFields.contains(k)) {
				entries.add(new JSONEntry(k, get(k)));
			}
		}
		return entries;
	}

	@Override
	public boolean isArray() {return (Boolean) get(IsArrayTag);}

	@Override
	public int getRefHashCode() {return (Integer) get(RefHashCodeTag) ;}

	@Override
	public boolean isPrimitive() {return (Boolean) get(IsPrimitiveTag);}

	@Override
	public String getClassName() {return get(ClassNameTag).toString();}

	
	
}
