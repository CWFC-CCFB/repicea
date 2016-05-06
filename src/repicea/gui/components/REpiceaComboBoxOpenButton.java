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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;
import repicea.gui.ShowableObjectWithParent;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.gui.permissions.REpiceaGUIPermission;

@SuppressWarnings("serial")
public class REpiceaComboBoxOpenButton<P extends ShowableObjectWithParent> extends REpiceaPanel implements ActionListener {

	protected final JComboBox<P> comboBox;
	private JButton openButton;
	private final CopyOnWriteArrayList<PropertyChangeListener> propertyListeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	/**
	 * Specific constructor with eventual restricted permissions.
	 * @param label the label of the combobox
	 * @param permission a REpiceaGUIPermission instance 
	 */
	public REpiceaComboBoxOpenButton(String label, REpiceaGUIPermission permission) {
		comboBox = new JComboBox<P>();
		FontMetrics fm = comboBox.getFontMetrics(comboBox.getFont());
		int width = fm.stringWidth("e") * 35 + 2;
		int height = fm.getHeight() + 5;
		Dimension dim = new Dimension(width, height);
		comboBox.setPreferredSize(dim);
		comboBox.setEnabled(permission.isEnablingGranted());
		
		openButton = UIControlManager.createCommonButton(CommonControlID.Open);
		openButton.setText("");
		openButton.setMargin(new Insets(2,2,2,2));
		openButton.setToolTipText(CommonControlID.Open.toString());
//		openButton.setEnabled(permission.isEnablingGranted());		// correction even if the option cannot be changed it should be at least opened

		setLayout(new BorderLayout());
		Border border = BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(border, label));
//		add(UIControlManager.getLabel(label));
//		add(Box.createHorizontalStrut(5));
		JPanel subPanel = new JPanel(new BorderLayout());
		subPanel.add(comboBox, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(5));
		subPanel.add(openButton, BorderLayout.EAST);
		add(subPanel, BorderLayout.CENTER);
	}

	/**
	 * Default constructor with permissions granted.
	 * @param label the label of the combobox
	 */
	public REpiceaComboBoxOpenButton(String label) {
		this(label, new DefaultREpiceaGUIPermission(true));
	}

	public void addComboBoxEntryPropertyListener(PropertyChangeListener listener) {
		if (!propertyListeners.contains(listener)) {
			propertyListeners.add(listener);
		}
	}

	
	public void removeComboBoxEntryPropertyListener(PropertyChangeListener listener) {
		propertyListeners.remove(listener);
	}

	
	@Override
	public void refreshInterface() {}

	@Override
	public void listenTo() {
		openButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		openButton.removeActionListener(this);
	}

	/**
	 * This method returns the JComboBox instance for adding listeners or other eventual changes.
	 * @return a JComboBox
	 */
	public JComboBox<P> getComboBox() {return comboBox;}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(openButton)) {
			if (comboBox.getSelectedIndex() != -1) {
				Window window = CommonGuiUtility.getParentWindow(this);
				Component comp = comboBox.getItemAt(comboBox.getSelectedIndex()).getGuiInterface(window);
				for (PropertyChangeListener listener : propertyListeners) {
					comp.addPropertyChangeListener(listener);
				}
				comboBox.getItemAt(comboBox.getSelectedIndex()).showInterface(window);
				for (PropertyChangeListener listener : propertyListeners) {
					comp.removePropertyChangeListener(listener);
				}
			}
		}
	}

	@Override
	public void setEnabled(boolean b) {
		openButton.setEnabled(b);
		comboBox.setEnabled(b);
	}
//	@Override
//	public void firePropertyChange(REpiceaAWTProperty propertyName,	Object obj1, Object obj2) {
//		firePropertyChange(propertyName.name(), obj1, obj2);
//	}

}
