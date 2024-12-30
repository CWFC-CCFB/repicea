/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import repicea.gui.OwnedWindow;
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
 * The REpiceaMatchSelectorDialog class is the user interface of the REpiceaMatchSelector.
 * @author Mathieu Fortin - July 2017
 *
 */
@SuppressWarnings("serial")
public class REpiceaMatchSelectorDialog<E> extends REpiceaDialog implements IOUserInterface, OwnedWindow {
	
	private final REpiceaMatchSelector<?> caller;
	private REpiceaTable table;
	private REpiceaTableModel tableModel;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final WindowSettings windowSettings;
	private boolean isCancelled;
	protected final REpiceaControlPanel controlPanel;
	
	protected REpiceaMatchSelectorDialog(REpiceaMatchSelector<E> caller, Window parent, Object[] columnNames) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.caller = caller;
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);

		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		new REpiceaMemorizerHandler(this);
		tableModel = new REpiceaTableModel(columnNames);
		tableModel.setEditableVetos(0, true);
		table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
		table.putClientProperty("terminateEditOnFocusLost", true);
		// MF2020-11-26 Bug fixed, the enum might not implement the TextableEnum interface. Anyway, it all goes through the toString method.
		//		TextableEnum[] possibleTreatments =  caller.potentialMatches.toArray(new TextableEnum[]{});
		Object[] possibleTreatments =  caller.potentialMatches.toArray();
		table.setDefaultEditor(Object.class, new REpiceaCellEditor(new JComboBox<Object>(possibleTreatments), tableModel));
		table.setRowSelectionAllowed(false);

		controlPanel = new REpiceaControlPanel(this);
		
		init();
		
		refreshInterface();
		initUI();
		pack();

	}
	
	protected void init() {}
	
	protected REpiceaMatchSelector<?> getCaller() {return caller;}
	
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
	
	@Override
	public void refreshInterface() {
		tableModel.removeAll();
		List<Object> l = new ArrayList<Object>();
		for (Object s : caller.matchMap.keySet()) {
			l.clear();
			Object currentMatch = caller.matchMap.get(s);
			l.add(s);
			l.add(currentMatch);
			if (currentMatch instanceof REpiceaMatchComplexObject) {
				l.addAll(((REpiceaMatchComplexObject) currentMatch).getAdditionalFields());
			}
			tableModel.addRow(l.toArray());
		}
		super.refreshInterface();
	}

	@Override
	public void listenTo() {
		tableModel.addTableModelListener(caller);
	}

	@Override
	public void doNotListenToAnymore() {
		tableModel.removeTableModelListener(caller);
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

	protected REpiceaTable getTable() {return table;}
	
	protected JPanel getMainPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		pane.add(Box.createVerticalStrut(10));
		JScrollPane scrollPane = new JScrollPane(getTable());
		pane.add(createSimplePanel(scrollPane, 10));
		pane.add(Box.createVerticalStrut(10));
		return pane;
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
		refreshInterface();
		refreshTitle();
		listenTo();
	}

	@Override
	public Memorizable getWindowOwner() {
		return caller;
	}

}
