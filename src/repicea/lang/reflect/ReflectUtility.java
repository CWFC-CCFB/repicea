/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.lang.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectUtility {

	public final static Set<Class<?>> PrimitiveWrappers = new HashSet<Class<?>>();
	static {
		PrimitiveWrappers.add(Byte.class);
		PrimitiveWrappers.add(Short.class);
		PrimitiveWrappers.add(Character.class);
		PrimitiveWrappers.add(Integer.class);
		PrimitiveWrappers.add(Long.class);
		PrimitiveWrappers.add(Float.class);
		PrimitiveWrappers.add(Double.class);
		PrimitiveWrappers.add(Boolean.class);
	}
	
	public final static Map<String, Class<?>> PrimitiveTypeMap = new HashMap<String, Class<?>>();
	static {
		PrimitiveTypeMap.put("byte", byte.class);
		PrimitiveTypeMap.put("short", short.class);
		PrimitiveTypeMap.put("char", char.class);
		PrimitiveTypeMap.put("int", int.class);
		PrimitiveTypeMap.put("long", long.class);
		PrimitiveTypeMap.put("float", float.class);
		PrimitiveTypeMap.put("double", double.class);
		PrimitiveTypeMap.put("boolean", boolean.class);
	}

	
	
	/**
	 * This static method returns all the fields from a class including those
	 * inherited.
	 * @param clazz a Class object
	 * @return a List of Field instances
	 */
	public static List<Field> retrieveAllFieldsFromClass(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		do {
			Field[] fieldFromThisClass = clazz.getDeclaredFields();
			fields.addAll(Arrays.asList(fieldFromThisClass));
		} while ((clazz = clazz.getSuperclass()) != null);
		return fields;
	}

	/**
	 * This method returns true if the class is primitive or 
	 * a wrapper for a primitive.
	 * @param clazz
	 * @return a boolean
	 */
	public static boolean isPrimitive(Class<?> clazz) {
		return PrimitiveWrappers.contains(clazz) || PrimitiveTypeMap.values().contains(clazz);
	}
	
}
