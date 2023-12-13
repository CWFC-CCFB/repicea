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

import java.io.File;
import java.security.InvalidParameterException;

/**
 * An abstract class for the different serializer.
 * @author Mathieu Fortin - December 2023
 */
public abstract class AbstractSerializer {

	protected final File file;
	protected final boolean enableCompression;
	
	/**
	 * Constructor.
	 * @param filename the file that serves as output
	 * @param enableCompression true to use compression or false otherwise
	 */
	protected AbstractSerializer(String filename, boolean enableCompression) {
		this.enableCompression = enableCompression;
		file = new File(filename);
		if (file.isDirectory()) {
			throw new InvalidParameterException("The filename parameter denotes a directory!");
		}
	}

	
	/**
	 * This method serializes an object in a file.
	 * @param obj any Object instance
	 * @throws MarshallingException if a marshalling error has occurred
	 */
	public abstract void writeObject(Object obj) throws MarshallingException;
	
}
