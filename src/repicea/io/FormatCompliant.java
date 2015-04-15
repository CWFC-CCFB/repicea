/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea, 
 * Quebec Ministry of Natural Resources, and INRA
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
package repicea.io;

import java.util.List;

import repicea.io.FormatField;

/**
 * This interface ensures the instance can provides and deserialize a record set.
 * @author Mathieu Fortin - July 2012
 */
public interface FormatCompliant {
	
	/**
	 * This method returns the FormatField instances that serve to define the header of the file.
	 * @return a List of FormatField instances
	 */
	public List<FormatField> getFieldsToSave();
	
	/**
	 * This method returns the records to be saved.
	 * @return a List of List of Object instances
	 */
	public List<List<Object>> getRecordsToSave();
	
	/**
	 * This method deserializes the records into the appropriate instances.
	 * @param records a List of List of Object instances
	 */
	public void deserializeFromRecords(List<List<Object>> records);
	


}
