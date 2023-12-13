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

/**
 * Ensures the entry can provide the information required for serialization.
 * @author Mathieu Fortin - December 2023
 */
public interface SerializableEntry {

	/**
	 * The value of this entry.
	 * @return an Object instance
	 */
	public Object getValue();
	
	/**
	 * The field name associated to this entry.
	 * @return a String 
	 */
	public String getFieldName();
}
