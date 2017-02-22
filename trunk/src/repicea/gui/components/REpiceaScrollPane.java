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
package repicea.gui.components;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import repicea.gui.dnd.DragGestureMoveComponentHandler;

/**
 * The REpiceaScrollPane is a JScrollPane that enables the mouse dragging in any direction.
 * @author Mathieu Fortin - April 2014
 */
@SuppressWarnings("serial")
public class REpiceaScrollPane extends JScrollPane {
	
	protected static class InternalDragGestureMoveComponentHandler extends DragGestureMoveComponentHandler<REpiceaScrollPane> {

		private int xValue;
		private int yValue;

		protected InternalDragGestureMoveComponentHandler(REpiceaScrollPane component) {
			super(component);
		}
		
		@Override
		public void mousePressed(MouseEvent arg0) {
			super.mousePressed(arg0);
			xValue = component.getHorizontalScrollBar().getValue();
			yValue = component.getVerticalScrollBar().getValue();		
		}
		
		@Override
		public void mouseDragged(MouseEvent arg0) {
			JScrollBar hScrollBar = component.getHorizontalScrollBar();
			JScrollBar vScrollBar = component.getVerticalScrollBar();
			if (hScrollBar.isVisible() || vScrollBar.isVisible()) {
				if (hScrollBar.isVisible()) {
					Point currentPoint = arg0.getPoint();
					int xMove = currentPoint.x - offsetPoint.x;
					hScrollBar.setValue(xValue - xMove);
				}
				if (vScrollBar.isVisible()) {
					Point currentPoint = arg0.getPoint();
					int yMove = currentPoint.y - offsetPoint.y;
					vScrollBar.setValue(yValue - yMove);
				}
			}
		}
		
	}
	

	public static class REpiceaViewport extends JViewport {
	
		public REpiceaViewport() {
			super();
		}
		
		
		protected boolean isDropping;		
	
		@Override
		public void setViewPosition(Point point) {
			if (!isDropping) {
				super.setViewPosition(point);
			}
		}
		
		/**
		 * This method is used to disable the reset of the upper left corner during a drop. When set to true, the 
		 * setViewPosition method is ignored.
		 */
		public void setDropping(boolean isDropping) {
			this.isDropping = isDropping;
		}

	}

	/**
	 * Constructor.
	 * @param container the container to be put in the scroll panel.
	 */
	public REpiceaScrollPane(Container container) {
		this();
		setViewportView(container);
	}
	
	public REpiceaScrollPane() {
		super();
		new InternalDragGestureMoveComponentHandler(this);
	}
	
	

	@Override
	protected JViewport createViewport() {
		return new REpiceaViewport();
	}
	
	@Override
	public REpiceaViewport getViewport() {
		return (REpiceaViewport) super.getViewport();
	}

}

