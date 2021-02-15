/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.components;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.io.GFileFilter.FileType;
import repicea.io.IOUserInterfaceableObject;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;

/**
 * The REpiceaMatchSelector class has a Map that related some strings to an enum variable. It has
 * a user interface that displays a table in which the user can make the different matches.
 * @author Mathieu Fortin - July 2017
 *
 * @param <E> the class of the object to be matched with the key
 */
public class REpiceaMatchSelector<E> implements REpiceaShowableUIWithParent, 
											TableModelListener, 
											IOUserInterfaceableObject, 
											Memorizable {

	
	protected final Map<Object, E> matchMap;
	protected final List<E> potentialMatches;
	protected String filename;
	protected transient REpiceaMatchSelectorDialog<E> guiInterface;
	protected final Object[] columnNames;
	
	/**
	 * Official constructor.
	 * @param toBeMatched an array of strings to be matched
	 * @param potentialMatchArray an array of enum variables
	 * @param defaultMatchId an integer which refers to the index in the potential match array. The object at this location in the potential match array
	 * is used as a default match. If the value is negative or goes beyond the length of the array, the last value of
	 * the array is selected as default match
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchSelector(Object[] toBeMatched, E[] potentialMatchArray, int defaultMatchId, Object[] columnNames) {
		potentialMatches = new ArrayList<E>();
		addMatches(potentialMatchArray);
		matchMap = new TreeMap<Object, E>();
		E defaultMatch = potentialMatchArray[potentialMatchArray.length - 1]; // we pick the last element as a default match
		if (defaultMatchId >= 0 && defaultMatchId < potentialMatchArray.length) { // however if the defaultMatchId is appropriate this can be overriden
			defaultMatch = potentialMatchArray[defaultMatchId];
		}
		int expectedNbCols = 2;
		if (defaultMatch instanceof REpiceaMatchComplexObject) {
			expectedNbCols = 2 + ((REpiceaMatchComplexObject) defaultMatch).getNbAdditionalFields();
		}
		if (expectedNbCols != columnNames.length) {
			throw new InvalidParameterException("The number of column names is inconsistent!");
		}
		this.columnNames = columnNames;
		for (Object s : toBeMatched) {
			matchMap.put(s, defaultMatch);
		}
	}

	/**
	 * Constructor with the default match being the last entry of the potential match array.
	 * @param toBeMatched an array of strings to be matched
	 * @param potentialMatchArray an array of enum variables
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchSelector(Object[] toBeMatched, E[] potentialMatchArray, Object[] columnNames) {
		this(toBeMatched, potentialMatchArray, -1, columnNames);
	}


	/**
	 * This method adds a potential treatment to the list of available treatments
	 * @param enumValues an array of enum variable 
	 */
	protected void addMatches(E[] values) {
		for (E value : values) {
			if (!potentialMatches.contains(value)) {
				potentialMatches.add(value);
			}
		}
	}
	
	protected List<E> getPotentialMatches() {return potentialMatches;}
	
	@Override
	public REpiceaMatchSelectorDialog<E> getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaMatchSelectorDialog<E>(this, (Window) parent, columnNames);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		if (guiInterface != null && guiInterface.isVisible()) {
			return true;
		}
		return false;
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			if (e.getSource() instanceof REpiceaTableModel) {
				REpiceaTableModel model = (REpiceaTableModel) e.getSource();
				int currentRow = 0;
				String s = (String) model.getValueAt(e.getLastRow(), currentRow++);
				E match = (E) model.getValueAt(e.getLastRow(), currentRow++);
				if (e.getColumn() == 1) {	// the event occurred in the match object
					matchMap.put(s, match);
					System.out.println("New match : " + s + " = " + match.toString());
					if (match instanceof REpiceaMatchComplexObject) { // means there is more information in the match object and we need to update the table
						getUI(null).doNotListenToAnymore();	// first remove the listeners to avoid looping indefinitely
						for (Object o : ((REpiceaMatchComplexObject) match).getAdditionalFields()) { // set the values that correspond to the new match
							model.setValueAt(o, e.getLastRow(), currentRow++);
						}
						getUI(null).listenTo(); // finally re-enable the listeners
					}
				} else { // it comes from the additional columns
					((REpiceaMatchComplexObject) match).setValueAt(e.getColumn() - 2,  // first two columns are the key and the match 
							guiInterface.getTable().getValueAt(e.getLastRow(), e.getColumn()));
				}
			}
		}
	}


	@Override
	public void save(String filename) throws IOException {
		setFilename(filename);
		XmlSerializer serializer = new XmlSerializer(filename);
		try {
			serializer.writeObject(this);
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while saving the file!");
		}
	}
	
	protected void setFilename(String filename) {this.filename = filename;}


	@SuppressWarnings("unchecked")
	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer;
		try {
			deserializer = new XmlDeserializer(filename);
		} catch (Exception e) {
			InputStream is = getClass().getResourceAsStream("/" + filename);
			if (is == null) {
				throw new IOException("The filename is not a file and cannot be converted into a stream!");
			} else {
				deserializer = new XmlDeserializer(is);
			}
		}
		REpiceaMatchSelector<E> newloadedInstance;
		try {
			newloadedInstance = (REpiceaMatchSelector<E>) deserializer.readObject();
			unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
			setFilename(filename);
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while loading the file!");
		}
	}


	@Override
	public FileFilter getFileFilter() {return FileType.XML.getFileFilter();}


	@Override
	public String getFilename() {return filename;}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add((Serializable) matchMap);
		mp.add((Serializable) potentialMatches);
		return mp;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		matchMap.clear();
		matchMap.putAll((Map) wasMemorized.get(0));
		potentialMatches.clear();
		potentialMatches.addAll((List) wasMemorized.get(1));
	}

	/**
	 * This method returns the match corresponding to the parameter.
	 * @param obj
	 * @return an Object of class E
	 */
	public E getMatch(Object obj) {
		return matchMap.get(obj);
	}
	
}
