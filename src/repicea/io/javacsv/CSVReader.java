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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import repicea.io.FormatReader;
import repicea.util.ObjectUtility;

public class CSVReader extends FormatReader<CSVHeader> {

	private BufferedReader bufferedReader;
	private final Charset currentCharset;

	/**
	 * Constructor with default encoding
	 * @param filename the file to read
	 * @throws IOException if an I/O error has occurred
	 */
	public CSVReader(String filename) throws IOException {
		this(filename, null);
	}

	/**
	 * Constructor with specific encoding
	 * @param filename the file to read
	 * @param charset the encoding
	 * @throws IOException if an I/O error has occurred
	 */
	public CSVReader(String filename, Charset charset) throws IOException {
		super(filename);
		if (charset != null) {
			currentCharset = charset;
		} else {
			currentCharset = Charset.defaultCharset();
		}
		reset();
	}

	@Override
	public void reset() throws IOException {
		if (bufferedReader != null) {
			close();
		}
		bufferedReader = openReader();
		setFormatHeader(new CSVHeader());
		getHeader().read(bufferedReader);
		bufferedReader = openReader();		// reopen the stream because the header has read the whole file
		bufferedReader.readLine();			// skip the first line which is the header
		linePointer = 0;
		isClosed = false;
	}
	
	
	
	@Override
	public Object[] nextRecord(int skipThisNumberOfLines) throws IOException {
		int numberOfLinesSkipped = 0;
		while (numberOfLinesSkipped < skipThisNumberOfLines) {
			bufferedReader.readLine();
			numberOfLinesSkipped++;
			linePointer++;
		}
		String line = bufferedReader.readLine();
		if (line != null) {								
			List<String> splitter = ObjectUtility.splitLine(line, getHeader().getToken());
			if (splitter.size() > getFieldCount()) {
				throw new IOException("The number of fields in this line is larger than the number of fields in the header: line " + (linePointer + 1) + ".");
			}
			linePointer++;
			return splitter.toArray();
		} else {				// if the line is null then the end of file has been reached
			return null;
		}
	}
	
	
	private BufferedReader openReader() throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(openStream(), currentCharset);
		return new BufferedReader(inputStreamReader);
	}
	
	
	@Override
	protected void closeInternalStream() {
		try {
			bufferedReader.close();
		} catch (IOException e) {}
	}
	
}
