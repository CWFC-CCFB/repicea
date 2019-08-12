/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.popup.REpiceaPopupListener;
import repicea.gui.popup.REpiceaPopupMenu;

/**
 * The REpiceaTable is a JTable with additional methods. Adding and deleting row is possible through a popup menu.
 * @author Mathieu Fortin - January 2015
 */
@SuppressWarnings("serial")
public class REpiceaTable extends JTable implements ActionListener {

	private final REpiceaPopupMenu popupMenu;
	private final JMenuItem addItem;
	private final JMenuItem deleteItem;

	/**
	 * General constructor.
	 * @param model an REpiceaTableModel instance
	 * @param popupEnabled true to enable the popup menu that allows to add or delete a record
	 */
	public REpiceaTable(REpiceaTableModel model, boolean popupEnabled) {
		super(model);
		addItem = UIControlManager.createCommonMenuItem(CommonControlID.Addone);
		deleteItem = UIControlManager.createCommonMenuItem(CommonControlID.Delete);
		popupMenu = new REpiceaPopupMenu(this, addItem, deleteItem);
		if (popupEnabled) {
			addMouseListener(new REpiceaPopupListener(popupMenu));
			getTableHeader().addMouseListener(new REpiceaPopupListener(popupMenu));
		}
	}

	/**
	 * Default constructor with popup menu enabled. 
	 * @param model an REpiceaTableModel instance
	 */
	public REpiceaTable(REpiceaTableModel model) {
		this(model, true);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(addItem)) {
			((REpiceaTableModel) getModel()).addDefaultRecord();
		} else if (arg0.getSource().equals(deleteItem)) {
			List<Integer> rowIndices = new ArrayList<Integer>();
			for (int index : getSelectedRows()) {
				rowIndices.add(index);
			}
			Collections.sort(rowIndices);
			while (!rowIndices.isEmpty()) {
				((REpiceaTableModel) getModel()).removeRow(rowIndices.remove(rowIndices.size()-1));
			}
		}
	}
}

