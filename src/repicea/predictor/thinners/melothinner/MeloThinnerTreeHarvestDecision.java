/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.thinners.melothinner;

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
import repicea.gui.components.REpiceaTableModel;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.GFileFilter.FileType;
import repicea.predictor.QuebecGeneralSettings;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class MeloThinnerTreeHarvestDecision implements REpiceaShowableUIWithParent, TableModelListener, IOUserInterfaceableObject, Memorizable {

	public static enum BasicDefaultTreatment implements TextableEnum {
		CPRS("Harvesting with soil and regeneration protection", "Coupe avec protection de la r\u00E9g\u00E9n\u00E9ration et des sols (CPRS)"),
		CPPTM("Harvesting with advanced regeneration protection (HARP)", "Coupe avec protection des petites tiges marchandes (CPPTM)");
		
		BasicDefaultTreatment(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
		
	protected final Map<String, Enum<?>> treatmentMatchMap;
	protected final List<Enum<?>> potentialTreatments;
	private String filename;

	private transient MeloThinnerTreeHarvestDecisionDialog guiInterface;

	protected MeloThinnerTreeHarvestDecision() {
		potentialTreatments = new ArrayList<Enum<?>>();
		addPotentialTreatment(BasicDefaultTreatment.values());
		treatmentMatchMap = new TreeMap<String, Enum<?>>();
		for (String potentialVegetation : QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST) {
			treatmentMatchMap.put(potentialVegetation, BasicDefaultTreatment.CPRS);
		}
	}

	
	public void addPotentialTreatment(Enum<?>[] enumValues) {
		for (Enum<?> enumValue : enumValues) {
			if (!potentialTreatments.contains(enumValue)) {
				potentialTreatments.add(enumValue);
			}
		}
	}
	
	
	
	@Override
	public MeloThinnerTreeHarvestDecisionDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new MeloThinnerTreeHarvestDecisionDialog(this, (Window) parent);
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

	
	public static void main(String[] args) {
		new MeloThinnerTreeHarvestDecision().showUI(null);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			if (e.getSource() instanceof REpiceaTableModel) {
				REpiceaTableModel model = (REpiceaTableModel) e.getSource();
				String potentialVegetation = (String) model.getValueAt(e.getLastRow(), 0);
				Enum<?> treatement = (Enum<?>) model.getValueAt(e.getLastRow(), 1);
				treatmentMatchMap.put(potentialVegetation, treatement);
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
	
	private void setFilename(String filename) {this.filename = filename;}


	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer;
		try {
			deserializer = new XmlDeserializer(filename);
		} catch (Exception e) {
			InputStream is = ClassLoader.getSystemResourceAsStream(filename);
			if (is == null) {
				throw new IOException("The filename is not a file and cannot be converted into a stream!");
			} else {
				deserializer = new XmlDeserializer(is);
			}
		}
		MeloThinnerTreeHarvestDecision newloadedInstance;
		try {
			newloadedInstance = (MeloThinnerTreeHarvestDecision) deserializer.readObject();
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
		mp.add((Serializable) treatmentMatchMap);
		mp.add((Serializable) potentialTreatments);
		return mp;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		treatmentMatchMap.clear();
		treatmentMatchMap.putAll((Map) wasMemorized.get(0));
		potentialTreatments.clear();
		potentialTreatments.addAll((List) wasMemorized.get(1));
	}
	
}
