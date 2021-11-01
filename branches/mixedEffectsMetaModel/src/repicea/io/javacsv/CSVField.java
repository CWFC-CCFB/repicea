/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.io.javacsv;

import repicea.io.FormatField;

/**
 * This class represents a field of a CSV file. The CSVField only contains a String to provide the name of the field.
 * @author Mathieu Fortin - September 2011
 */
public class CSVField extends FormatField {

	/**
	 * Constructor for a CSV field.
	 * @param name the name of the field
	 */
	public CSVField(String name) {
		super(name);
	}
	
}
