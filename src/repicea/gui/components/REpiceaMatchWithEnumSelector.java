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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import repicea.app.UseModeProvider.UseMode;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.REpiceaFileFilter.FileType;
import repicea.io.REpiceaFileFilterList;
import repicea.serial.MarshallingException;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The REpiceaEnhancedMatchSelector class is similar to the
 * REpiceaMatchSelector class, except that it allows for multiple
 * categories. The categories are defined by a list of Enum variables
 * specified in the constructor.
 * @author Mathieu Fortin - December 2024
 *
 * @param <K> the class of the key of the REpiceaMatch instance
 * @param <V> the class of the value to which the key is matched (typically an Enum)
 * could be either an Enum or a REpiceaMatchComplexObject-derived class
 * 
 * @see REpiceaMatchComplexObject
 */
public class REpiceaMatchWithEnumSelector<K,V extends Enum<?>> implements REpiceaShowableUIWithParent, 
											IOUserInterfaceableObject, 
											Memorizable {

	public enum DefaultSingleCategory implements TextableEnum { 
		SingleCategory("Default", "D\u00E9faut");
		DefaultSingleCategory(String englishName, String frenchName) {
			this.setText(englishName, frenchName);
		}
		
		@Override 
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	protected final Map<Enum<?>, Map<K, REpiceaMatchWithEnumObject<K, V>>> matchMaps;
	protected final Map<Enum<?>, List<V>> potentialMatchesMap;
	protected String filename;
	protected transient REpiceaMatchWithEnumSelectorDialog guiInterface;
	protected final ArrayList<String> columnNames;
	
	
	/**
	 * Official constructor.
	 * @param categories a List of Enum variables defining the categories
	 * @param toBeMatched an array of REpiceaMatch instances
	 * @param defaultValueMatch an integer which refers to the index in the potential match array. The object at this location in the potential match array
	 * is used as a default match. If the value is negative or goes beyond the length of the array, the last value of
	 * the array is selected as default match
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	@SuppressWarnings("unchecked")
	public REpiceaMatchWithEnumSelector(List<Enum<?>> categories, 
			REpiceaMatchWithEnumObject<K,V>[] toBeMatched,
			int defaultValueMatch, 
			String[] columnNames) {
		this();
		V[] acceptableValues = (V[]) toBeMatched[0].getValue().getClass().getEnumConstants();
		for (Enum<?> thisEnum : categories) {
			List<V> thisEnumList = new ArrayList<V>();
			potentialMatchesMap.put(thisEnum, thisEnumList);
			addMatches(thisEnum, acceptableValues);		// remove duplicates
		}
		List<V> listOfPotentialMatches = potentialMatchesMap.values().iterator().next();
		int defaultMatchIndex = listOfPotentialMatches.size() - 1; // default match is the last one
		if (defaultValueMatch >= 0 && defaultValueMatch < listOfPotentialMatches.size()) { // however if the defaultMatchId is appropriate this can be overriden
			defaultMatchIndex = defaultValueMatch;
		}
		V defaultValue = listOfPotentialMatches.get(defaultMatchIndex);
		
		int expectedNbCols = 2 + toBeMatched[0].getNbAdditionalFields();
		if (expectedNbCols != columnNames.length) {
			throw new InvalidParameterException("The number of column names is inconsistent!");
		}
		
		this.columnNames.addAll(Arrays.asList(columnNames));
		
//		instantiatePotentialMatchesByKey(categories, toBeMatched);

		for (Enum<?> thisEnum : categories) {
			matchMaps.put(thisEnum, new TreeMap<K, REpiceaMatchWithEnumObject<K,V>>());
			for (REpiceaMatchWithEnumObject<K,V> s : toBeMatched) {
				REpiceaMatchWithEnumObject<K,V> clone = s.getDeepClone();
				clone.setValue(defaultValue);
				matchMaps.get(thisEnum).put(clone.getKey(), clone);
			}
		}
	}
	
	
	/**
	 * Official constructor with single category.
	 * @param toBeMatched an array of REpiceaMatch instances
	 * @param defaultValueMatch an integer which refers to the index in the potential match array. The object at this location in the potential match array
	 * is used as a default match. If the value is negative or goes beyond the length of the array, the last value of
	 * the array is selected as default match
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchWithEnumSelector(REpiceaMatchWithEnumObject<K,V>[] toBeMatched,
			int defaultValueMatch, 
			String[] columnNames) {
		this(Arrays.asList(DefaultSingleCategory.values()), toBeMatched, defaultValueMatch, columnNames);
	}


	/**
	 * Constructor with the default match being the last entry of the potential match array.
	 * @param categories a List of Enum variables defining the categories
	 * @param toBeMatched an array of REpiceaMatch instances
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchWithEnumSelector(List<Enum<?>> categories, 
			REpiceaMatchWithEnumObject<K,V>[] toBeMatched,
			String[] columnNames) {
		this(categories, toBeMatched, -1, columnNames);
	}

	/**
	 * Constructor with the default match being the last entry of the potential match array.
	 * @param toBeMatched an array of REpiceaMatch instances
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchWithEnumSelector(REpiceaMatchWithEnumObject<K,V>[] toBeMatched,
			String[] columnNames) {
		this(Arrays.asList(DefaultSingleCategory.values()), toBeMatched, -1, columnNames);
	}


	/**
	 * Default contructor for loading from scratch.
	 */
	private REpiceaMatchWithEnumSelector() {
		potentialMatchesMap = new HashMap<Enum<?>, List<V>>();
		columnNames = new ArrayList<String>();
		matchMaps = new LinkedHashMap<Enum<?>, Map<K, REpiceaMatchWithEnumObject<K, V>>>();
	}

	
	
	/**
	 * Add a potential treatment to the list of available treatments.
	 * @param thisEnum the enum variable standing for the category
	 * @param values an array of enum variable 
	 */
	protected void addMatches(Enum<?> thisEnum, V[] values) {
		List<V> thisEnumList = potentialMatchesMap.get(thisEnum);
		for (V value : values) {
			if (!thisEnumList.contains(value)) {
				thisEnumList.add(value);
			}
		}
	}
	
	protected List<V> getPotentialMatches(Enum<?> thisEnum) {return potentialMatchesMap.get(thisEnum);}
	
	@Override
	public REpiceaMatchWithEnumSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaMatchWithEnumSelectorDialog(this, (Window) parent, columnNames.toArray());
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
		REpiceaMatchWithEnumSelector<K,V> newloadedInstance;
		try {
			newloadedInstance = (REpiceaMatchWithEnumSelector<K,V>) deserializer.readObject();
			unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
			setFilename(filename);
		} catch (UnmarshallingException e) {
			throw new IOException("A UnmarshallException occurred while loading the file!");
		}
	}

	/**
	 * Load an instance of this class from a file.
	 * @param filename the filename 
	 * @return An REpiceaEnhancedMatchSelector instance
	 * @throws IOException if an I/O error occurs
	 */
	@SuppressWarnings("rawtypes")
	public static REpiceaMatchWithEnumSelector<?, ?> Load(String filename) throws IOException {
		REpiceaMatchWithEnumSelector<?,?> newInstance = new REpiceaMatchWithEnumSelector();
		newInstance.load(filename);
		return newInstance;
	}
	
	@Override
	public REpiceaFileFilterList getFileFilters() {return new REpiceaFileFilterList(FileType.XML.getFileFilter());}


	@Override
	public String getFilename() {return filename;}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add((Serializable) matchMaps);
		mp.add((Serializable) potentialMatchesMap);
		mp.add(columnNames);
//		mp.add((Serializable) potentialMatchesByKeyMap);
		return mp;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		matchMaps.clear();
		matchMaps.putAll((Map) wasMemorized.get(0));
		potentialMatchesMap.clear();
		potentialMatchesMap.putAll((Map) wasMemorized.get(1));
		columnNames.clear();
		columnNames.addAll((List) wasMemorized.get(2));
//		potentialMatchesByKeyMap = (Map) wasMemorized.get(3);
	}

	/**
	 * Provide the match corresponding to the parameter.<p>
	 * This method is synchronized since the underlying maps are
	 * LinkedHashMap and TreeMap instances. The LinkedHashMap.get
	 * method is known to induce structure changes in the map and 
	 * therefore, there is a possibility of concurrent changes.
	 * @param thisEnum the enum variable standing for the category
	 * @param obj the Object instance for which we want the match
	 * @return an Object of class E or null if there is no match map for thisEnum.
	 */
	public synchronized REpiceaMatchWithEnumObject<K,V> getMatch(Enum<?> thisEnum, K obj) {
		return matchMaps.containsKey(thisEnum) ? matchMaps.get(thisEnum).get(obj) : null;
	}
	
	/**
	 * Provide the match corresponding to the parameter.<p>
	 * This method is synchronized since the underlying maps are
	 * LinkedHashMap and TreeMap instances. The LinkedHashMap.get
	 * method is known to induce structure changes in the map and 
	 * therefore, there is a possibility of concurrent changes.
	 * @param obj the Object instance for which we want the match
	 * @return an Object of class E or null if there is no match map for thisEnum.
	 */
	public REpiceaMatchWithEnumObject<K,V> getMatch(K obj) {
		if (!matchMaps.containsKey(DefaultSingleCategory.SingleCategory)) {
			throw new UnsupportedOperationException("The match map does not contain a unique default category!");
		}
		return matchMaps.get(DefaultSingleCategory.SingleCategory).get(obj);
	}
	
	
	
	static class MyComplexObjectClass implements REpiceaMatchWithEnumObject<String, UseMode> {

		UseMode name;
		int index;
		String key;
		
		MyComplexObjectClass(String key, UseMode name, int index) {
			this.name = name;
			this.index = index;
			this.key = key;
		}
		
		@Override
		public int getNbAdditionalFields() {
			return 1;
		}

		@Override
		public List<Object> getAdditionalFields() {
			List<Object> myList = new ArrayList<Object>();
			myList.add(index);
			return myList;
		}

		@Override
		public void setValueAt(int indexOfThisAdditionalField, Object value) {
			if (indexOfThisAdditionalField == 0) {
				this.index = (Integer) value;
			}
		}

		@Override
		public UseMode getValue() {
			return name;
		}

		@Override
		public REpiceaMatchWithEnumObject<String, UseMode> getDeepClone() {
			return new MyComplexObjectClass(key, name, index);
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public void setValue(UseMode value) {
			name = value;
		}

		
	}

	
	public static void main(String[] args) {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		int i = 0;
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(("a" + i++), sc, sc.ordinal()));
		}
		REpiceaMatchWithEnumSelector<String, UseMode> selector = new REpiceaMatchWithEnumSelector<String, UseMode>(Arrays.asList(Language.values()),
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		selector.showUI(null);
	}

}
