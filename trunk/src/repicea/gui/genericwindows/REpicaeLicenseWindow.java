/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.genericwindows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.FontType;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


public class REpicaeLicenseWindow extends REpicaeGenericShowDialog implements ActionListener {
	
	private static final long serialVersionUID = 20120104L;

	static {
		UIControlManager.setTitle(REpicaeLicenseWindow.class, "License of use", "Licence d'utilisation");
	}
	
	public static enum ControlID implements TextableEnum {
		AcceptLicense("I accept the terms of this license" , "J'accepte les termes de cette licence d'utilisation");
		
		ControlID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	
	/*
	 * Members of this class
	 */
	private JButton continueButton;
	private JButton cancelButton;
	private JRadioButton acceptingLicenseButton;
	private boolean licenseHasBeenAccepted;
	
	/**
	 * Constructor.
	 * @param parent a Window instance
	 * @param licensePath the File instance that refers to the license text
	 * @throws IOException if the file that contains the license text does not exist or does not have the appropriate format
	 */
	public REpicaeLicenseWindow(Window parent, String licensePath) throws IOException {
		super(parent, licensePath);
		initUI();
	}
	

	@Override
	public void okAction() {
		licenseHasBeenAccepted = true;		// true implies that the welcome window has been seen and that the user has accepted the license
		super.okAction();
	}
	
	
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(acceptingLicenseButton)) {
			if (acceptingLicenseButton.isSelected())
				continueButton.setEnabled(true);
			if (!acceptingLicenseButton.isSelected())
				continueButton.setEnabled(false);
		} else if (evt.getSource().equals(continueButton)) {
			okAction();
		} else if (evt.getSource().equals(cancelButton)) {
			cancelAction();
		}
	}

	@Override
	public void cancelAction() {
		licenseHasBeenAccepted = false;
		super.cancelAction();
	}
	
	public boolean isLicenseAccepted() {
		return licenseHasBeenAccepted;
	}
	
	/** 
	 * Inits the GUI. 
	 */
	@Override
	protected void initUI() {
		
		/*
		 * Control panel
		 */
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		continueButton = UIControlManager.createCommonButton(CommonControlID.Continue);
		cancelButton = UIControlManager.createCommonButton(CommonControlID.Cancel);
		controlPanel.add(continueButton);
		controlPanel.add(cancelButton);
		continueButton.setEnabled(false);				// set to disable until the user accepts the license
		controlPanel.add(Box.createHorizontalStrut(4));

		JPanel accLicencePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		acceptingLicenseButton = new JRadioButton();
		acceptingLicenseButton.setText(REpiceaTranslator.getString(ControlID.AcceptLicense));
		acceptingLicenseButton.setFont(UIControlManager.getFont(FontType.ButtonFont));
		acceptingLicenseButton.setSelected(false);	// by default is set to false
		accLicencePanel.add(acceptingLicenseButton);

		JPanel c1 = new JPanel();
		BoxLayout layout = new BoxLayout(c1, BoxLayout.Y_AXIS);
		c1.setLayout(layout);
		c1.add(accLicencePanel);
		c1.add(Box.createVerticalStrut(10));
		c1.add(controlPanel);
		
		continueButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(continueButton);

		
		getContentPane().setLayout(new BorderLayout ());
		getContentPane().add(getFileInAScrollPane(), BorderLayout.CENTER);
		getContentPane().add (c1, BorderLayout.SOUTH);

		setTitle(UIControlManager.getTitle(this.getClass()));
		
		Dimension dim = new Dimension(400,400);
		setMinimumSize(dim);
		setSize(dim);
	}

	
	
	@Override
	public void listenTo() {
		continueButton.addActionListener(this);
		cancelButton.addActionListener (this);
		acceptingLicenseButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		continueButton.removeActionListener(this);
		cancelButton.removeActionListener (this);
		acceptingLicenseButton.removeActionListener(this);
	}

	
//	public static void main(String[] args) throws IOException {
//		File htmlFile = new File("C:" + File.separator +
//				"Travail" + File.separator +
//				"7_Developpement" + File.separator +
//				"JavaProjects" + File.separator +
//				"capsis4" + File.separator +
//				"src" + File.separator +
//				"artemis" + File.separator +
//				"license_fr.html");
//		JDialog dlg = null;
//		GeneralLicenseWindow glw = new GeneralLicenseWindow(dlg, htmlFile);
//		glw.setVisible(true);
//		System.out.println(glw.isLicenseAccepted());
//		System.exit(0);
//	}
	
}

