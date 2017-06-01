/*
 * This file is part of the repicea-util library.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

/**
 * The REpiceaTableModel is the model for the REpiceaTable class.
 * @author Mathieu Fortin - January 2015
 */
@SuppressWarnings("serial")
public class REpiceaTableModel extends DefaultTableModel implements CellEditorListener {

	private final List<Class<?>> columnClass;
	private final Map<Integer, Boolean> vetos;

	public REpiceaTableModel(Object[] columnNames) {
		super(columnNames, 0);
		columnClass = new ArrayList<Class<?>>();
		vetos = new HashMap<Integer, Boolean>();
	}

	@Override
	public void addRow(Object[] record) {
		if (getRowCount() == 0) {
			recordColumnType(record);
		}
		super.addRow(record);
	}

	private void recordColumnType(Object[] record) {
		for (Object obj : record) {
			columnClass.add(obj.getClass());
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < columnClass.size()) {
			return columnClass.get(columnIndex);
		} else {
			return null;
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {}

	@Override
	public void editingStopped(ChangeEvent e) {
		if (e.getSource() instanceof REpiceaCellEditor) {
			REpiceaCellEditor cellEditor = (REpiceaCellEditor) e.getSource();
			cellEditor.setValue();
		}
	}

	protected void addDefaultRecord() {
		Object[] record = new Object[getColumnCount()];
		for (int i = 0; i < getColumnCount(); i++) {
			Class<?> clazz = columnClass.get(i);
			if (clazz.equals(Integer.class)) {
				record[i] = new Integer(0);
			} else if (clazz.equals(Double.class)) {
				record[i] = 0.;
			} else if (clazz.isEnum()) {
				record[i] = clazz.getEnumConstants()[0];
			} else if (clazz.equals(String.class)) {
				record[i] = "";
			} else if (clazz.equals(Boolean.class)) {
				record[i] = true;
			}
		}
		addRow(record);
	}
	
	/**
	 * By default, all the cells are editable. This method can be used to disable the editing is a particular column.
	 * @param columnIndex the index of the column
	 * @param veto true to disable the editing
	 */
	public void setEditableVetos(int columnIndex, boolean veto) {
		vetos.put(columnIndex, veto);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (vetos.containsKey(column)) {
			return !vetos.get(column);
		}
		return true;
	}
	


}

