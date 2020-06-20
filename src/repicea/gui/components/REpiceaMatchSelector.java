/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge Epicea.
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
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

/**
 * The REpiceaMatchSelector class has a Map that related some strings to an enum variable. It has
 * a user interface that displays a table in which the user can make the different matches.
 * @author Mathieu Fortin - July 2017
 *
 * @param <E> an Enum
 */
public class REpiceaMatchSelector<E extends Enum<E>> implements REpiceaShowableUIWithParent, 
											TableModelListener, 
											IOUserInterfaceableObject, 
											Memorizable {

	
	protected final Map<Object, Enum<E>> matchMap;
	protected final List<Enum<E>> potentialMatches;
	protected String filename;
	protected transient REpiceaMatchSelectorDialog guiInterface;
	protected final Object[] columnNames;
	
	/**
	 * Official constructor
	 * @param toBeMatched an array of strings to be matched
	 * @param potentialMatchArray an array of enum variables
	 * @param defaultMatch an enum variable that acts as a default match
	 * @param columnNames an array of object (Strings or Enum) for column titles
	 */
	public REpiceaMatchSelector(Object[] toBeMatched, Enum<E>[] potentialMatchArray, Enum<E> defaultMatch, Object[] columnNames) {
		potentialMatches = new ArrayList<Enum<E>>();
		addMatches(potentialMatchArray);
		matchMap = new TreeMap<Object, Enum<E>>();
		for (Object s : toBeMatched) {
			matchMap.put(s, defaultMatch);
		}
		this.columnNames = columnNames;
	}
	

	/**
	 * This method adds a potential treatment to the list of available treatments
	 * @param enumValues an array of enum variable 
	 */
	protected void addMatches(Enum<E>[] enumValues) {
		for (Enum<E> enumValue : enumValues) {
			if (!potentialMatches.contains(enumValue)) {
				potentialMatches.add(enumValue);
			}
		}
	}
	
	protected List<Enum<E>> getPotentialMatches() {return potentialMatches;}
	
	@Override
	public REpiceaMatchSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaMatchSelectorDialog(this, (Window) parent, columnNames);
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
				String s = (String) model.getValueAt(e.getLastRow(), 0);
				Enum<E> match = (Enum<E>) model.getValueAt(e.getLastRow(), 1);
				matchMap.put(s, match);
				System.out.println("new match : " + s + " = " + match.name());
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
//			InputStream is = ClassLoader.getSystemResourceAsStream(filename);
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
	 * @return an Enum of class E
	 */
	public Enum<E> getMatch(Object obj) {
		return matchMap.get(obj);
	}
	
	
	public static void main(String[] args) {
		REpiceaMatchSelector<StatusClass> selector = new REpiceaMatchSelector<StatusClass>(new String[]{"a","b","c","d","e","f"},
				StatusClass.values(), 
				StatusClass.alive, 
				new String[]{"string", "status"});
		selector.showUI(null);
		boolean cancelled = selector.getUI(null).hasBeenCancelled();
		System.out.println("The dialog has been cancelled : " + cancelled);
	}

}
