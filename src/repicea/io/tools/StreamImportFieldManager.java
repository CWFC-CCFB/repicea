/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Rouge-Epicea
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
package repicea.io.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import repicea.io.FormatField;
import repicea.io.FormatHeader;
import repicea.io.FormatReader;

/**
 * The StreamImportFieldManager class is a special type of ImportFieldManager that 
 * reads the records from a Queue instance instead of reading them from a file. 
 * This can be useful if the records come from a stream.
 * @author Mathieu Fortin - October 2020
 */
public class StreamImportFieldManager extends ImportFieldManager {

	static class QueueReaderFormatField extends FormatField {
		QueueReaderFormatField(String fieldName) {
			super(fieldName);
		}
	}
	
	static class QueueReaderHeader extends FormatHeader<QueueReaderFormatField> {
		QueueReaderHeader() {
			super();
		}

		@Override
		protected void addField(QueueReaderFormatField field) {
			super.addField(field);
		}
		
		List<String> getFieldNames() {
			List<String> fieldNames = new ArrayList<String>();
			for (QueueReaderFormatField f : getFieldList()) {
				fieldNames.add(f.getName());
			}
			return fieldNames;
		}
	}
	
	/**
	 * The QueueReader class inherits from the FormatReader class. Instead of 
	 * reading the record from a file, it reads the records from a Queue instance.
	 * @author Mathieu Fortin - October 2020
	 */
	public static class QueueReader extends FormatReader<QueueReaderHeader> {

		private final ConcurrentLinkedQueue<Object[]> recordQueue;
		
		QueueReader(){
			setFormatHeader(new QueueReaderHeader());
			recordQueue = new ConcurrentLinkedQueue<Object[]>();
		} 

		@Override
		public void close() throws IOException {}

		@Override
		public Object[] nextRecord(int skipThisNumberOfLines) throws IOException {
			return recordQueue.poll();
		}
		
		@Override
		public int getRecordCount() {
			return recordQueue.size();
		}

		/**
		 * Add a record to the Queue instance.
		 * @param record an array of Object instances.
		 */
		public void addRecord(Object[] record) {recordQueue.add(record);}
		
		/**
		 * Return the list of the field names.
		 * @return a List of String
		 */
		public List<String> getFieldNames() {return getHeader().getFieldNames();}
	}
	
	private final QueueReader streamReader;
	
	/**
	 * Constructor. Takes the recordReader object and extracts all the ImportFieldElement. 
	 * Then it sets the fields in the header of the QueueReader to match the ImportFieldElement.
	 * @param recordReader a REpiceaRecordReader instance
	 * @throws Exception
	 */
	public StreamImportFieldManager(REpiceaRecordReader recordReader) throws Exception {
		super(recordReader.defineFieldsToImport(), QueueReader.NOT_USING_FILES);
		
		streamReader = new QueueReader();
		QueueReaderHeader header = streamReader.getHeader();
		List<ImportFieldElement> fields = getFields();
		for (int i = 0; i < fields.size(); i++) {
			ImportFieldElement f = fields.get(i);
			if (!f.fieldID.equals(recordReader.defineGroupFieldEnum())) {
				QueueReaderFormatField sff = new QueueReaderFormatField(f.fieldID.name());
				header.addField(sff);
				f.setFieldMatch(sff);
			}
		}
	}

	@Override
	protected QueueReader instantiateFormatReader() {
		return getFormatReader();
	}

	/**
	 * Return the QueueReader embedded in this instance.
	 * @return a QueueReader instance
	 */
	public QueueReader getFormatReader() {return streamReader;}
	

	
}
