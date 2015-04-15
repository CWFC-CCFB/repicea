/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.processsystem;

import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.ShowableObjectWithParent;
import repicea.gui.dnd.DragGestureMoveComponentHandler;
import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.gui.permissions.REpiceaGUIPermissionProvider;
import repicea.simulation.processsystem.UISetup.BasicMode;

@SuppressWarnings("serial")
public class ProcessorButton extends SelectableJButton implements AnchorProvider, ShowableObjectWithParent, REpiceaGUIPermissionProvider {

	
	protected static class DragGestureButtonMoveHandler extends DragGestureMoveComponentHandler<ProcessorButton> {
		
		private Point currentCorner;
		private boolean isDragging = false;
		
		protected DragGestureButtonMoveHandler(ProcessorButton component) {
			super(component);
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			isDragging = true;
			Point cursorPoint = arg0.getPoint();
			currentCorner = component.getLocation();
			int xCoord = cursorPoint.x - offsetPoint.x + currentCorner.x;
			int yCoord = cursorPoint.y - offsetPoint.y + currentCorner.y;
			Container container = component.getParent();
			if (container != null) {
				if (xCoord + component.getSize().width > container.getSize().width) {
					xCoord = container.getSize().width - component.getSize().width;
				}
				if (xCoord < 0) {
					xCoord = 0;
				}
				if (yCoord + component.getSize().height > container.getSize().height) {
					yCoord = container.getSize().height - component.getSize().height;
				}
				if (yCoord < 0) {
					yCoord = 0;
				}
			}
			Point newLocation = new Point(xCoord, yCoord);
			component.setLocation(newLocation);	
			component.validate();
		}
		
		
		@Override
		public void mouseReleased(MouseEvent evt) {
			if (isEnabled()) {
				super.mouseReleased(evt);
				SystemManagerDialog dlg = (SystemManagerDialog) CommonGuiUtility.getParentComponent(component, SystemManagerDialog.class);
				if (dlg != null && isDragging) {
					dlg.firePropertyChange(REpiceaAWTProperty.ActionPerformed, offsetPoint, component.getLocation());
					isDragging = false;
				}
			}
		}
	}

	
	private final Processor process;
	protected final DragGestureMoveComponentHandler<ProcessorButton> buttonMoveRecognizer;
	protected final DragGestureRecognizer createLinkRecognizer;
	protected final JLabel label;
	private final SystemPanel panel;
	private transient ProcessorInternalDialog guiInterface;
	
	protected ProcessorButton(SystemPanel panel, Processor process, REpiceaGUIPermission permission) {
		super(permission);
		this.panel = panel;
		this.process = process;
		this.label = new JLabel();
		addMouseListener(new SystemComponentMouseAdapter(this));
		DragSource ds = new DragSource();
		buttonMoveRecognizer = new DragGestureButtonMoveHandler(this);
		buttonMoveRecognizer.setEnabled(false);
		createLinkRecognizer = ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureCreateLinkListener(this));
		createLinkRecognizer.setComponent(null);
		setLabel();
	}

	protected ProcessorButton(SystemPanel panel, Processor process) {
		this(panel, process, panel.getListManager().getGUIPermission());
	}
	
	protected void setLabel() {
		String processName = process.getName();
		if (processName.length() > 30) {
			processName = processName.substring(0, 30) + "...";
		}
		label.setText(processName);
	}
	

	@Override
	protected void finalize() {
		panel.remove(this);
		panel.remove(label);
	}
	

	/**
	 * This method returns the Processor instance that owns this button.
	 * @return a Processor instance
	 */
	public Processor getOwner() {return process;}
		
	public Point getRightAnchor() {
		int x = getLocation().x + getSize().width;
		int y = getLocation().y + getSize().height / 2;
		return new Point(x,y);
	}

	public Point getLeftAnchor() {
		int x = getLocation().x;
		int y = getLocation().y + getSize().height / 2;
		return new Point(x,y);
	}

	protected Point retrieveFormerLocation() {
		return process.getOriginalLocation();
	}
	
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		getOwner().setOriginalLocation(new Point(x,y));
		label.setLocation(x, y + (int) getSize().getHeight());
	}

	@Override
	public void setLocation(Point point) {
		super.setLocation(point);
		getOwner().setOriginalLocation(point);
		label.setLocation(point.x, point.y + (int) getSize().getHeight());
	}

	
	@SuppressWarnings("rawtypes")
	protected void setDragMode(Enum mode) {
		if (getGUIPermission().isDragAndDropGranted()) {
			buttonMoveRecognizer.setEnabled(false);
			createLinkRecognizer.setComponent(null);
			if (mode == BasicMode.MoveProcessor) {
				buttonMoveRecognizer.setEnabled(true);
			} else if (mode == BasicMode.CreateLink) {
				if (!getOwner().isTerminalProcessor()) {
					createLinkRecognizer.setComponent(this);
				}
			}
		}
	}

	@Override
	public ProcessorInternalDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new ProcessorInternalDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public void showInterface(Window window) {
		ProcessorInternalDialog dlg = getGuiInterface(window);
		dlg.setVisible(true);
	}

	@Override
	public String toString() {return getOwner().getName();}


	@Override
	protected Icon getDefaultIcon() {return UISetup.Icons.get(ProcessorButton.class.getName());}

}
