/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.processsystem;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import repicea.app.SettingMemory;
import repicea.gui.CommonGuiUtility;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaDialog;
import repicea.gui.Resettable;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.serial.Memorizable;
import repicea.serial.REpiceaMemorizerHandler;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class SystemManagerDialog extends REpiceaDialog implements ActionListener, 
									IOUserInterface,
									Resettable,
									OwnedWindow {
	
	protected static enum MessageID implements TextableEnum {
		SliderTitle("Output flux", "Flux sortant"),
		Unnamed("Unnamed", "SansNom"),
		Burn("Burn","Graver");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
		
	static {
		UIControlManager.setTitle(SystemManagerDialog.class, "System Manager", "Gestionnaire de syst\u00E8me");
	}
	
	protected SystemPanel systemPanel;
	protected ToolPanel toolPanel;
	private final SystemManager caller;

	protected JMenuItem load;
	protected JMenuItem save;
	protected JMenuItem saveAs;
	protected JMenuItem close;
	protected JMenuItem reset;
	protected JMenuItem help;
	protected JMenuItem undo;
	protected JMenuItem redo;
	
	protected final WindowSettings windowSettings;
	
	protected SystemManagerDialog(Window parent, SystemManager systemManager) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		setCancelOnClose(false);	// closing by clicking on the "x" is interpreted as ok
		this.caller = systemManager;
		init();
		initUI();
		setMinimumSize(new Dimension(400,500));
		pack();
	}

	protected SystemPanel createSystemPanel() {
		return new SystemPanel(getCaller(), createSystemLayout());
	}
	
	
	protected void init() {
		systemPanel = createSystemPanel();
		setToolPanel();
		CommonGuiUtility.enableThoseComponents(toolPanel, AbstractButton.class, getCaller().getGUIPermission().isEnablingGranted());
		
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);;
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		
		close = UIControlManager.createCommonMenuItem(CommonControlID.Close);
		reset = UIControlManager.createCommonMenuItem(CommonControlID.Reset);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		
		undo = UIControlManager.createCommonMenuItem(CommonControlID.Undo);
		redo = UIControlManager.createCommonMenuItem(CommonControlID.Redo);
		
		new REpiceaMemorizerHandler(this, undo, redo);
	}
	
	protected void setToolPanel() {
		toolPanel = new ToolPanel(systemPanel);
	}

	protected SystemLayout createSystemLayout() {
		return new SystemLayout();
	}
	
	protected JMenu createFileMenu() {
		JMenu file = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		file.add(load);
		file.add(save);
		file.add(saveAs);
		file.add(new JSeparator());
		file.add(close);
		return file;
	}
	
	protected JMenu createEditMenu() {
		JMenu edit = UIControlManager.createCommonMenu(CommonMenuTitle.Edit);
		edit.add(reset);
		edit.addSeparator();
		edit.add(undo);
		edit.add(redo);
		return edit;
	}
	
	
	protected JMenu createAboutMenu() {
		JMenu about = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		about.add(help);
		return about;
	}
	
	/**
	 * This method returns the SystemManager instance behind this dialog.
	 * @return a SystemManager instance.
	 */
	public SystemManager getCaller() {return caller;}
	
	@Override
	protected void initUI() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = createFileMenu();
		menuBar.add(fileMenu);
		fileMenu.setEnabled(getCaller().getGUIPermission().isEnablingGranted());

		JMenu editMenu = createEditMenu();
		menuBar.add(editMenu);
		editMenu.setEnabled(getCaller().getGUIPermission().isEnablingGranted());
		
		menuBar.add(createAboutMenu());
		
		getContentPane().setLayout(new BorderLayout());
//		JScrollPane scrollPane = new REpiceaScrollPane(systemPanel);
		getContentPane().add(systemPanel, BorderLayout.CENTER);
		getContentPane().add(toolPanel, BorderLayout.WEST);
		refreshTitle();
	}
	
	
	@Override
	public void listenTo() {
		reset.addActionListener(this);
		close.addActionListener(this);
		help.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		reset.removeActionListener(this);
		close.removeActionListener(this);
		help.removeActionListener(this);
	}

	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public void postLoadingAction() {
		firePropertyChange(REpiceaAWTProperty.SynchronizeWithOwner, null, this);
	}
	
	private String getTitleForThisDialog() {
		String titleOfThisClass = UIControlManager.getTitle(getClass());
		if (titleOfThisClass.isEmpty()) {
			return UIControlManager.getTitle(SystemManagerDialog.class); // Default title
		} else {
			return titleOfThisClass;
		}
	}
	
	/**
	 * The method sets the title of the dialog.
	 */
	protected void refreshTitle() {
		String filename = getCaller().getName();
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
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(close)) {
			okAction();
		} else if (evt.getSource().equals(reset)) {
			reset();
		} else if (evt.getSource().equals(help)) {
			helpAction();
		} 
	}

	@Override
	public void reset() {
		getCaller().reset();
		synchronizeUIWithOwner();
		firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, "reset just done");
	}

	@Override
	public void synchronizeUIWithOwner() {
		doNotListenToAnymore();
		systemPanel.initUI();
		systemPanel.refreshInterface();
		refreshTitle();
		listenTo();
	}

	@Override
	public Memorizable getWindowOwner() {return getCaller();}

	public SettingMemory getSettingMemory() {return windowSettings;}
	
}
