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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import repicea.app.SettingMemory;
import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaWindow;
import repicea.gui.UIControlManager;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The IOFileHandlerUI class handles the save, save as and load actions in REpicea windows.
 * @author Mathieu Fortin - May 2014
 */
public class REpiceaIOFileHandlerUI extends REpiceaSaveAsHandlerUI implements ActionListener, PropertyChangeListener {

	protected static enum MessageID implements TextableEnum {
		
		ParamsHaveChanged("The parameters have been changed. Do you want to save them?",
				"Les param\u00E8tres ont \u00E9t\u00E9 chang\u00E9s. D\u00E9sirez-vous les sauvegarder ?");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}

	
//	private final IOUserInterface component;
	private final IOUserInterfaceableObject componentOwner;
	private final AbstractButton saveButton;
	private final AbstractButton saveAsButton;
	private final AbstractButton loadButton;
	
	private boolean hasChanged;

	/**
	 * Handle the load, save and save as menu item in a dialog. If the component parameter
	 * is not a component instance, the constructor will throw a ClassCastException.
	 * 
	 * @param component Must be a Component instance implementing the IOUserInterface
	 * @param componentOwner an IOUserInterfaceableObject instance
	 * @param saveButton
	 * @param saveAsButton
	 * @param loadButton
	 */
	public REpiceaIOFileHandlerUI(IOUserInterface component, 
			IOUserInterfaceableObject componentOwner,
			AbstractButton saveButton,
			AbstractButton saveAsButton,
			AbstractButton loadButton) {
		super((Component) component, componentOwner.getFileFilter());
		this.component.addPropertyChangeListener(this);
		this.componentOwner = componentOwner;
		getIOUserInterface().firePropertyChange(REpiceaAWTProperty.DisconnectAutoShutdown, null, this);
		this.saveButton = saveButton;
		this.saveButton.addActionListener(this);
		
		this.saveAsButton = saveAsButton;
		this.saveAsButton.addActionListener(this);

		this.loadButton = loadButton;
		this.loadButton.addActionListener(this);
	}

	
	private IOUserInterface getIOUserInterface() {
		return (IOUserInterface) component;
	}
	
	
	/**
	 * This method loads a previously serialized instance.
	 */
	protected void loadAction() {
		try {
			SettingMemory settings = getIOUserInterface().getWindowSettings();
			String filename;
			if (settings != null) {
				filename = settings.getProperty(component.getClass().getSimpleName() + ".last.file.loaded", componentOwner.getFilename());
			} else {
				filename = componentOwner.getFilename();
			}
			List<FileFilter> fileFilters = new ArrayList<FileFilter>();
			fileFilters.add(componentOwner.getFileFilter());
			
			FileChooserOutput fileChooserOutput = CommonGuiUtility.browseAction((Component) component,
					JFileChooser.FILES_ONLY, 
					filename,
					fileFilters,
					JFileChooser.OPEN_DIALOG);		// false : not restricted
			
			if (fileChooserOutput.isValid()) {
				componentOwner.load(fileChooserOutput.getFilename());
				getIOUserInterface().postLoadingAction();
				hasChanged = false;
				getIOUserInterface().firePropertyChange(REpiceaAWTProperty.JustLoaded,	null, component);
				if (settings != null) {
					settings.setProperty(component.getClass().getSimpleName() + ".last.file.loaded", fileChooserOutput.getFilename());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog((Component) component, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.ErrorWhileLoadingData),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void saveAction() {
		if (componentOwner.getFilename() != null && new File(componentOwner.getFilename()).isFile()) {
			try {
				internalSaveAction(componentOwner.getFilename());
			} catch (Exception e) {
				e.printStackTrace();
				showSaveActionFailedMessage();
			}
		} else {
			saveAsAction();
		}
	}

	
	@Override
	protected void internalSaveAction(String filename) throws Exception {
		componentOwner.save(filename);
		getIOUserInterface().postSavingAction();
		hasChanged = false;
		getIOUserInterface().firePropertyChange(REpiceaAWTProperty.JustSaved, null, component);
	}

	
	@Override
	protected String getFilename() {
		return componentOwner.getFilename();
	}
	
	
//	protected boolean saveAsAction() {
//		try {
////			String filename = componentOwner.getFilename();
////			List<FileFilter> fileFilters = new ArrayList<FileFilter>();
////			fileFilters.add(componentOwner.getFileFilter());
//			FileChooserOutput fileChooserOutput = CommonGuiUtility.browseAction((Component) component,
//					JFileChooser.FILES_ONLY,
//					filename,
//					fileFilters,
//					JFileChooser.SAVE_DIALOG);
//			if (fileChooserOutput.isValid()) {
//				if (new File(fileChooserOutput.getFilename()).exists()) {
//					if (!CommonGuiUtility.popupWriteOverWarningDialog((Component) component)) {
//						return false;
//					}
//				}
//				internalSaveAction(fileChooserOutput.getFilename());
//				return true;
//			} else {
//				return false;	// file chooser has been cancelled
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			showSaveActionFailedMessage();
//			return false;
//		}
//	}


	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(loadButton)) {
			loadAction();
		} else if (evt.getSource().equals(saveButton)) {
			saveAction();
		} else if (evt.getSource().equals(saveAsButton)) {
			saveAsAction();
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(REpiceaAWTProperty.WindowAcceptedConfirmed.name())) {
			if (hasChanged) {
				int userReply = JOptionPane.showConfirmDialog((Component) component, 
						REpiceaTranslator.getString(MessageID.ParamsHaveChanged),
						REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning),
						JOptionPane.YES_NO_OPTION);
				if (userReply == 0) {
					if (saveAsAction()) {	// answer is yes but possibility to come back if the file chooser is cancelled
						component.setVisible(false);
					}
				} else if (userReply == 1) {  // answer is no
					component.setVisible(false);
				} else {
					return;	// answer is cancel (we go back then)
				}
			} else {
				component.setVisible(false);	// there was no change so shutdown
			}
		} else if (arg0.getPropertyName().equals(REpiceaAWTProperty.ActionPerformed.name())) {
			hasChanged = true;
		} else if (arg0.getPropertyName().equals(REpiceaAWTProperty.WindowCancelledConfirmed.name())) {
			component.setVisible(false);	// just cancelled so shutdown
		}
	}


	/**
	 * This method sets the title of the window instance according to the filename provided by the caller. It is typically called
	 * by the GUI after loading a file.
	 * @param caller a IOUserInterfaceableObject interface
	 * @param window a REpiceaWindow window
	 */
	public static void RefreshTitle(IOUserInterfaceableObject caller, REpiceaWindow window) {
		if (caller.getFilename() != null && !caller.getFilename().isEmpty() && caller.getFileFilter().accept(new File(caller.getFilename()))) {
			String title = CommonGuiUtility.convertFilenameForLabel(caller.getFilename(), 45);
			window.setTitle(UIControlManager.getTitle(((Window) window).getClass()) + " - " + title);
		} else {
			window.setTitle(UIControlManager.getTitle(((Window) window).getClass()));
		}

	}

	
}
