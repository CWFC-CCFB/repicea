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
import java.io.InputStream;
import java.security.InvalidParameterException;

import repicea.lang.REpiceaSystem;

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
	 * Constructor.
	 * @param filename the file from which the object is deserialized
	 */
	protected AbstractDeserializer(String filename) {
		file = new File(filename);
		if (!file.isFile()) {
			throw new InvalidParameterException("The file is either a directory or does not exist!");
		}
		is = null;
		readMode = ReadMode.File;
	}

	
	/**
	 * Constructor.
	 * @param is an InputStream instance from which the object is deserialized
	 */
	protected AbstractDeserializer(InputStream is) {
		this.is = is;
		file = null;
		readMode = ReadMode.InputStream;
	}
	
	
	/**
	 * This method returns the object that has been deserialized.
	 * @return an Object instance
	 * @throws UnmarshallingException if an error has occurred during the unmarshalling
	 */
	public abstract Object readObject() throws UnmarshallingException;
	
}
