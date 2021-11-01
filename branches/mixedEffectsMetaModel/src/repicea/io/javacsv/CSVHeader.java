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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.io.FormatHeader;

/**
 * This private class defines the header of a CSV file.
 * @author Mathieu Fortin - October 2011
 */
public class CSVHeader extends FormatHeader<CSVField> {
	
	private String token = ";";			// default value
	
	protected CSVHeader(String token) {
		super();
		if (token != null && !token.isEmpty()) {
			this.token = token.substring(0, 1);	// pick the first character only
			if (!this.token.equals(";") && !this.token.equals(",")) {
				throw new InvalidParameterException("The only available field splitter for the CSV writer is either , or ;");
			}
		}
	}

	protected CSVHeader() {
		this(null);
	}

	protected void read(BufferedReader bufferedReader) throws IOException {
		String firstLine = bufferedReader.readLine();

		if (firstLine == null) {
			bufferedReader.close();
			throw new IOException("The file has no header");
		} else {
			String[] splitter = firstLine.split(token);
			String[] splitter2 = firstLine.split(",");

			if (splitter2.length > splitter.length) {
				splitter = splitter2;
				token = ",";
			}

			List<CSVField> fields = new ArrayList<CSVField>();
			List<String> fieldNames = new ArrayList<String>();
			int index = 0;
			for (String fieldName : splitter) {
				fieldName = fieldName.replace("\"","");
				if (fieldName.isEmpty()) {
					fieldName = "Empty" + index;
				}
				if (!Character.isLetter(fieldName.charAt(0))) {
					throw new IOException("Field name " + fieldName + " is not acceptable. A field name must start with a letter.");
				}
				if (fieldNames.contains(fieldName)) {
					int numberOfTimes = fieldNames.indexOf(fieldName) - fieldNames.indexOf(fieldName) + 1;
					fieldName = fieldName + numberOfTimes;
				}
				fieldNames.add(fieldName);
				fields.add(new CSVField(fieldName));
				index++;
			}

			setFieldList(fields);

			int numberOfLines = 0;
			while (bufferedReader.readLine() != null) {
				numberOfLines++;
			}
			setNumberOfRecords(numberOfLines);
			bufferedReader.close();
		} 
		
	}

	
	protected String getToken() {return token;}
	
	@Override
	protected int getNumberOfRecords() {return super.getNumberOfRecords();}
	
	@Override
	protected void setNumberOfRecords(int numberOfRecords) {super.setNumberOfRecords(numberOfRecords);}

}
