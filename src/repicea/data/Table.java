/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
package repicea.data;

import java.util.List;

public interface Table {

	/**
	 * Return the field names in a list. The list is a new list so that changes will
	 * not affect the fieldNames member.
	 * @return a List instance
	 */
	public List<String> getFieldNames();
	
	/**
	 * Provide the primitive types of each field
	 * @return a List of Class instances
	 */
	public List<Class<?>> getFieldTypes();
	
	/**
	 * Return the list of records contained in the table.
	 * @return a List of Record instances
	 */
	public List<? extends Record> getRecords();

}
