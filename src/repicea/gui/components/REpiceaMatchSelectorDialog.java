/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge Epicea.
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
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.serial.Memorizable;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The REpiceaMatchSelectorDialog class is the user interface of the REpiceaMatchSelector.
 * @author Mathieu Fortin - July 2017
 *
 */
@SuppressWarnings("serial")
public class REpiceaMatchSelectorDialog extends REpiceaDialog implements IOUserInterface, OwnedWindow, ActionListener {
	
	private final REpiceaMatchSelector<?> caller;
	private REpiceaTable table;
	private REpiceaTableModel tableModel;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final WindowSettings windowSettings;
	private final JButton okButton;
	private final JButton cancelButton;
	private boolean isCancelled;
	
	protected REpiceaMatchSelectorDialog(REpiceaMatchSelector<?> caller, Window parent, Object[] columnNames) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.caller = caller;
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);

		okButton = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancelButton = UIControlManager.createCommonButton(CommonControlID.Cancel);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		
		tableModel = new REpiceaTableModel(columnNames);
		tableModel.setEditableVetos(0, true);
		table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
		TextableEnum[] possibleTreatments =  caller.potentialMatches.toArray(new TextableEnum[]{});
		table.setDefaultEditor(Enum.class, new REpiceaCellEditor(new JComboBox<TextableEnum>(possibleTreatments), tableModel));
		table.setRowSelectionAllowed(false);

		init();
		
		refreshInterface();
		initUI();
		pack();

	}
	
	protected void init() {}
	
	
	protected JPanel getControlPanel() {
		JPanel pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pane.add(okButton);
		pane.add(cancelButton);
		return pane;
	}
	
	
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
		Object[] record;
		for (Object s : caller.matchMap.keySet()) {
			record = new Object[2];
			record[0] = s;
			record[1] = caller.matchMap.get(s);
			tableModel.addRow(record);
		}
		super.refreshInterface();
	}
	

	@Override
	public void listenTo() {
		tableModel.addTableModelListener(caller);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		tableModel.removeTableModelListener(caller);
		okButton.removeActionListener(this);
		cancelButton.removeActionListener(this);
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
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		getContentPane().add(pane, BorderLayout.CENTER);		
		
		pane.add(Box.createVerticalStrut(10));
		JScrollPane scrollPane = new JScrollPane(table);
		pane.add(createSimplePanel(scrollPane, 20));
		pane.add(Box.createVerticalStrut(10));
		
		getContentPane().add(getControlPanel(), BorderLayout.SOUTH);
	}

	
	protected JPanel createSimplePanel(Component comp, int margin) {
		JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pane.add(Box.createHorizontalStrut(margin));
		pane.add(comp);
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
		if (filename.isEmpty()) {
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(okButton)) {
			okAction();
		} else if (e.getSource().equals(cancelButton)) {
			cancelAction();
		}
	}


}
