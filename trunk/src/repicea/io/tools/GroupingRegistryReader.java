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
package repicea.io.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import repicea.app.AbstractGenericTask;
import repicea.io.FormatReader;

/**
 * This private class handles the index of a data set that contains one or many grouping. 
 * @author Mathieu Fortin - October 2011 
 */
class GroupingRegistryReader extends AbstractGenericTask implements Serializable {

	private static final long serialVersionUID = 20100804L;

	/*
	 * Members of this class
	 */
	protected Map<String, List<Integer>> groupMap;
	protected ImportFieldManager importFieldManager;
	
	private Vector<String> groupList;
	private boolean groupFieldEnabled;

	/**	
	 * Script constructor. General for any model.
	 */
	protected GroupingRegistryReader(ImportFieldManager importFieldManager) {
		groupList  = new Vector<String>(); 
		groupMap = new TreeMap<String, List<Integer>>();
		groupFieldEnabled = false;

		this.importFieldManager = importFieldManager;
	}


	/**
	 * This method scans the strata throughout the input file.
	 */
	@SuppressWarnings("rawtypes")
	protected void doThisJob() throws Exception {
		groupList.clear();
		groupMap.clear();
		Enum stratumEnum = importFieldManager.getStratumFieldEnum();
		if (stratumEnum != null && importFieldManager.getField(stratumEnum).getMatchingFieldIndex() != -1) {		// means a stratum field has been selected
			List<Integer> index = null;
			try {
				FormatReader formatReader = importFieldManager.instantiateFormatReader();
				double progressFactor = (double) 100d / formatReader.getRecordCount();

				// Now, lets start reading the rows

				Object[] rowObjects;
				int indexOfStratumField = importFieldManager.getIndexOfThisField(importFieldManager.getStratumFieldEnum());

				int iFieldStratum = importFieldManager.getFields().get(indexOfStratumField).getMatchingFieldIndex();
				String strStratum = null;
				String strStratumLast = "";
				boolean bStratumChanged = false; 

				int line = 0;
				while( (rowObjects = formatReader.nextRecord()) != null && !isCancelled) {
					strStratum = ((Object) rowObjects[iFieldStratum]).toString().trim();
					bStratumChanged = (strStratum.compareTo(strStratumLast) != 0); 

					if (strStratum!=null) {
						if (bStratumChanged) {
							index = groupMap.get(strStratum);
							if (index==null) {
								index = new ArrayList<Integer>();
								groupMap.put(strStratum, index);
							}
						}

						index.add(line);

						if (bStratumChanged) {
							if (!groupList.contains(strStratum)) {
								groupList.add(strStratum);
							}
						}

						strStratumLast = strStratum;
					}
					line++;
					setProgress((int) (line * progressFactor));
				}
				// By now, we have iterated through all of the rows
				formatReader.close();
				groupFieldEnabled = true;
			} catch (Exception e) {
				System.out.println("Error while loading file: " + importFieldManager.getFileSpecifications());
				e.printStackTrace();
				throw e;
			} 
		} 
	}


	/*
	 * Accessors
	 */
	protected boolean isGroupingEnabled() {return groupFieldEnabled;}
	
	protected Vector<String> getGroupList() {
		return groupList;
	}

	protected List<Integer> getObservationIndicesForThisGroup(int groupPositionID) {
		if (groupFieldEnabled) {
			return groupMap.get(getGroupName(groupPositionID));
		} else {
			return null;
		}
	}

	protected String getGroupName(int groupPositionID) {
		if (groupFieldEnabled) {
			return groupList.get(groupPositionID);
		} else {
			return null;
		}
	}

	protected Map<String, List<Integer>> getGroupMap() {
		if (groupFieldEnabled) {
			return groupMap;
		}
		else {
			return null;
		}
	}


}
