/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaControlPanel;
import repicea.gui.REpiceaDialog;
import repicea.gui.REpiceaMemorizerHandler;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.lang.REpiceaSystem;
import repicea.serial.Memorizable;

/**
 * The REpiceaEnhancedMatchSelectorDialog class is 
 * similar to the user interface of the 
 * REpiceaEnhancedMatchSelector class.
 * @author Mathieu Fortin - December 2024
 */
@SuppressWarnings("serial")
public class REpiceaMatchWithEnumSelectorDialog extends REpiceaDialog implements IOUserInterface, 
																					TableModelListener, 
																					OwnedWindow {

	final static class REpiceaMatchMapTableModel extends REpiceaTableModel {
		final Enum<?> enumForThisTableModel;
		
		REpiceaMatchMapTableModel(Object[] columnNames, Enum<?> thisEnum) {
			super(columnNames);
			enumForThisTableModel = thisEnum;
		}
	}

	private final REpiceaMatchWithEnumSelector<?, ?> caller;
	private Map<Enum<?>, REpiceaTable> tables;
	private Map<Enum<?>, REpiceaMatchMapTableModel> tableModels;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final WindowSettings windowSettings;
	private boolean isCancelled;
	protected final REpiceaControlPanel controlPanel;
	JTabbedPane tabbedPane;
	
	protected REpiceaMatchWithEnumSelectorDialog(REpiceaMatchWithEnumSelector<?, ?> caller, Window parent, Object[] columnNames) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.caller = caller;
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);

		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		new REpiceaMemorizerHandler(this);
		tables = new HashMap<Enum<?>, REpiceaTable>();
		tableModels = new HashMap<Enum<?>, REpiceaMatchMapTableModel>();
		
		for (Enum<?> thisEnum : caller.potentialMatchesMap.keySet()) {
			REpiceaMatchMapTableModel tableModel = new REpiceaMatchMapTableModel(columnNames, thisEnum);
			tableModel.setEditableVetos(0, true);
			REpiceaTable table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
			table.putClientProperty("terminateEditOnFocusLost", true);
			Object[] possibleTreatments =  caller.potentialMatchesMap.get(thisEnum).toArray();
			table.setDefaultEditor(Object.class, new REpiceaCellEditor(new JComboBox<Object>(possibleTreatments), tableModel));
			table.setDefaultEditor(Integer.class, new REpiceaCellEditor(NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Integer, NumberFormatFieldFactory.Range.All, false), tableModel));
			table.setDefaultEditor(Double.class, new REpiceaCellEditor(NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.All, false), tableModel));
			table.setRowSelectionAllowed(false);
			tables.put(thisEnum, table);
			tableModels.put(thisEnum, tableModel);
		}

		controlPanel = new REpiceaControlPanel(this);
		
		init();
		
		refreshInterface();
		initUI();
		pack();

	}
	
	protected void init() {}
	
	protected REpiceaMatchWithEnumSelector<?,?> getCaller() {return caller;}
	
	@Override
	public void cancelAction() {
		super.cancelAction();
		this.isCancelled = true;
	}

	/**
	 * This method returns true if the window has been cancelled.
	 * @return a boolean
	 */
	public boolean hasBeenCancelled() {return isCancelled;}
	
	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			isCancelled = false;
		}
		super.setVisible(bool);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void refreshInterface() {
		for (Enum<?> thisEnum : caller.matchMaps.keySet()) {
			REpiceaMatchMapTableModel tableModel = tableModels.get(thisEnum);
			Map<?, REpiceaMatchWithEnumObject<?,?>> matchesForThisEnum = (Map) caller.matchMaps.get(thisEnum);
			tableModel.removeAll();
			List<Object> l = new ArrayList<Object>();
			for (Object s : matchesForThisEnum.keySet()) {
				l.clear();
				REpiceaMatchWithEnumObject<?,?> currentMatch = matchesForThisEnum.get(s);
				l.add(currentMatch.getKey());
				l.add(currentMatch.getValue());
				l.addAll(currentMatch.getAdditionalFields());
				tableModel.addRow(l.toArray());
			}
		}
		super.refreshInterface();
	}

	@Override
	public void listenTo() {
		for (REpiceaMatchMapTableModel tableModel : tableModels.values()) {
			tableModel.addTableModelListener(this);
		}
	}

	@Override
	public void doNotListenToAnymore() {
		for (REpiceaMatchMapTableModel tableModel : tableModels.values()) {
			tableModel.removeTableModelListener(this);
		}
	}

	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		setJMenuBar(new JMenuBar());
		JMenu fileMenu = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		getJMenuBar().add(fileMenu);
		fileMenu.add(load);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getMainPanel(), BorderLayout.CENTER);		
		
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
	}

	protected REpiceaTable getTable(Enum<?> thisEnum) {return tables.get(thisEnum);}
	
	protected JPanel getMainPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		pane.add(Box.createVerticalStrut(10));
		tabbedPane = new JTabbedPane();
		for (Enum<?> thisEnum : caller.matchMaps.keySet()) {
			tabbedPane.add(thisEnum.toString(), getPanelToBeEmbeddedInTab(thisEnum));
		}
		pane.add(createSimplePanel(tabbedPane, 10));
		pane.add(Box.createVerticalStrut(10));
		return pane;
	}
	
	protected JComponent getPanelToBeEmbeddedInTab(Enum<?> thisEnum) {
		return new JScrollPane(getTable(thisEnum));
	}
	
	protected JPanel createSimplePanel(Component comp, int margin) {
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(Box.createHorizontalStrut(margin), BorderLayout.WEST);
		pane.add(comp, BorderLayout.CENTER);
		pane.add(Box.createHorizontalStrut(margin), BorderLayout.EAST);
		return(pane);
	}
	
	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public void postLoadingAction() {
		synchronizeUIWithOwner();
	}

	protected String getTitleForThisDialog() {
		String titleOfThisClass = UIControlManager.getTitle(getClass());
		return titleOfThisClass;
	}
 
	/**
	 * The method sets the title of the dialog.
	 */
	protected void refreshTitle() {
		String filename = caller.getFilename();
		if (filename == null || filename.isEmpty()) {
			setTitle(getTitleForThisDialog());
		} else {
			if (filename.length() > 40) {
				filename = "..." + filename.substring(filename.length()-41, filename.length());
			}
			setTitle(getTitleForThisDialog() + " - " + filename);
		}
	}

	@Override
	public WindowSettings getWindowSettings() {return windowSettings;}

	@Override
	public void synchronizeUIWithOwner() {
		doNotListenToAnymore();
		for (REpiceaTable table : this.tables.values()) {
			if (table.isEditing()) {
				table.getCellEditor().stopCellEditing();
			}
		}
		refreshInterface();
		refreshTitle();
		listenTo();
	}

	@Override
	public Memorizable getWindowOwner() {
		return caller;
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			if (e.getSource() instanceof REpiceaMatchMapTableModel) {
				REpiceaMatchMapTableModel model = (REpiceaMatchMapTableModel) e.getSource();
				String key = (String) model.getValueAt(e.getLastRow(), 0);
				REpiceaMatchWithEnumObject match = (REpiceaMatchWithEnumObject) caller.matchMaps.get(model.enumForThisTableModel).get(key);
				if (e.getColumn() == 1) {	// the event occurred in the match object
					Enum<?> value = (Enum) model.getValueAt(e.getLastRow(), 1);
					match.setValue(value);
					System.out.println("New match : " + key + " = " + value.toString());
				} else { // it comes from the additional columns
					Object newValue = getTable(model.enumForThisTableModel).getValueAt(e.getLastRow(), e.getColumn());
					match.setValueAt(e.getColumn() - 2, newValue);  // first two columns are the key and the match 
					System.out.println("New match : " + key + " (column " + e.getColumn() + ") set to " + newValue.toString());
				}
				firePropertyChange(REpiceaAWTProperty.ActionPerformed, "", "table updated");
			}
		}
	}

}
