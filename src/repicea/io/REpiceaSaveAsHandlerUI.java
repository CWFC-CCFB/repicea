/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge-Epicea
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

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.UIControlManager;
import repicea.util.REpiceaTranslator;

/**
 * An abstract class that handles the save as action. Provide
 * a FileChooser first and ask the user if he/she wants to 
 * overwrite if the file exists.
 * @author Mathieu Fortin - May 2021
 */
public abstract class REpiceaSaveAsHandlerUI {

	protected final Component component;
	private final REpiceaFileFilterList fileFilters;
	
	
	protected REpiceaSaveAsHandlerUI(Component component, REpiceaFileFilterList fFilters) {
		this.component = component;
		this.fileFilters = new REpiceaFileFilterList(fFilters);
		if (fFilters != null) {
			fileFilters.addAll(fFilters);
		} 
	}

	protected String getFilename() {
		return "";
	}

	protected abstract void internalSaveAction(String filename) throws Exception;
	
	protected void showSaveActionFailedMessage() {
		JOptionPane.showMessageDialog((Component) component, 
				REpiceaTranslator.getString(UIControlManager.InformationMessage.ErrorWhileSavingData),
				REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
				JOptionPane.ERROR_MESSAGE);
	}

	
	protected boolean saveAsAction() {
		try {
			FileChooserOutput fileChooserOutput = CommonGuiUtility.browseAction(component,
					JFileChooser.FILES_ONLY,
					getFilename(),
					fileFilters,
					JFileChooser.SAVE_DIALOG);
			if (fileChooserOutput.isValid()) {
				if (new File(fileChooserOutput.getFilename()).exists()) {
					if (!CommonGuiUtility.popupWriteOverWarningDialog((Component) component)) {
						return false;
					}
				}
				internalSaveAction(fileChooserOutput.getFilename());
				return true;
			} else {
				return false;	// file chooser has been cancelled
			}
		} catch (Exception e) {
			e.printStackTrace();
			showSaveActionFailedMessage();
			return false;
		}
	}

	
	
	
	
}
