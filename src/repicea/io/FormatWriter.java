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
import java.io.IOException;
import java.util.List;

import repicea.io.GFileFilter.FileType;
import repicea.io.javacsv.CSVWriter;
import repicea.io.javadbf.DBFWriter;
import repicea.io.javasql.SQLWriter;


/**
 * Format reader is a general class for all the writers that exists (e.g. DBFWriter).
 * @author Mathieu Fortin - September 2011
 */
@SuppressWarnings("rawtypes")
public abstract class FormatWriter<H extends FormatHeader> implements Closeable {
	
	private H header;
	private String outputFilename;
	protected boolean appendFile;
	
	/**
	 * Empty constructor needed for the DBFWriter.
	 * @deprecated Use constructor FormatWriter(File outputFile) instead
	 */
	@Deprecated
	protected FormatWriter() {}
	
	protected FormatWriter(File outputFile, boolean append) throws IOException {
		this.outputFilename = outputFile.getAbsolutePath();
		this.appendFile = append;
	}
	
	/**
	 * This method returns the header of the output file.
	 * @return a FormatHeader instance
	 */
	public H getHeader() {return header;}
	
	/**
	 * This method sets the header of the output file.
	 * @param header a FormatHeader instance
	 */
	protected void setFormatHeader(H header) {this.header = header;}
	
	/**
	 * This method sets the fields in the header.
	 * @param formatHeader a H instance
	 * @throws IOException if an I/O error has occurred
	 */
	public void setFields(FormatHeader<FormatField> formatHeader) throws IOException {
		setFields(formatHeader.getFieldList());
	}
	
	
	/**
	 * This method sets the fields in the header.
	 * @param fields a Vector of FormatField instances
	 * @throws IOException if an I/O error has occurred
	 */
	@SuppressWarnings("unchecked")
	public void setFields(List<FormatField> fields) throws IOException {
		if (getHeader().getNumberOfFields() != 0) {
			throw new IOException("Fields has already been set");
		}
		if (fields.isEmpty()) {
			throw new IOException( "Should have at least one field");
		}
		getHeader().setFieldList(fields);
	}

	
	/**
	 * This method returns the name of the output file.
	 * @return a String instance
	 */
	protected String getFilename() {return outputFilename;}

	/**
	 * This method writes a record in the output file.
	 * @param record an Array of object to be written in the output file
	 * @throws IOException if an I/O error has occurred
	 */
	public abstract void addRecord(Object[] record) throws IOException;
	
	/**
	 * This method validates the record before adding it to the output file.
	 * @param record an array of Object instances
	 * @throws IOException if an I/O error has occurred
	 */
	protected void validateRecord(Object[] record) throws IOException {
		if (getHeader().getNumberOfFields() == 0) {
			throw new IOException( "Fields should be set before adding records");
		}

		if (record == null) {
			throw new IOException("A null record cannot be added.");
		}

		if (record.length != getHeader().getNumberOfFields()) {
			throw new IOException( "Invalid record. Invalid number of fields in row");
		}
	}
	
	
	/**
	 * This method selects the appropriate FormatWriter class.
	 * @param append true if the file is to be appended or false otherwise
	 * @param fileSpec a series of string, the first being at least the name of the output file, the second can be the table if exporting in a database for example
	 * @return a FormatWriter instance
	 * @throws IOException if an I/O error has occurred
	 */
	public static FormatWriter<? extends FormatHeader<? extends FormatField>> createFormatWriter(boolean append, String... fileSpec) throws IOException {
		try {
			FileType f = GFileFilter.getFileType(fileSpec[0]);
			if (f == FileType.DBF) {
				return new DBFWriter(new File(fileSpec[0]), append);
			} else if (f == FileType.CSV) {
				return new CSVWriter(new File(fileSpec[0]), append);
			} else if (f == FileType.ACCDB || f == FileType.MDB) {
				return new SQLWriter(new File(fileSpec[0]), fileSpec[1], append);
			} else {
				throw new IOException("Unknown file format!");
			}
		} catch (IOException e) {
			throw e;
		}
	}


	/**
	 * This method generates the appropriate FormatField given a GExportFieldDetails instance.
	 * @param details a GExportFieldDetails object
	 * @return a FormatField instance
	 */
	public abstract FormatField convertGExportFieldDetailsToFormatField(GExportFieldDetails details);
}
