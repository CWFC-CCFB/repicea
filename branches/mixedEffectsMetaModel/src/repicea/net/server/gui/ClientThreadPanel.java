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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;


/**
 * This private class is the GUI interface of the ClientThread class.
 * @author Mathieu Fortin - October 2011
 */
public class ClientThreadPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 20111018L;

	private JButton restartButton;
	private JProgressBar monitorBar;
	private JLabel threadIDLabel;
	private JLabel statusLabel;
	private Component horizontalStrut_2;
	private JLabel clientIDLabel;
	private int workerID;
	
	protected ClientThreadPanel(int workerID) {
		this.workerID = workerID;
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		createUI();
		setRestartButtonEnabled(false);			// default value
	}

	private void createUI() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		threadIDLabel = new JLabel("Client thread no " + workerID);
		add(threadIDLabel);
		Component horizontalStrut = Box.createHorizontalStrut(20);
		add(horizontalStrut);
		
		restartButton = new JButton("Restart");
		restartButton.setName("restartButton");
		add(restartButton);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		add(horizontalStrut_1);
		
		monitorBar = new JProgressBar();
		add(monitorBar);
		monitorBar.setForeground(Color.GREEN);
		
		horizontalStrut_2 = Box.createHorizontalStrut(20);
		add(horizontalStrut_2);
		
		statusLabel = new JLabel();
		setStatusLabel("Uninstantiated");
		add(statusLabel);
		
		clientIDLabel = new JLabel("None");
		add(clientIDLabel);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals("status")) {
			String status = arg0.getNewValue().toString();
			setStatusLabel(status);
		} else if (propertyName.equals("clientID")) {
			String clientID = "none";
			if (arg0.getNewValue() != null) {
				clientID = arg0.getNewValue().toString();
			} 
			clientIDLabel.setText(clientID);
		} else if (propertyName.equals("progressBar")) {
			setProgressBarStatus((Boolean) arg0.getOldValue(), (Boolean) arg0.getNewValue());
		} else if (propertyName.equals("currentStatus")) {
			@SuppressWarnings("unchecked")
			Map<String, PropertyChangeEvent> propertyMap = (Map<String, PropertyChangeEvent>) arg0.getNewValue();
			for (PropertyChangeEvent pce : propertyMap.values()) {
				propertyChange(pce);
			}
		} else if (propertyName.equals("restartButton")) {
			setRestartButtonEnabled((Boolean) arg0.getNewValue());
		}
	}

	private void setStatusLabel(String status) {
		statusLabel.setText(status);
		if (status.equals("Uninstantiated")) {
			statusLabel.setForeground(Color.BLACK);
		} else if (status.equals("Interrupted")) {
			statusLabel.setForeground(Color.RED);
		} else {
			statusLabel.setForeground(Color.GREEN);
		}
	}
	
	private void setProgressBarStatus(boolean enabled, boolean interrupted) {
		if (enabled) {
			monitorBar.setIndeterminate(true);
		} else {
			if (!interrupted) {
				monitorBar.setIndeterminate(false);
			} else {
				monitorBar.setIndeterminate(false);
			}
		}
	}
	
	private void setRestartButtonEnabled(boolean restartButtonEnabled) {
		restartButton.setEnabled(restartButtonEnabled);
	}
	
	protected JButton getRestartButton() {return restartButton;}
}
