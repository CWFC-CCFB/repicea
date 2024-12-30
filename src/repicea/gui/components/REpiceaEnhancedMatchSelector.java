/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.components.REpiceaEnhancedMatchSelectorDialog.REpiceaMatchMapTableModel;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.REpiceaFileFilter.FileType;
import repicea.io.REpiceaFileFilterList;
import repicea.serial.MarshallingException;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;

/**
 * The REpiceaEnhancedMatchSelector class is similar to the
 * REpiceaMatchSelector class, except that it allows for multiple
 * categories. The categories are defined by a list of Enum variables
 * specified in the constructor.
 * @author Mathieu Fortin - December 2024
 *
 * @param <E> the class of the object to be matched with the key, 
 * could be either an Enum or a REpiceaMatchComplexObject-derived class
 * 
 * @see REpiceaMatchComplexObject
 */
public class REpiceaEnhancedMatchSelector<E> implements REpiceaShowableUIWithParent, 
											TableModelListener, 
											IOUserInterfaceableObject, 
											Memorizable {

	
	protected final Map<Enum<?>, Map<Object, E>> matchMap;
	protected final Map<Enum<?>, List<E>> potentialMatches;
	protected String filename;
	protected transient REpiceaEnhancedMatchSelectorDialog<E> guiInterface;
	protected final Object[] columnNames;
	
	protected Map<Enum<?>, Map<Object, Map<E, E>>> potentialMatchesByKey;
	
	
	
	/**
	 * Official constructor.
	 * @param categories a List of Enum variables defining the categories
	 * @param toBeMatched an array of strings to be matched
	 * @param potentialMatchArray an array of enum variables
	 * @param defaultMatchId an integer which refers to the index in the potential match array. The object at this location in the potential match array
	 * is used as a default match. If the value is negative or goes beyond the length of the array, the last value of
	 * the array is selected as default match
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	@SuppressWarnings("unchecked")
	public REpiceaEnhancedMatchSelector(List<Enum<?>> categories, Object[] toBeMatched, E[] potentialMatchArray, int defaultMatchId, Object[] columnNames) {
		potentialMatches = new HashMap<Enum<?>, List<E>>();
		for (Enum<?> thisEnum : categories) {
			List<E> thisEnumList = new ArrayList<E>();
			potentialMatches.put(thisEnum, thisEnumList);
			addMatches(thisEnum, potentialMatchArray);		// remove duplicates
		}
		List<E> listOfPotentialMatches = potentialMatches.values().iterator().next();
		int defaultMatchIndex = listOfPotentialMatches.size() - 1; // default match is the last one
		if (defaultMatchId >= 0 && defaultMatchId < listOfPotentialMatches.size()) { // however if the defaultMatchId is appropriate this can be overriden
			defaultMatchIndex = defaultMatchId;
		}
		
		int expectedNbCols = 2;
		E defaultMatch = listOfPotentialMatches.get(defaultMatchIndex);
		if (defaultMatch instanceof REpiceaMatchComplexObject) {
			expectedNbCols = 2 + ((REpiceaMatchComplexObject<E>) defaultMatch).getNbAdditionalFields();
		}
		if (expectedNbCols != columnNames.length) {
			throw new InvalidParameterException("The number of column names is inconsistent!");
		}
		this.columnNames = columnNames;
		
		instantiatePotentialMatchesByKey(categories, toBeMatched);

		matchMap = new LinkedHashMap<Enum<?>, Map<Object, E>>();
		for (Enum<?> thisEnum : categories) {
			Map<Object, E> innerMap = new TreeMap<Object, E>();
			matchMap.put(thisEnum, innerMap);
			for (Object s : toBeMatched) {
				Map<E, E> tmpMap = getMatchesForThisKey(thisEnum, s);
				E defaultMatchForThisKey = tmpMap.get(defaultMatch);
				innerMap.put(s, defaultMatchForThisKey);
			}
		}
	}

	/**
	 * Constructor with the default match being the last entry of the potential match array.
	 * @param categories a List of Enum variables defining the categories
	 * @param toBeMatched an array of strings to be matched
	 * @param potentialMatchArray an array of enum variables
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaEnhancedMatchSelector(List<Enum<?>> categories, Object[] toBeMatched, E[] potentialMatchArray, Object[] columnNames) {
		this(categories, toBeMatched, potentialMatchArray, -1, columnNames);
	}


	/**
	 * Add a potential treatment to the list of available treatments.
	 * @param thisEnum the enum variable standing for the category
	 * @param values an array of enum variable 
	 */
	protected void addMatches(Enum<?> thisEnum, E[] values) {
		List<E> thisEnumList = potentialMatches.get(thisEnum);
		for (E value : values) {
			if (!thisEnumList.contains(value)) {
				thisEnumList.add(value);
			}
		}
	}
	
	protected List<E> getPotentialMatches(Enum<?> thisEnum) {return potentialMatches.get(thisEnum);}
	
	@Override
	public REpiceaEnhancedMatchSelectorDialog<E> getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaEnhancedMatchSelectorDialog<E>(this, (Window) parent, columnNames);
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
	private void instantiatePotentialMatchesByKey(List<Enum<?>> categories, Object[] toBeMatched) {
		potentialMatchesByKey = new HashMap<Enum<?>, Map<Object, Map<E, E>>>();
		for (Enum<?> thisEnum : categories) {
			Map<Object, Map<E,E>> innerMap = new HashMap<Object, Map<E, E>>();
			potentialMatchesByKey.put(thisEnum, innerMap);
			for (Object obj : toBeMatched) {
				Map<E, E> individualInstancesMap = new HashMap<E, E>();
				innerMap.put(obj, individualInstancesMap);
				for (E e : potentialMatches.get(thisEnum)) {
					if (e instanceof REpiceaMatchComplexObject) {
						individualInstancesMap.put(e, ((REpiceaMatchComplexObject<E>) e).getDeepClone());
					} else {
						individualInstancesMap.put(e, e);
					}
				}
			}
		}
	}
	
	
	private Map<E, E> getMatchesForThisKey(Enum<?> thisEnum, Object key) {
		return potentialMatchesByKey.get(thisEnum).get(key);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			if (e.getSource() instanceof REpiceaMatchMapTableModel) {
				REpiceaMatchMapTableModel model = (REpiceaMatchMapTableModel) e.getSource();
				if (e.getColumn() == 1) {	// the event occurred in the match object
					String s = (String) model.getValueAt(e.getLastRow(), 0);
					E m = (E) model.getValueAt(e.getLastRow(), 1);
					Map<E,E> potentialMatchesForThisKey = getMatchesForThisKey(model.enumForThisTableModel, s);
					E trueMatch = potentialMatchesForThisKey.get(m);
					matchMap.get(model.enumForThisTableModel).put(s, trueMatch);
					getUI(null).doNotListenToAnymore();	// first remove the listeners to avoid looping indefinitely
					model.setValueAt(trueMatch, e.getLastRow(), 1);
					System.out.println("New match : " + s + " = " + trueMatch.toString());
					if (trueMatch instanceof REpiceaMatchComplexObject) { // means there is more information in the match object and we need to update the table
						int currentColumn = 2;
						for (Object o : ((REpiceaMatchComplexObject<E>) trueMatch).getAdditionalFields()) { // set the values that correspond to the new match
							model.setValueAt(o, e.getLastRow(), currentColumn++);
						}
					}
					getUI(null).listenTo(); // finally re-enable the listeners
				} else { // it comes from the additional columns
					E m = (E) model.getValueAt(e.getLastRow(), 1);
					((REpiceaMatchComplexObject<E>) m).setValueAt(e.getColumn() - 2,  // first two columns are the key and the match 
							guiInterface.getTable(model.enumForThisTableModel).getValueAt(e.getLastRow(), e.getColumn()));
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
		} catch (MarshallingException e) {
			throw new IOException("A MarshallingException occurred while saving the file!");
		}
	}
	
	protected void setFilename(String filename) {this.filename = filename;}


	@SuppressWarnings("unchecked")
	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		REpiceaEnhancedMatchSelector<E> newloadedInstance;
		try {
			newloadedInstance = (REpiceaEnhancedMatchSelector<E>) deserializer.readObject();
			unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
			setFilename(filename);
		} catch (UnmarshallingException e) {
			throw new IOException("A UnmarshallException occurred while loading the file!");
		}
	}


	@Override
	public REpiceaFileFilterList getFileFilters() {return new REpiceaFileFilterList(FileType.XML.getFileFilter());}


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
		potentialMatches.putAll((Map) wasMemorized.get(1));
	}

	/**
	 * This method returns the match corresponding to the parameter.
	 * @param thisEnum the enum variable standing for the category
	 * @param obj the Object instance for which we want the match
	 * @return an Object of class E or null if there is no match map for thisEnum.
	 */
	public E getMatch(Enum<?> thisEnum, Object obj) {
		return matchMap.containsKey(thisEnum) ? 
			matchMap.get(thisEnum).get(obj) :
				null;
	}
	
}
