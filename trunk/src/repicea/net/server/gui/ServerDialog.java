/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge Epicea.
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
package repicea.net.server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import repicea.gui.REpiceaFrame;
import repicea.net.server.gui.InterfaceTask.InterfaceRelatedTask;

/**
 * This class is the gui interface of the CapsisServer class.
 * @author Mathieu Fortin - October 2012
 */
public class ServerDialog extends REpiceaFrame implements PropertyChangeListener {

	private static final long serialVersionUID = 20111018L;
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu updaterMenu;
	private JMenu mnOptions;
	private JMenuItem mntmQuit;
	private JMenuItem mntmConnect;
	private JMenuItem mntmDisconnect;
	private JMenuItem mntmExceptionsRules;
	private JMenuItem mntmPreferences;
	private JMenuItem mntmShutdown;
	
	private JPanel mainPanel;
	
	private boolean connected;
	
	private Vector<ClientThreadPanel> clientThreadPanels;
	
	private ServerInterfaceEngine caller;

	protected ServerDialog(ServerInterfaceEngine caller) {
		
		clientThreadPanels = new Vector<ClientThreadPanel>();
		
		this.caller = caller;
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		mntmConnect = new JMenuItem("Connect");
		fileMenu.add(mntmConnect);
	
		mntmDisconnect = new JMenuItem("Disconnect");
		fileMenu.add(mntmDisconnect);
		
		mntmQuit = new JMenuItem("Quit");
		fileMenu.add(mntmQuit);
		
		updaterMenu = new JMenu("Updater");
		menuBar.add(updaterMenu);
		
		mntmShutdown = new JMenuItem("Stop");
		updaterMenu.add(mntmShutdown);
		
		mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);
		
		mntmExceptionsRules = new JMenuItem("Exceptions rules");
		mnOptions.add(mntmExceptionsRules);
		
		mntmPreferences = new JMenuItem("Preferences");
		mnOptions.add(mntmPreferences);
		
		setConnected(false);
		
		setResizable(false);
		createUI();
	}

	
	private void setConnected(boolean b) {
		connected = b;
		mntmConnect.setEnabled(!b);
		mntmDisconnect.setEnabled(b);
		updaterMenu.setEnabled(b);
		mntmExceptionsRules.setEnabled(b);
	}


//	/*
//	 * Just to avoid errors with the WindowBuilder
//	 */
//	@Override
//	protected void setIcon() {}
	
	private void createUI() {
		updateTitle(null);
		
		getContentPane().add(menuBar, BorderLayout.NORTH);
		
		mainPanel = new JPanel();
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		
		Dimension dim = new Dimension(800,400);
		setSize(dim);
	}

	private void updateTitle(String refPath) {
		String title = "Updater Server Application ";
		if (refPath != null && !refPath.isEmpty()) {
			title = title.concat("- Reference directory : " + refPath);
		}
		setTitle(title); // + caller.getReferenceRepository().getRootDirectory().toString());
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() instanceof JMenuItem) {
			JMenuItem menuItem = (JMenuItem) arg0.getSource();
			if (menuItem.equals(mntmConnect)) {
				connectAction();
			} else if (menuItem.equals(mntmDisconnect)) {
				disconnectAction();
			} else if (menuItem.equals(mntmQuit)) {
				cancelAction();
			} else if (menuItem.equals(mntmExceptionsRules)) {
				exceptionRulesAction();
			} else if (menuItem.equals(mntmShutdown)) {
				shutdownServerAction();
			}
		}
	}

	
	private void shutdownServerAction() {
		if (JOptionPane.showConfirmDialog(this, 
				"Do you really want to shutdown the server application? Any connected client will be automatically disconnected.", 
				"Warning!", 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE) == 0) {
			firePropertyChange("shutdownServer", null, null);
			disconnectAction();
		}
	}


	private void connectAction() {
		String selectedPort = JOptionPane.showInputDialog(this, "Please select a port", "Port selection", JOptionPane.QUESTION_MESSAGE);
		try {
			int port = Integer.parseInt(selectedPort);
			if (port < 0 || port > 65535) {
				throw new NumberFormatException();
			}
			caller.setCommunicationPort(port);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "This port number is invalid. Please select a number between 0 and 65535.", "Port selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		caller.addTask(new InterfaceTask(caller, InterfaceRelatedTask.Connect));
	}

	
	private void disconnectAction() {
		caller.addTask(new InterfaceTask(caller, InterfaceRelatedTask.Disconnect));
		updateTitle(null);
		setConnected(false);
		clientThreadPanels.clear();
		refreshMainPanel();
	}
	
	private void exceptionRulesAction() {
		firePropertyChange("exceptionRuleRequest", null, null);
	}


	@Override
	public void cancelAction() {
		if (JOptionPane.showConfirmDialog(this, 
				"Do you really want to close the updater interface?", 
				"Warning!", 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE) == 0) {
			super.cancelAction();
			caller.requestShutdown();
		}
	}

	
	protected ClientThreadPanel registerNewClientThreadPanel() {
		ClientThreadPanel panel = new ClientThreadPanel(clientThreadPanels.size() + 1);
		clientThreadPanels.add(panel);
		return panel;
	}


	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals("referencePath")) {
			updateTitle(arg0.getNewValue().toString());
		} else if (propertyName.equals("connected")) {
			setConnected(true);
			refreshMainPanel();
		} 
	}

	
	private void refreshMainPanel() {
		mainPanel.removeAll();
		for (ClientThreadPanel panel : clientThreadPanels) {
			mainPanel.add(panel);
		}
		validate();
		repaint();
	}

	protected boolean isConnected() {return connected;}


	@Override
	public void doNotListenToAnymore() {
		mntmConnect.removeActionListener(this);
		mntmDisconnect.removeActionListener(this);
		mntmQuit.removeActionListener(this);
		mntmShutdown.removeActionListener(this);
		mntmExceptionsRules.removeActionListener(this);
	}


	@Override
	public void listenTo() {
		mntmConnect.addActionListener(this);
		mntmDisconnect.addActionListener(this);
		mntmQuit.addActionListener(this);
		mntmShutdown.addActionListener(this);
		mntmExceptionsRules.addActionListener(this);
	}
	
}
