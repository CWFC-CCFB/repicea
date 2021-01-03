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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import repicea.io.FormatField;
import repicea.io.FormatHeader;
import repicea.io.FormatReader;
import repicea.io.tools.ImportFieldElement.ImportFieldElementIDCard;

/**
 * The StreamImportFieldManager class is a special type of ImportFieldManager that 
 * reads the records from a Queue instance instead of reading them from a file. 
 * This can be useful if the records come from a stream.
 * @author Mathieu Fortin - October 2020
 */
@SuppressWarnings("serial")
public class StreamImportFieldManager extends ImportFieldManager {

	static class QueueReaderFormatField extends FormatField {
		QueueReaderFormatField(String name) {
			super(name);
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
		
//		@Override 
//		protected List<QueueReaderFormatField> getFieldList() {
//			return super.getFieldList();
//		}
		
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
		 * Provide a copy of the record for eventual re-insertion.
		 * @return a list of arrays of objects
		 */
		List<Object[]> getCopyOfRecords() {
			List<Object[]> copy = new ArrayList<Object[]>();
			for (Object[] record : recordQueue) {
				copy.add(Arrays.copyOf(record, record.length));
			}
			return copy;
		}
		
	}
	
	private final QueueReader streamReader;
	private final Enum<?> groupFieldEnum;
	private List<Object[]> backup;
	
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
		groupFieldEnum = recordReader.defineGroupFieldEnum();
		
		List<ImportFieldElement> mandatoryFields = getFieldsByType(false);
		int i = 0;
		for (ImportFieldElement ife : mandatoryFields) {
			QueueReaderFormatField f = new QueueReaderFormatField("Field" + i);
			header.addField(f);
			ife.setFieldMatch(f);
			i++;
		}
		List<ImportFieldElement> optionalFields = getFieldsByType(true);
		for (ImportFieldElement ife : optionalFields) {
			QueueReaderFormatField f = new QueueReaderFormatField("Field" + i);
			header.addField(f);
			ife.setFieldMatch(FormatField.NON_AVAILABLE_FIELD);
			i++;
		}
	}

	/** 
	 * Keep a copy of the records in a private member for eventual re insertion in the stream.
	 */
	public void backupStream() {
		backup = streamReader.getCopyOfRecords();
	}

	/**
	 * Reinsert the backup into the stream if the backup exists and the queue is null.
	 * @return a boolean true if the backup has been reinserted or false if the backup is
	 * null or the queue is not empty yet.
	 */
	public boolean restream() {
		if (backup != null && !backup.isEmpty() && streamReader.recordQueue.isEmpty()) {
			for (Object[] record : backup) {
				streamReader.addRecord(record);
			}
			return true;
		}
		return false;
	}
	
	
	private List<ImportFieldElement> getFieldsByType(boolean isOptional) {
		List<ImportFieldElement> fields = new ArrayList<ImportFieldElement>();
		for (ImportFieldElement ife : getFields()) {
			if (!ife.fieldID.equals(groupFieldEnum)) {
				if (ife.isOptional == isOptional) {
					fields.add(ife);
				}
			}
		}
		return fields;
	}
	
	
	@Override
	public List<ImportFieldElementIDCard> getFieldDescriptions() {
		List<ImportFieldElementIDCard> fieldDescriptions = new ArrayList<ImportFieldElementIDCard>();
		for (ImportFieldElement f : getMandatoryAndOptionalFields()) {
			fieldDescriptions.add(f.getIDCard());
		}
		return fieldDescriptions;
	}

	
	private List<ImportFieldElement> getMandatoryAndOptionalFields() {
		List<ImportFieldElement> ifes = getFieldsByType(false);
		ifes.addAll(getFieldsByType(true));
		return ifes;
	}

	/**
	 * Set the field matches manually if needed.
	 * @param indices a list of integer
	 * @return true if the field matches have been changed or false otherwise
	 */
	public boolean setFieldMatches(int[] indices) {
		List<ImportFieldElement> ifes = getMandatoryAndOptionalFields();
		if (indices != null) {
			for (int i = 0; i < indices.length; i++) {
				if (i < ifes.size()) {
					int index = indices[i];
					ifes.get(i).setMatchingFieldIndex(index);
				}
			}
			return true;
		}
		return false;
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
