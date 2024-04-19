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
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import repicea.lang.REpiceaSystem;
import repicea.serial.xml.XmlDeserializer;

/**
 * An asbtract class for deserializers.
 * @author Mathieu Fortin - December 2023
 */
public abstract class AbstractDeserializer { 

	static {
		if (REpiceaSystem.isCurrentJVMLaterThanThisVersion("1.7")) {
			SerializerChangeMonitor.registerClassNameChange("java.util.HashMap$Entry", "java.util.AbstractMap$SimpleEntry");
		}
	}
	
	
	protected static enum ReadMode {File, InputStream}
	
	protected final File file;
	protected final InputStream is;
	protected final ReadMode readMode;
	
	
	/**
	 * Constructor.<p>
	 * 
	 * The deserializer can work with files or streams. It first tries to 
	 * locate the physical file represented by the argument filename. If it
	 * fails, it then tries to find the resource and to get it as a stream. 
	 * 
	 * @param filename the file from which the object is deserialized
	 */
	protected AbstractDeserializer(String filename) {
		File f = new File(filename);
		if (f.isFile()) {
			file = f;
			is = null;
			readMode = ReadMode.File;
		} else {
			file = null;
			is = getClass().getResourceAsStream("/" + filename);
			if (is == null) {
				throw new InvalidParameterException("The filename is not a file and cannot be converted into a stream!");
			}
			readMode = ReadMode.InputStream;
		}
	}
	
	/**
	 * This method returns the object that has been deserialized.
	 * @return an Object instance
	 * @throws UnmarshallingException if an error has occurred during the unmarshalling
	 */
	public abstract Object readObject() throws UnmarshallingException;
	
}
