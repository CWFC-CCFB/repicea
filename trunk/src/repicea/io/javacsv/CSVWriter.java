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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.GFileFilter;
import repicea.io.GFileFilter.FileType;

public class CSVWriter extends FormatWriter<CSVHeader> {
	
	private BufferedWriter bufferedWriter;
	
	public CSVWriter(File csvFile, boolean append) throws IOException {
		super(csvFile, append);
		if (GFileFilter.getFileType(getFilename()) != FileType.CSV) {
			throw new IOException("CSVWriter.c. The file is not a .csv file");
		}
		setFormatHeader(new CSVHeader());

		File outputFile = new File(getFilename());
		if (outputFile.exists() && appendFile) {
			BufferedReader reader = openStream();
			getHeader().read(reader);
		} else {
			if (outputFile.exists() && !outputFile.delete()) {
				throw new IOException("Java has been unable to delete file : " + outputFile.getAbsolutePath());
			}
			outputFile.createNewFile();
		}
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(getFilename(), appendFile));
		bufferedWriter = new BufferedWriter(outputStreamWriter);
	}


	@Override
	public void addRecord(Object[] record) throws IOException {
		try {
			validateRecord(record);
			bufferedWriter.write(buildLine(record));
			bufferedWriter.newLine();
			getHeader().setNumberOfRecords(getHeader().getNumberOfRecords() + 1);
		} catch (IOException e) {
			close();
			throw new IOException("CSVWriter.addRecord(). An error occured while adding a record to the file!");
		}
	}
	
	
	private String buildLine(Object[] record) {
		String line = "";
		String token = getHeader().getToken();
		String fieldValue;
		for (int i = 0; i < record.length; i++) {
			fieldValue = record[i].toString();
			if (fieldValue.isEmpty()) {
				fieldValue = "";				// changed 2013-09-10 MFortin
			}
			if (i == 0) {
				line = line.concat(fieldValue);
			} else {
				line = line.concat(token.concat(fieldValue));
			}
		}
		return line;
	}
	
	@Override
	public void setFields(List<FormatField> fields) throws IOException {
		try {
			super.setFields(fields);
			String headerInTheFile = "";
			String token = getHeader().getToken();
			String fieldName;
			for (int i = 0; i < getHeader().getNumberOfFields(); i++) {				
				fieldName = getHeader().getField(i).getName();				
				if (i != getHeader().getNumberOfFields() - 1) {
					fieldName = fieldName.concat(token);
				}
				headerInTheFile = headerInTheFile.concat(fieldName);
			}
			bufferedWriter.write(headerInTheFile);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			close();
			throw new IOException(e.getMessage() + "CSVWriter.setFields(). An error occured while setting the fields");
		}
	}

	
	@Override
	public void close() {
		try {
			bufferedWriter.close();
		} catch (Exception e) {}
	}

	@Override
	public FormatField convertGExportFieldDetailsToFormatField(GExportFieldDetails details) {
		return new CSVField(details.getName());
	}	

	
	protected BufferedReader openStream() throws IOException {
		InputStream in = new FileInputStream(getFilename());
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		return new BufferedReader(inputStreamReader);
	}

//	public static void main(String[] args) {
//		String input = "C:" + File.separator 
//				+ "Users" + File.separator
//				+ "mfortin" + File.separator 
//				+ "Desktop" + File.separator
//				+ "donneesR_min.csv";
//		String output = "C:" + File.separator 
//				+ "Users" + File.separator
//				+ "mfortin" + File.separator 
//				+ "Desktop" + File.separator
//				+ "allo.csv";
//		File file = new File(output);
//		try {
//			CSVReader reader = new CSVReader(input);
//			CSVWriter writer = new CSVWriter(file);
////			writer.setFields(reader.getHeader().getFieldArray());
//			Object[] lineRead = reader.nextRecord();
//			while (lineRead != null) {
//				writer.addRecord(lineRead);
//				lineRead = reader.nextRecord();
//			}
//			writer.close();
//			int u = 0;
//		} catch (IOException e) {
//			
//		}
//	}

}



