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
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class REpiceaTipDialog extends REpiceaGenericShowDialog implements ActionListener {
	
	
	public static enum MessageID implements TextableEnum {
		DoNotShowAnymore("Do not show anymore", "Ne plus afficher"),
		UpdateTitle("News from the latest version", "Nouveaut\u00E9s de la derni\u00E8re version"),
		TipTitle("Tip!", "Astuce!");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	private JCheckBox doNotShowAnymore;
	private JButton close;
	private String title;

	/**
	 * Constructor.
	 * @param parent a Window instance
	 * @param updateFilepath the file that contains the update
	 * @param title a String that defines the title of this dialog
	 * @throws IOException if the html file cannot be read
	 */
	public REpiceaTipDialog(Window parent, String updateFilepath, String title) throws IOException {
		super(parent, updateFilepath);
		this.title = title;
		initUI();
	}


	/**
	 * This method returns true if the option "do not show anymore" has been selected.
	 * @return a boolean
	 */
	public boolean isDoNotShowAnymoreSelected() {
		return doNotShowAnymore.isSelected();
	}
	
	@Override
	protected void initUI(){
		
		/*
		 * Control panel
		 */
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		doNotShowAnymore = new JCheckBox(REpiceaTranslator.getString(MessageID.DoNotShowAnymore));
		doNotShowAnymore.setSelected(false);	// by default is set to false
		
		close = UIControlManager.createCommonButton(CommonControlID.Close);
		
		controlPanel.add(doNotShowAnymore);
		controlPanel.add(close);
		controlPanel.add(Box.createHorizontalStrut(10));


		// sets continue as default (see AmapDialog)
		close.setDefaultCapable(true);
		getRootPane().setDefaultButton(close);

		
		getContentPane().setLayout(new BorderLayout ());
		getContentPane().add(getFileInAScrollPane(), BorderLayout.CENTER);
		getContentPane().add (controlPanel, BorderLayout.SOUTH);

		setTitle(title);
		
		Dimension dim = new Dimension(450,250);
		setMinimumSize(dim);
		setSize(dim);
		close.setDefaultCapable(true);
		getRootPane().setDefaultButton(close);

	}


	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(close)) {
			cancelAction();
		}
	}


	@Override
	public void listenTo() {
		close.addActionListener(this);
	}


	@Override
	public void doNotListenToAnymore() {
		close.removeActionListener(this);
	}

	public static void main(String[] args) throws IOException {
//		String file = File.separator + "home" + File.separator +
//				"fortin" + File.separator + 
//				"Documents" + File.separator + 
//				"7_Developpement" + File.separator +
//				"JavaProjects" + File.separator + 
//				"capsis4" + File.separator + 
//				"src" + File.separator + 
//				"quebecmrnf" + File.separator + 
//				"gui" + File.separator + 
//				"tips" + File.separator + 
//				"sybilleTip_fr.html";
		
		String file = File.separator + "home" + File.separator + 
				"fortin" + File.separator + 
				"Documents" + File.separator + 
				"7_Developpement" + File.separator + 
				"JavaProjects" + File.separator + 
				"lerfob-foresttools" + File.separator + 
				"src" + File.separator + 
				"lerfob" + File.separator + 
				"carbonbalancetool" + File.separator + 
				"CATLicense_en.txt";
		
		JDialog dlg = null;
		REpiceaTipDialog glw = new REpiceaTipDialog(dlg, file, REpiceaTranslator.getString(MessageID.TipTitle));
		glw.setVisible(true);
		System.out.println(glw.isDoNotShowAnymoreSelected());
		System.exit(0);
	}

}
