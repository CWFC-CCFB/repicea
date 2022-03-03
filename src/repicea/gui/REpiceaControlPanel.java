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
package repicea.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import repicea.gui.UIControlManager.CommonControlID;

/**
 * The REpiceaControlPanel class is a panel that contains two buttons: ok and cancel. 
 * There is a listener and the linked REpiceaDialog instance is notified.
 * @author Mathieu Fortin - February 2021
 */
@SuppressWarnings("serial")
public class REpiceaControlPanel extends REpiceaPanel implements ActionListener {
	
	protected final JButton okButton;
	protected final JButton cancelButton;
	protected final REpiceaDialog owner;
	
	/**
	 * Constructor.
	 * @param owner the REpiceaDialog instance to which this panel is added.
	 */
	public REpiceaControlPanel(REpiceaDialog owner) {
		super();
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.owner = owner;
		okButton = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancelButton = UIControlManager.createCommonButton(CommonControlID.Cancel);
		add(okButton);
		add(cancelButton);
	}

	@Override
	public void refreshInterface() {}

	@Override
	public void listenTo() {
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		okButton.removeActionListener(this);
		cancelButton.removeActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(okButton)) {
			owner.okAction();
		} else if (e.getSource().equals(cancelButton)) {
			owner.cancelAction();
		}
	}
	
//	/**
//	 * Press the cancel button. Essentially for JUnit testing.
//	 */
//	public void doClickCancelButton() {
//		cancelButton.doClick();
//	}
//	
//	/**
//	 * Press the ok button. Essentially for JUnit testing.
//	 */
//	public void doClickOkButton() {
//		okButton.doClick();
//	}

}
