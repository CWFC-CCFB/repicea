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

import java.util.List;

/**
 * Ensure the list provides the information for proper serialization.
 * @author Mathieu Fortin - December 2023
 *
 * @param <P> a class inheriting from SerializableEntry
 */
public interface SerializableList<P extends SerializableEntry> {

	/**
	 * Add the entry to the list.
	 * @param entry a SerializableEntry derived instance
	 */
	public void add(P entry);

	/**
	 * Add all the entries of list to this list.
	 * @param entries a list of SerializableEntry derived instances
	 */
	public void addAll(List<P> entries);

	/**
	 * Provide the entries in this list instance
	 * @return a List of SerializableEntry derived instances
	 */
	public List<P> getEntries();

	/**
	 * Inform on whether the original instance was an array of not
	 * @return a boolean
	 */
	public boolean isArray();

	/**
	 * Provide the reference hashcode of this instance.
	 * @return an integer
	 */
	public int getRefHashCode();

	/**
	 * Inform on whether the original instance was a primitive.
	 * @return a boolean
	 */
	public boolean isPrimitive();
	
	/**
	 * Provide the class name of the original instance.
	 * @return a String
	 */
	public String getClassName();
}
