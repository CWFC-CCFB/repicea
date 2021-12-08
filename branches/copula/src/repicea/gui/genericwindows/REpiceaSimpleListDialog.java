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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.FontType;


/**
 * This dialog is a generic dialog that shows a list. 
 * The user can select only one entry of the list.
 * It is used to select the stratum in Artemis module for example
 * @author Mathieu Fortin - August 2009
 */
@SuppressWarnings("serial")
public class REpiceaSimpleListDialog extends REpiceaDialog implements ActionListener {
	
	/*
	 * Members of this class
	 */
	@SuppressWarnings("rawtypes")
	private JList m_oList;
	private JButton ok;
	private JButton cancel;
	private String strText;
	
	private boolean isValidated = false;


	/**
	 * General constructor.
	 * @param owner the Window instance that acts as owner for this simple dialog (can be null)
	 * @param vector a Vector of instances that represents the different options
	 * @param title the title of the dialog
	 * @param text the text of the dialog
	 * @param sortingEnabled sorts the value of the input vector (true by default)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public REpiceaSimpleListDialog(Window owner, 
			Vector vector, 
			String title, 
			String text,
			boolean sortingEnabled) {
		super(owner);
	
		if (sortingEnabled) {
			Collections.sort(vector);
		}
		
		m_oList = new JList(vector);
		strText = text;
		
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		
		initUI();
		setTitle(title);
		pack();	// uses component's preferredSize
		setVisible(true);
	}
	
	
	/**
	 * Constructor 2. With sorting enabled by default.
	 * @param owner the Window instance that acts as owner for this simple dialog (can be null)
	 * @param vector a Vector of instances that represents the different options
	 * @param title the title of the dialog
	 * @param text the text of the dialog
	 */
	public REpiceaSimpleListDialog(Window owner, Vector<?> vector, String title, String text) {
		this(owner, vector, title, text, true);
	}

	/**
	 * Constructor 3. Without owner.
	 * @param vector a Vector of String instances
	 * @param title the title of the dialog
	 * @param text the text of the dialog
	 */
	public REpiceaSimpleListDialog(Vector<String> vector, String title, String text) {
		this(null, vector, title, text);
	}

		
	/**
	 * This method returns the String selected in the list.
	 * @return the selected String  
	 */
	public Object getSelectedValue() {
		return m_oList.getSelectedValue();				// return the selected value of the JList object
	}

	/**
	 * This method returns the selected index the list.
	 * @return an integer 
	 */
	public int getSelectedIndex() {
		return m_oList.getSelectedIndex();				// return the selected value of the JList object
	}

	public boolean isValidated() {return this.isValidated;}
	
	@Override
	public void okAction() {
		isValidated = true;
		super.okAction();
	}

	/* 
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			isValidated = false;
			setVisible(false);
		}
	}
		
	@Override
	protected void initUI () {

		m_oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_oList.setSelectedIndex(0);

		// 1. selection panel
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel subPanel1 = new JPanel();
		subPanel1.setLayout(new BoxLayout(subPanel1, BoxLayout.Y_AXIS));
		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new BoxLayout(subPanel2, BoxLayout.Y_AXIS));
		JScrollPane	panelList = new JScrollPane(m_oList);

		JTextArea label1 = new JTextArea(strText);
		label1.setWrapStyleWord(true);
		label1.setLineWrap(true);
		label1.setEditable(false);
		label1.setBackground(this.getBackground());
		label1.setFont(UIControlManager.getFont(FontType.LabelFont));
		label1.setSize(200, 150);
		subPanel1.add (label1);

		panel1.add (subPanel1);

		subPanel2.add(panelList);

		panel2.add (subPanel2);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (panel1, BorderLayout.WEST);
		getContentPane ().add (panel2, BorderLayout.CENTER);

		// 2. control panel (ok cancel help);
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		pControl.add (ok);
		pControl.add (cancel);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					okAction();
				}
			}
		};
		
		m_oList.addMouseListener(mouseListener); 		// to enable the double-click option in the intervention dialog

		getContentPane ().add (pControl, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);
		setMinimumSize(new Dimension(400, 170));
	}

	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener (this);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener(this);
		cancel.removeActionListener (this);
	}

}


