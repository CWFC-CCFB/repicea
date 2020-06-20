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
package repicea.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import repicea.io.GFileFilter.FileType;
import repicea.io.javacsv.CSVReader;
import repicea.io.javadbf.DBFReader;
import repicea.io.javasql.SQLReader;

/**
 * Format reader is a general class for all the readers that exists (e.g. DBFReader).
 * @author Mathieu Fortin - September 2011
 */
@SuppressWarnings("rawtypes")
public abstract class FormatReader<H extends FormatHeader> implements Closeable {

	private String filename;
	private H header;
	private boolean isSystemResource;

	protected FormatReader(String filename) throws IOException {
		this.filename = filename;
		isSystemResource = false;
		File file = new File(filename);
		if (!file.exists()) {			// then try to load it as a resource
			InputStream in = getInputStream();
			if (in == null) {
				throw new IOException("The file " + filename + " does not exist and cannot be loaded as a ressource!");
			} else {
				in.close();
				isSystemResource = true;
			}
		}
	}
	
	
	private InputStream getInputStream() throws IOException {
//		InputStream in = ClassLoader.getSystemResourceAsStream(getFilename());
		InputStream in = getClass().getResourceAsStream(File.separator + filename);
//		URL url = getClass().getResource(File.separator + filename);
//		Object obj = url.getContent();
//		InputStream in = (InputStream) obj;
		return in;
	}
	
	/**
	 * This method opens the stream depending on its nature (a resource or a file)
	 * @return an InputStream instance
	 * @throws IOException
	 */
	protected InputStream openStream() throws IOException {
		InputStream in;
		if (isSystemResource()) {
			in = getInputStream();
//			in = getClass().getResourceAsStream(File.separator + getFilename());
//			in = ClassLoader.getSystemResourceAsStream(getFilename());
		} else {
			in = new FileInputStream(getFilename());
		}
		return in;
	}

	
	/**
	 * This method returns the number of records that contains the file read by the FormatReader instance.
	 * @return an integer
	 */
	public int getRecordCount() {
		return getHeader().getNumberOfRecords();
	}

	/**
	 * This method returns the field information provided by the header.
	 * @param fieldIndex the index of the field
	 * @return a FormatField instance
	 */
	protected FormatField getField(int fieldIndex) {
		return getHeader().getField(fieldIndex);
	}
	
	/**
	 * This method returns the number of fields of this file.	
	 * @return an integer
	 */
	public int getFieldCount() {
		return getHeader().getNumberOfFields();
	}
	
	/**
	 * This method returns the next record in the file.
	 * @return an array of Object instances
	 */
	public Object[] nextRecord() throws IOException {
		return nextRecord(0);
	}
	
	/**
	 * This method skips some lines and then reads a record in the file;
	 * @param skipThisNumberOfLines the number of lines to skip before reading the observation
	 * @return an Array of Object instances
	 */
	public abstract Object[] nextRecord(int skipThisNumberOfLines) throws IOException;
	
	/**
	 * This method selects the appropriate FormatReader class.
	 * @param fileSpec a list of specification for the file to open (e.g. the filename, the table, etc...)
	 * @return a FormatReader instance
	 * @throws IOException
	 */
	public static FormatReader createFormatReader(String... fileSpec) throws IOException {
		try {
			FileType f = GFileFilter.getFileType(fileSpec[0]);
			if (f == FileType.DBF) {
				return new DBFReader(fileSpec[0]);
			} else if (f == FileType.CSV) {
				return new CSVReader(fileSpec[0]);
			} else if (f == FileType.ACCDB || f == FileType.MDB) {
				return new SQLReader(fileSpec[0], fileSpec[1]);
			} else {
				throw new IOException("Unknown file format!");
			}
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * This method returns the header of the file that is to be read.
	 * @return a FormatHeader instance
	 */
	public H getHeader() {return header;}
	
	protected void setFormatHeader(H header) {this.header = header;}
	
	/**
	 * This method returns the name of the file this reader is supposed to read.
	 * @return a String
	 */
	public String getFilename() {return filename;}
	
	/**
	 * This method returns true if the reader is based on a system resource or false otherwise. The condition is
	 * tested in the constructor.
	 * @return a boolean
	 */
	protected boolean isSystemResource() {return isSystemResource;}
}
