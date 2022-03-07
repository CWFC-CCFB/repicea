/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import repicea.gui.REpiceaDialog.MessageID;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.util.REpiceaTranslator;

/**
 * The REpiceaBasicWindowControlHandler manages the basic controls that are common to the REpiceaDialog and REpiceaFrame classes.
 * @author Mathieu Fortin - January 2015
 */
class REpiceaInternalControlHandler implements PropertyChangeListener {
	
	private final REpiceaWindow owner;
	
	REpiceaInternalControlHandler(REpiceaWindow owner) {
		this.owner = owner;
		((Window) owner).addPropertyChangeListener(this);
	}
	
	protected void cancelRequestedAction() {
		if (((REpiceaWindow) owner).askUserBeforeExit()) {
			
//			int reply = JOptionPane.showConfirmDialog((Window) owner, 
//					REpiceaTranslator.getString(MessageID.ConfirmQuitMessage),
//					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning), 
//					JOptionPane.YES_NO_OPTION);
			JOptionPane optionPane = new JOptionPane(REpiceaTranslator.getString(MessageID.ConfirmQuitMessage),
					JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION);
			JDialog dlg = optionPane.createDialog((Window) owner,	REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning));
			CommonGuiUtility.findAndAdaptButtonOfThisKind(optionPane, CommonControlID.Yes);
			CommonGuiUtility.findAndAdaptButtonOfThisKind(optionPane, CommonControlID.No);

			dlg.setVisible(true);
			Object reply = optionPane.getValue();
			if (reply != null && reply instanceof Integer) {
				if ((int) reply == 0) {
					owner.firePropertyChange(REpiceaAWTProperty.WindowCancelledConfirmed, null, this);
				}
			}
		} else {
			owner.firePropertyChange(REpiceaAWTProperty.WindowCancelledConfirmed, null, this);
		}
	}

	protected void okRequestedAction() {
		if (owner.askUserBeforeExit()) {
			int reply = JOptionPane.showConfirmDialog((Window) owner, 
					REpiceaTranslator.getString(MessageID.ConfirmQuitMessage),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning), 
					JOptionPane.YES_NO_OPTION);
			if (reply == 0) {
				owner.firePropertyChange(REpiceaAWTProperty.WindowAcceptedConfirmed, null, this);
			}
		} else {
			owner.firePropertyChange(REpiceaAWTProperty.WindowAcceptedConfirmed, null, this);
		}
	}
	
	protected void helpRequestedAction() {
		AutomatedHelper helper = UIControlManager.getHelper(owner.getClass());
		if (helper != null) {
			helper.callHelp();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(owner)) {
			if (evt.getPropertyName().equals(REpiceaAWTProperty.WindowOkRequested.name())) {
				okRequestedAction();
			} else if (evt.getPropertyName().equals(REpiceaAWTProperty.WindowCancelRequested.name())) {
				cancelRequestedAction();
			} else if (evt.getPropertyName().equals(REpiceaAWTProperty.WindowHelpRequested.name())) {
				helpRequestedAction();
			}
		}
	}
	
}
