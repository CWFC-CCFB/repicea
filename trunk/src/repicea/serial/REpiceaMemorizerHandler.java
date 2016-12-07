/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.serial;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;

import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.Resettable;
import repicea.gui.SynchronizedListening;
import repicea.serial.xml.XmlList;
import repicea.serial.xml.XmlUnmarshaller;

public class REpiceaMemorizerHandler implements ActionListener, SynchronizedListening, Resettable {
	
	private class InternalComponentAdapter implements ComponentListener {

		@Override
		public void componentMoved(ComponentEvent arg0) {}

		@Override
		public void componentResized(ComponentEvent arg0) {	}

		@Override
		public void componentShown(ComponentEvent arg0) {
			if (arg0.getSource().equals(window)) {
				REpiceaMemorizerHandler.this.setMemorizerWorkerEnabled(true);
				reset();
				listenTo();
			}
		}

		@Override
		public void componentHidden(ComponentEvent arg0) {
			if (arg0.getSource().equals(window)) {
				doNotListenToAnymore();
				REpiceaMemorizerHandler.this.setMemorizerWorkerEnabled(false);
			}
		}
	}

	private class InternalPropertyChangeAdapter implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			String propertyName = arg0.getPropertyName();
			if (propertyName.equals(REpiceaAWTProperty.ActionPerformed.name()) || propertyName.equals(REpiceaAWTProperty.JustLoaded.name())) {
				if (extendedImplementation) {
					Container container = (Container) arg0.getSource();
					if (container.isVisible()) {			// memorizing only if the container is visible
						OwnedWindow window = (OwnedWindow) container;
						memorizeFromWindow(window);
					}
				}				
			} else if (propertyName.equals(REpiceaAWTProperty.JustSaved.name())) {
				reset();
			} else if (propertyName.equals(REpiceaAWTProperty.WindowCancelledConfirmed.name())) {
				REpiceaMemorizerHandler.this.windowOwner.unpackMemorizerPackage(convertToMemorizerPackage(referencePackage));
				synchronize();
			} else  if (propertyName.equals(REpiceaAWTProperty.SynchronizeWithOwner.name())) {
				synchronize();
			}

		}
	}
	
	private MemorizerPackage convertToMemorizerPackage(XmlList list) {
		XmlUnmarshaller unmarshaller = new XmlUnmarshaller();
		MemorizerPackage mp;
		try {
			mp = (MemorizerPackage) unmarshaller.unmarshall(list);
			return mp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
	protected final InternalComponentAdapter componentAdapter;

	private final InternalPropertyChangeAdapter propertyChangeAdapter;

	private final Memorizable windowOwner;

	private final OwnedWindow window;

	private MemorizerWorker worker;
	
	private boolean isFirstMemorizing;
	private boolean extendedImplementation;

	protected XmlList referencePackage;

	private final MemorizedArray memorized; 
	
	private AbstractButton undo;
	private AbstractButton redo;
	
	private int maxCapacity = 10;
	private int currentPointer = -1;
	
	/**
	 * Constructor for full implementation.
	 * @param window a OwnedWindow instance
	 * @param undo the undo button in the OwnerWindow instance
	 * @param redo the redo button in the OwnerWindow instance
	 */
	public REpiceaMemorizerHandler(OwnedWindow window, AbstractButton undo, AbstractButton redo) {
		this.windowOwner = window.getWindowOwner();
		this.window = window;
		this.memorized = new MemorizedArray();
		
		propertyChangeAdapter = new InternalPropertyChangeAdapter();
		componentAdapter = new InternalComponentAdapter();
		((Component) window).addComponentListener(componentAdapter);

		this.undo = undo;
		this.redo = redo;
		if (undo != null) {
			this.undo.addActionListener(this);
			extendedImplementation = true;
		}
		if (redo != null) {
			this.redo.addActionListener(this);
			extendedImplementation = true;
		}
	}
	
	/**
	 * Constructor for light implementation.
	 * @param window a OwnedWindow instance
	 */
	public REpiceaMemorizerHandler(OwnedWindow window) {
		this(window, null, null);
	}

	private void addMemorizerPackage(XmlList retrieve) {
		if (currentPointer + 1 == maxCapacity) {
			memorized.remove(0);
			currentPointer--;
		} 
		memorized.add(++currentPointer, retrieve);
		while (currentPointer < memorized.size() - 1) {
			memorized.remove(currentPointer + 1);
		}
		if (redo != null) {
			redo.setEnabled(false);
		}
		if (currentPointer > 0) {
			if (undo != null) {
				undo.setEnabled(true);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(undo)) {
			currentPointer--;
			MemorizerPackage serializedMemorizerPackage = convertToMemorizerPackage(memorized.get(currentPointer));
			windowOwner.unpackMemorizerPackage(serializedMemorizerPackage);
			redo.setEnabled(true);
			undo.setEnabled(true);
			if (currentPointer == 0) {
				undo.setEnabled(false);
			} 
			synchronize();
		} else if (arg0.getSource().equals(redo)) {
			MemorizerPackage serializedMemorizerPackage = convertToMemorizerPackage(memorized.get(++currentPointer));
			windowOwner.unpackMemorizerPackage(serializedMemorizerPackage);
			undo.setEnabled(true);
			redo.setEnabled(true);
			if (currentPointer == memorized.size() - 1) {
				redo.setEnabled(false);
			} 
			synchronize();
		}
	}

	@Override
	public void listenTo() {
		((Window) window).addPropertyChangeListener(propertyChangeAdapter);
	}

	@Override
	public void doNotListenToAnymore() {
		((Window) window).removePropertyChangeListener(propertyChangeAdapter);
	}
	
	protected void registerMemorizerPackage(XmlList mp) {
		if (isFirstMemorizing) {
			referencePackage = mp;
			isFirstMemorizing = false;
		} 
		if (extendedImplementation) {
			addMemorizerPackage(mp);
		}
	}
	
	protected void memorizeFromWindow(OwnedWindow window) {
		worker.addToQueue(window.getWindowOwner().getMemorizerPackage());
	}

	protected void synchronize() {
		doNotListenToAnymore();
		window.synchronizeUIWithOwner();
		listenTo();
	}
	
	@Override
	public void reset() {
		if (undo != null) {
			undo.setEnabled(false);
		}
		if (redo != null) {
			redo.setEnabled(false);
		}
		memorized.clear();
		currentPointer = -1;
		isFirstMemorizing = true;		// ensure the package will be saved in the referencePackage member
		memorizeFromWindow(window);
	}

	protected void setMemorizerWorkerEnabled(boolean enabled) {
		if (worker == null || !worker.isAlive()) {
			if (enabled) {
				worker = new MemorizerWorker("Memorizer Thread", REpiceaMemorizerHandler.this);
				worker.start();
			}
		} else if (worker != null) {
			if (!enabled) {
				worker.addToQueue(MemorizerWorker.ShutDownMemorizerPackage);
			}
		}
	}
	
}
