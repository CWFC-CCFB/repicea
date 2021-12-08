/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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

import java.awt.Image;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JDialog;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * A generic modal dialog that ensures that the method cancelAction is called when the dialog can be closed.
 * If any action must be done when closing the dialog, the method cancelAction should be overriden in 
 * the derived class.
 * @author Mathieu Fortin - January 2011
 */
@SuppressWarnings("serial")
public abstract class REpiceaDialog extends JDialog implements REpiceaWindow, Refreshable {

	static enum MessageID implements TextableEnum {
		ConfirmQuitMessage("Are you sure you want to quit this application?", "\u00CAtes-vous s\u00FBr de vouloir quitter cette application?");
		
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
	
	
	private Image iconImage;
	
	protected boolean cancelOnClose = true;
	protected boolean askUserBeforeExit = false;
	
	/**
	 * Constructor 1 with a Window parent.
	 * @param parent a Window parent (can be null if there is no parent)
	 */
	protected REpiceaDialog(Window parent) {
		super(parent);
		new REpiceaInternalControlHandler(this);
		new REpiceaWindowHandler(this);
		new REpiceaWindowShutdown(this);
		setModal(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		if (parent != null) {
			if (!parent.getIconImages().isEmpty()) {
				iconImage = parent.getIconImages().get(0);
			}
		} 
		setIcon();
	}
	
	
	/**
	 * Constructor 2 with no parent at all.
	 */
	protected REpiceaDialog() {
		this(null);
	}
	
	/**
	 * This method sets the Icon of the frame.
	 */
	protected void setIcon() {
		Image image = getBufferedImage();
		if (image != null) {
			setIconImage(image);
		}
	}

	
	/**
	 * This method overrides the method of the super class JFrame. It first memorizes what should be memorized in the memorize()
	 * method and then adds or remove this instance as window listener.
	 */
	@Override
	public void setVisible(boolean bool) {
		if (bool && !isVisible()) {
			listenTo();
			Point location = UIControlManager.getLocation(this);
			if (location == null) {
				setLocationRelativeTo(getOwner());
			} else {
				setLocation(location);
			}
			super.setVisible(bool);
		} else if (isVisible() && !bool) {
			doNotListenToAnymore();
			UIControlManager.registerLocation(this);
			super.setVisible(bool);
		}
	}
	
	
	
	/**
	 * This method reads the image that serves as icon for the frame. By default, the Rouge Epicea logo is read. Just override this method to 
	 * get another icon.
	 * @return a Image instance
	 */
	protected Image getBufferedImage() {
		if (iconImage == null) {
			iconImage = UIControlManager.REpiceaIcon;
		}
		return iconImage;
	}
	
	@Override
	public void cancelAction() {
		firePropertyChange(REpiceaAWTProperty.WindowCancelRequested, null, this);
	}

	@Override
	public void okAction() {
		firePropertyChange(REpiceaAWTProperty.WindowOkRequested, null, this);
	}

	protected final void helpAction() {
		firePropertyChange(REpiceaAWTProperty.WindowHelpRequested, null, this);
	}

	/**
	 * This method defines the action to be undertaken when the window is closed
	 * by clicking on the "x". If true, the action is interpreted as a cancel. If false,
	 * the action is interpreted as an ok. BY DEFAULT, the value is true. 
	 * @param bool a boolean
	 */
	protected final void setCancelOnClose(boolean bool) {
		cancelOnClose = bool;
	}
	
	
	/**
	 * This method creates the UI, i.e. it puts the components at their proper location and so on.
	 * The components should have be previously instantiated.
	 */
	protected abstract void initUI();

	@Override
	public void refreshInterface() {
		validate();
		repaint();
	}

	@Override
	public final void firePropertyChange(REpiceaAWTProperty property, Object obj1, Object obj2) {
		firePropertyChange(property.name(), obj1, obj2);
	}
	

	@Override
	public final boolean isCancelOnClose() {return cancelOnClose;}
	
	@Override 
	public final boolean askUserBeforeExit() {return askUserBeforeExit;}
}
