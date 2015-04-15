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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The DragGestureMoveComponentHandler makes it possible to move the component in a panel.
 * @author Mathieu Fortin - April 2014
 *
 * @param <C> a Component
 */
public abstract class DragGestureMoveComponentHandler<C extends Component> extends MouseAdapter {

	protected static Cursor MouseDragCursor = new Cursor(Cursor.MOVE_CURSOR);

	protected final C component;
	protected Cursor cursorBeforeDragging;
	protected Point offsetPoint;
	private boolean enabled;
	
	protected DragGestureMoveComponentHandler(C component) {
		this.component = component;
		this.component.addMouseListener(this);
		setEnabled(true);
	}
	

	@Override
	public abstract void mouseDragged(MouseEvent arg0); 

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (enabled) {
			offsetPoint = arg0.getPoint();
			cursorBeforeDragging = component.getCursor();
			component.addMouseMotionListener(this);
			component.setCursor(MouseDragCursor);
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (enabled) {
			component.removeMouseMotionListener(this);
			component.setCursor(cursorBeforeDragging);
		}
	}

	/**
	 * This method enables or disables the handler.
	 * @param enabled a boolean
	 */
	public void setEnabled(boolean enabled) {this.enabled = enabled;}
	
	/**
	 * This method returns true if the handler is enabled or false otherwise. By default, the handler is enabled.
	 * @return a boolean
	 */
	public boolean isEnabled() {return enabled;}

}
