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
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.util.List;

/**
 * The DropTargetImpl class handles the drop during a drag and drop process.
 * @author Mathieu Fortin - October 2012
 * @param <P> the class of the object to be dropped
 */
public class DropTargetImpl<P> extends DropTargetAdapter implements DropTargetListener {

	private AcceptableDropComponent<P> comp;
	private DataFlavor dataFlavor;
	private List<Component> acceptedSources;
	private int dragType;
	
	/**
	 * Constructor 1.
	 * @param comp a Container instance that implements AcceptableDropContainer
	 * @param classToBeAccepted
	 * @param dragType an integer (see DnDConstants)
	 * @param acceptedSources a List of Component from which the drag may come (can be null)
	 */
	@SuppressWarnings("rawtypes")
	public DropTargetImpl(AcceptableDropComponent<P> comp, Class classToBeAccepted, int dragType, List<Component> acceptedSources) {
		this.comp = comp;
		this.dragType = dragType; 
		this.acceptedSources = acceptedSources;
		TransferableObject.registerDataFlavorForThisClass(classToBeAccepted);
		this.dataFlavor = TransferableObject.getDataFlavorForThisClass(classToBeAccepted);
		new DropTarget((Container) comp, dragType, this, true);
	}

	
	/**
	 * Constructor 2.
	 * @param comp a Container instance that implements AcceptableDropContainer
	 * @param classToBeAccepted
	 * @param acceptedSources a List of Component from which the drag may come (can be null)
	 */
	@SuppressWarnings("rawtypes")
	public DropTargetImpl(AcceptableDropComponent<P> comp, Class classToBeAccepted, List<Component> acceptedSources) {
		this(comp, classToBeAccepted, DnDConstants.ACTION_COPY, acceptedSources);
	}

	
	/**
	 * Constructor 3.
	 * @param comp a Container instance that implements AcceptableDropContainer
	 * @param classToBeAccepted
	 */
	@SuppressWarnings("rawtypes")
	public DropTargetImpl(AcceptableDropComponent<P> comp, Class classToBeAccepted) {
		this(comp, classToBeAccepted, DnDConstants.ACTION_COPY, null);
	}

	/**
	 * Constructor 4.
	 * @param comp a Container instance that implements AcceptableDropContainer
	 * @param classToBeAccepted
	 * @param dragType an integer (see DnDConstants)
	 */
	@SuppressWarnings("rawtypes")
	public DropTargetImpl(AcceptableDropComponent<P> comp, Class classToBeAccepted, int dragType) {
		this(comp, classToBeAccepted, dragType, null);
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent arg0) {
		if (acceptedSources != null && !acceptedSources.contains(DragGestureImpl.DragFromThisComponent)) {
			arg0.rejectDrop();
			return;
		}
		try {
			arg0.acceptDrop(dragType);
			Transferable tr = arg0.getTransferable();
			P obj = (P) tr.getTransferData(dataFlavor);
			comp.acceptThisObject(obj, arg0);
			((Container) comp).validate();
			return;
		} catch (Exception e) {
			arg0.rejectDrop();
		}
	}
}


