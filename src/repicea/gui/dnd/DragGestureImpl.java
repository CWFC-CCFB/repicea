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
package repicea.gui.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

import javax.swing.JList;

/**
 * The DragGestureImpl class handles the first step of the drag and drop procedure by creating a TransferableObject
 * instance of class P.
 * @author Mathieu Fortin - October 2012
 * @param <P> the class of the object to be transfered
 */
public class DragGestureImpl<P> implements DragGestureListener {

	protected static Component DragFromThisComponent;
	protected static Transferable LocalTransferable;
	protected DragSourceListener dsl;
	protected Image icon;

	
	public DragGestureImpl() {
		super();
	}
	
	public DragGestureImpl(DragSourceListener dsl, Image icon) {
		super();
		this.dsl = dsl;
		this.icon = icon;
	}
	
	
	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Cursor cursor = null;
		DragFromThisComponent = event.getComponent();
		if (event.getDragAction() == DnDConstants.ACTION_COPY) {
			cursor = DragSource.DefaultCopyDrop;
		}
		
		P obj = adaptSourceToTransferable(event);
		LocalTransferable = new TransferableObject<P>(obj);
		if (dsl != null && icon != null) {
			event.startDrag(cursor, icon, new Point(0,0), LocalTransferable, dsl);
		} else {
			try {
				event.startDrag(cursor, LocalTransferable);
			} catch(InvalidDnDOperationException e) {}				
		}
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	protected P adaptSourceToTransferable(DragGestureEvent event) {
		P obj;
		if (event.getComponent() instanceof JList) {
			obj = (P) ((JList) event.getComponent()).getSelectedValue();
		} else {
			obj = (P) event.getComponent();
		}
		return obj;
	}
	
}

