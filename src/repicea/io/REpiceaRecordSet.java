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

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This subclass of ArrayList only implements an additional method in order to enable 
 * the selection of subsets of GExportRecord objects.
 * @author Mathieu Fortin - April 2011
 */
public class REpiceaRecordSet extends LinkedBlockingQueue<GExportRecord> {

	private static final long serialVersionUID = 20110410L;

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
	
}
