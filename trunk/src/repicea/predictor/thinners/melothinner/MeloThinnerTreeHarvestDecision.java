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
import repicea.predictor.QuebecGeneralSettings;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class MeloThinnerTreeHarvestDecision implements REpiceaShowableUIWithParent, TableModelListener, IOUserInterfaceableObject {

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
		
	protected final Map<String, TextableEnum> treatmentMatchMap;
	protected List<TextableEnum> potentialTreatments;
	private MeloThinnerTreeHarvestDecisionDialog guiInterface;
	
	protected MeloThinnerTreeHarvestDecision() {
		potentialTreatments = new ArrayList<TextableEnum>();
		addPotentialTreatment(BasicDefaultTreatment.values());
		treatmentMatchMap = new TreeMap<String, TextableEnum>();
		for (String potentialVegetation : QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST) {
			treatmentMatchMap.put(potentialVegetation, BasicDefaultTreatment.CPRS);
		}
	}

	
	public void addPotentialTreatment(TextableEnum[] enumValues) {
		for (TextableEnum enumValue : enumValues) {
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
				TextableEnum treatement = (TextableEnum) model.getValueAt(e.getLastRow(), 1);
				treatmentMatchMap.put(potentialVegetation, treatement);
			}
		}
	}


	@Override
	public void save(String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void load(String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public FileFilter getFileFilter() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
