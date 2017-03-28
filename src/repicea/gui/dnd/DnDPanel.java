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
package repicea.gui.dnd;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import repicea.gui.ListManager;
import repicea.gui.REpiceaPanel;
import repicea.gui.REpiceaUIObject;
import repicea.gui.Refreshable;
import repicea.gui.components.REpiceaScrollPane;

/**
 * The DnDPanel class handles the drop of any object of class P.  
 * @author Mathieu Fortin - February 2014
 * @param <D> the class of this object to be received
 */
@SuppressWarnings("serial")
public class DnDPanel<D extends REpiceaUIObject> extends REpiceaScrollPane implements AcceptableDropComponent<D>, Refreshable { 

	public static class InternalPanel extends REpiceaPanel {
		protected InternalPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		@Override
		public void listenTo () {}

		@Override
		public void doNotListenToAnymore () {}

		@Override
		public void refreshInterface() {}
	}

	class InnerThread extends Thread {
		final Runnable runnable;
		
		InnerThread(Runnable runnable) {
			this.runnable = runnable;
		}
		
		@Override
		public void run() {
			try {
				wait(200);	// this pause gives some time for the other events to be processed before lifting the veto
			} catch (Exception e) {}
			SwingUtilities.invokeLater(runnable);
		}
	}
	
	
	protected static class InternalEmbeddingPanel extends JPanel {
		
		protected InternalEmbeddingPanel() {
			setLayout(new BorderLayout());
		}
	}


	private final ListManager<D> manager;
	protected final InternalPanel internalPanel;

	/**
	 * Constructor.
	 * @param manager an object which implements the ListManager interface
	 * @param clazz class of the interfaceable object
	 */
	public DnDPanel(ListManager<D> manager, Class<D> clazz) {
		super();
		this.internalPanel = createInternalPanel();
		setViewportView(internalPanel);
		this.manager = manager;
		new DropTargetImpl<D>(this, clazz);
	}

	protected InternalPanel createInternalPanel() {return new InternalPanel();}
	
	@Override
	public Component add(Component comp) {
		return internalPanel.add(comp);
	}

	@Override
	public void remove(Component comp) {
		internalPanel.remove(comp);
	}
	
	
	@Override
	public void acceptThisObject(D obj, DropTargetDropEvent evt) {
		manager.registerObject(obj);
		getViewport().setViewPositionVetoEnabled(true);
		refreshInterface();
		sendVetoDisabledOnDispatchThread();
	}

	protected final void sendVetoDisabledOnDispatchThread() {
		Runnable doRun = new Runnable() {
			@Override
			public void run() {
				getViewport().setViewPositionVetoEnabled(false);	// disable the bypass when the drop is over
			}
		};
		new InnerThread(doRun).start();
	}

	protected Point getRelativePointFromDropEvent(DropTargetDropEvent arg0) {
		Point dropPoint = arg0.getLocation();
		Point offset = getViewport().getViewPosition();
		return new Point(dropPoint.x + offset.x, dropPoint.y + offset.y);
	}
		
	@Override
	public void refreshInterface() {
		internalPanel.removeAll();
		addManagerComponents();
		validate();
		repaint();
	}

	protected void addManagerComponents() {
		for (REpiceaUIObject obj : manager.getList()) {
			internalPanel.add(obj.getUI());
		}
	}


	/**
	 * This method removes the UserInterfaceableObject parameter from the list and then
	 * refresh the interface.
	 * @param obj a UserInterfaceableObject instance
	 */
	public void removeSubpanel(D obj) {
		manager.removeObject(obj);
		refreshInterface();
	}
	
	
	protected ListManager<D> getListManager() {return manager;}

}
