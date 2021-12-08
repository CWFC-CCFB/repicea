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

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;

/**
 * The REpiceaCellEditor is the CellEditor class for REpiceaTable.
 * @author Mathieu Fortin - January 2015
 */
@SuppressWarnings("serial")
public class REpiceaCellEditor extends DefaultCellEditor {

	private int row;
	private int column;
	private final REpiceaTableModel tableModel;

	public REpiceaCellEditor(JTextField component, REpiceaTableModel tableModel) {
		super(component);
		this.tableModel = tableModel;
		addCellEditorListener(tableModel);
	}

	public REpiceaCellEditor(JCheckBox component, REpiceaTableModel tableModel) {
		super(component);
		this.tableModel = tableModel;
		addCellEditorListener(tableModel);
	}

	public REpiceaCellEditor(JComboBox<?> component, REpiceaTableModel tableModel) {
		super(component);
		this.tableModel = tableModel;
		addCellEditorListener(tableModel);
	}

	protected void setValue() {
		Component component = getComponent();
		if (component != null && component.getClass().isAssignableFrom(JFormattedNumericField.class)) {
			Number value = ((JFormattedNumericField) component).getValue();
			tableModel.setValueAt(value, row, column);
		}

	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.row = row;
		this.column = column;
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

}

