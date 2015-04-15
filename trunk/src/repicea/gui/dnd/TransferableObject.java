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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The TransferableObject class is a wrapper that takes an object from class P during the drag and drop
 * procedure.
 * @author Mathieu Fortin - October 2012
 * @param <P> The class of the object to be transfered
 */
class TransferableObject<P> implements Transferable {

	@SuppressWarnings("rawtypes")
	private static Map<Class, DataFlavor> dataFlavors = new HashMap<Class, DataFlavor>();
	
	private P obj;
	
	/**
	 * Constructor.
	 * @param obj the object to be transfered
	 */
	public TransferableObject(P obj) {
		this.obj = obj;
	}
	
	/**
	 * This method creates a DataFlavor instance for this class if it does not exist yet.
	 * @param clazz a Class instance
	 */
	@SuppressWarnings("rawtypes")
	protected static void registerDataFlavorForThisClass(Class clazz) {
		if (!dataFlavors.containsKey(clazz)) {
			DataFlavor dataFlavor = new DataFlavor(clazz, clazz.getSimpleName());
			dataFlavors.put(clazz, dataFlavor);
		}
	}
	
	/**
	 * This method retrieve the DataFlavor that matches this class.
	 * @param clazz a Class instance
	 * @return a DataFlavor instance
	 */
	@SuppressWarnings("rawtypes")
	public static DataFlavor getDataFlavorForThisClass(Class clazz) {
		DataFlavor dataFlavor = null;
		do {
			dataFlavor = dataFlavors.get(clazz);
			clazz = clazz.getSuperclass();
		} while (dataFlavor == null && clazz != null);
		return dataFlavor;
	}
	
	@Override
	public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(arg0)) {
			return obj;
		} else {
			throw new UnsupportedFlavorException(arg0);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {getDataFlavorForThisClass(obj.getClass())};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return arg0.equals(getDataFlavorForThisClass(obj.getClass()));
	}
	
}

