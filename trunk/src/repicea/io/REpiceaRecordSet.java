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

import java.nio.BufferOverflowException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This subclass of ArrayList only implements an additional method in order to enable 
 * the selection of subsets of GExportRecord objects.
 * @author Mathieu Fortin - April 2011
 */
@SuppressWarnings("serial")
public class REpiceaRecordSet extends LinkedBlockingQueue<GExportRecord> {

	private static final int MAX_NUMBER_RECORDS = 100000;
	
	private Thread saveThread;
	private final Object lock = new Object();
	
	/**
	 * General constructor for this class.
	 */
	public REpiceaRecordSet() {
		super();
	}
		
	/**
	 * This method makes it possible to select records that match particular value in a given field.
	 * @param field the String that represents the field name
	 * @param value the Object instance
	 * @return an ArrayList instance of GExportRecord
	 */
	public REpiceaRecordSet selectSubsetInRecordSet(String field, Object value) {
		GExportRecord refRecord = peek();
		if (refRecord != null) {
			Vector<String> oVec = refRecord.getFieldNameList();
			int pointer = oVec.indexOf(field);
			REpiceaRecordSet exportContainingSelectedRecordSet = new REpiceaRecordSet();
			GExportRecord r;
			boolean selectRecord;
			for (Iterator<GExportRecord> recordIterator = this.iterator(); recordIterator.hasNext();) {
				selectRecord = false;
				r = (GExportRecord) recordIterator.next();
				Object valueFromRecord = r.getFieldList().get(pointer).getValue();
				if (valueFromRecord.toString().trim().toLowerCase().compareTo(value.toString().trim().toLowerCase()) == 0) {
					selectRecord = true;
				}
				if (selectRecord) {
					exportContainingSelectedRecordSet.add(r);
				}
			}	
			return exportContainingSelectedRecordSet;
		} else {		// the queue is empty
			return null;
		}
	}

	/** 
	 * This method specifies that the record set is linked to a save thread. There is a protection
	 * to avoid overfilling the REpiceaRecordSet object. 
	 * @param saveThread a Thread instance
	 */
	public void setSaveThread(Thread saveThread) {
		this.saveThread = saveThread;
	}
	
	@Override
	public boolean add(GExportRecord record) {
		if (saveThread != null && shouldBeLocked()) {
			synchronized(lock) {
				try {
					lock.wait(10000);				// waits 10 seconds if not notify and then throw an exception to avoid a dead lock
				} catch (InterruptedException e) {
					throw new BufferOverflowException();
				}	
			}
		}
		return super.add(record);
	}

	
	private boolean shouldBeLocked() {
		return size() > MAX_NUMBER_RECORDS;
	}
	
	@Override
	public GExportRecord take() throws InterruptedException {
		if (saveThread != null) {
			synchronized(lock) {
				if (!shouldBeLocked()) {
					lock.notify();
				}
			}
		}
		return super.take();
	}
	
//	public static void main(String[] args) {
//		REpiceaRecordSet recordSet = new REpiceaRecordSet();
//		
//		List<GExportRecord> records = new ArrayList<GExportRecord>();
//		for (int i = 0; i < 1000; i++) {
//			records.add(new GExportRecord());
//		}
//		recordSet.addAll(records);
//	}
	
	
}
