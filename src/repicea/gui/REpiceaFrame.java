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
import java.awt.event.ActionListener;

import javax.swing.JFrame;

/**
 * This class is the main class for any JFrame. It implements a window listener that calls a cancelAction 
 * method when the window is closed. The run method serves for launching this window from the Event Dispatch
 * Thread using the SwingUtilities.invokeLater method.
 * @author Mathieu Fortin - November 2011
 */
@SuppressWarnings("serial")
public abstract class REpiceaFrame extends JFrame implements REpiceaWindow, ActionListener {
		
	private Image iconImage;
	private Window owner;
	private Point location;
	protected boolean cancelOnClose = true;
	protected boolean askUserBeforeExit = false;

	/**
	 * Protected constructor with owner.
	 * @param parent the parent window if any
	 */
	protected REpiceaFrame(Window parent) {
		super();
		this.owner = parent;
		new REpiceaInternalControlHandler(this);
		new REpiceaWindowHandler(this);
		new REpiceaWindowShutdown(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//		setLookAndFeel();
		setIcon();
	}

	/**
	 * Protected constructor without owner.
	 */
	protected REpiceaFrame() {
		this(null);
	}
	
		
//	/**
//	 * This method sets the look and feel to the system look and feel.
//	 */
//	protected void setLookAndFeel() {
//		try {
//			String lookAndFeelName = UIManager.getSystemLookAndFeelClassName();
//			UIManager.setLookAndFeel(lookAndFeelName);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			e.printStackTrace();
//		}
//		
//	}

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
	
	/**
	 * This method defines the action to be undertaken when the window is closed
	 * by clicking on the "x". If true, the action is interpreted as a cancel. If false,
	 * the action is interpreted as an ok. BY DEFAULT, the value is true. 
	 * @param bool a boolean
	 */
	protected final void setCancelOnClose(boolean bool) {
		cancelOnClose = bool;
	}
	
	@Override
	public void cancelAction() {
		firePropertyChange(REpiceaAWTProperty.WindowCancelRequested, null, this);
	}

	@Override
	public void okAction() {
		firePropertyChange(REpiceaAWTProperty.WindowOkRequested, null, this);
	}

	protected void helpAction() {
		firePropertyChange(REpiceaAWTProperty.WindowHelpRequested, null, this);
	}

	/**
	 * This method overrides the method of the super class JFrame. It just adds or remove
	 * this instance as window listener.
	 */
	@Override
	public void setVisible(boolean bool) {
		if (bool && !isVisible()) {
			listenTo();
			if (location != null) {
				setLocation(location);
			} else {
				setLocationRelativeTo(getOwner());
			}
			REpiceaAWTEvent.fireEvent(new REpiceaAWTEvent(this, REpiceaAWTProperty.WindowsAboutToBeVisible));
			super.setVisible(bool);
		} else if (isVisible() && !bool) {
			doNotListenToAnymore();
			super.setVisible(bool);
			REpiceaAWTEvent.fireEvent(new REpiceaAWTEvent(this, REpiceaAWTProperty.WindowsJustSetToInvisible));
		}
	}
	
	@Override
	public Window getOwner() {return owner;}
	
	@Override
	public final void firePropertyChange(REpiceaAWTProperty property, Object obj1, Object obj2) {
		firePropertyChange(property.name(), obj1, obj2);
	}

	protected void anchorLocation(Point location) {
		this.location = location;
	}
	
	@Override
	public final boolean isCancelOnClose() {return cancelOnClose;}
	
	@Override 
	public final boolean askUserBeforeExit() {return askUserBeforeExit;}


}

