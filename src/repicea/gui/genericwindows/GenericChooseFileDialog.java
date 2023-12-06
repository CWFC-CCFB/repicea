/*
 * This file is part of the repicea library.
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
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.InvalidParameterException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.io.REpiceaFileFilterList;

/**
 * The GenericChooseFileDialog class implements a simple dialog that allows the user to select either
 * a file or a directory.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
public class GenericChooseFileDialog extends REpiceaDialog implements ActionListener {

	private JButton ok;
	private JButton cancel;
	private JButton browse;
	private JTextField textField;
	private int fileSelectionMode;
	private REpiceaFileFilterList fileFilters;
	private boolean isValidated;
	private String message;
	
	
	/**
	 * Constructor with no file filters.
	 * @param parent a Window instance
	 * @param title the title of the dialog
	 * @param message the message of the dialog
	 * @param defaultPath the default file 
	 * @param fileSelectionMode either JFileChooser.FILES_ONLY (default), JFileChooser.DIRECTORIES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 */
	public GenericChooseFileDialog(Window parent,
			String title, 
			String message, 
			String defaultPath,
			int fileSelectionMode) {
		this(parent, title, message, defaultPath, null, fileSelectionMode);
	}

	
	/**
	 * General constructor.
	 * @param parent a Window instance
	 * @param title the title of the dialog
	 * @param message the message of the dialog
	 * @param defaultPath the default file 
	 * @param fileFilters a List of FileFilter instances (can be null)
	 * @param fileSelectionMode either JFileChooser.FILES_ONLY (default), JFileChooser.DIRECTORIES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 */
	public GenericChooseFileDialog(Window parent,
			String title, 
			String message, 
			String defaultPath,
			REpiceaFileFilterList fileFilters,
			int fileSelectionMode) {
		super(parent);
		this.message = message;
		isValidated = false;
		if (fileFilters != null) {
			this.fileFilters = fileFilters;
		} else {
			this.fileFilters = null;
		}
		if (fileSelectionMode != JFileChooser.DIRECTORIES_ONLY && fileSelectionMode != JFileChooser.FILES_ONLY && fileSelectionMode != JFileChooser.FILES_AND_DIRECTORIES) {
			throw new InvalidParameterException("The file selection mode is invalid!");
		}
		this.fileSelectionMode = fileSelectionMode;
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		browse = UIControlManager.createCommonButton(CommonControlID.Browse);
		textField = new JTextField(defaultPath);
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.setColumns(50);
		setTitle(title);
		initUI();
		pack();
		ok.requestFocusInWindow();
		setMinimumSize(getSize());
	}
	@Override
	protected void initUI() {
		getContentPane().setLayout(new BorderLayout());
		
		JLabel label = UIControlManager.getLabel(message);
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(Box.createHorizontalStrut(5));
		labelPanel.add(label);
		
		JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textFieldPanel.add(Box.createHorizontalStrut(5));
		textFieldPanel.add(textField);
		textFieldPanel.add(Box.createHorizontalGlue());
		textFieldPanel.add(browse);
		
//		JPanel mainPanel = new JPanel();
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//		mainPanel.add(Box.createVerticalStrut(5));
//		mainPanel.add(labelPanel);
//		mainPanel.add(Box.createVerticalStrut(5));
//		mainPanel.add(textFieldPanel);
//		mainPanel.add(Box.createVerticalStrut(5));
		
		JPanel mainPanel = new JPanel(new BorderLayout());
//		mainPanel.add(labelPanel, BorderLayout.NORTH);
		mainPanel.add(textFieldPanel, BorderLayout.CENTER);
		
		getContentPane().add(labelPanel, BorderLayout.NORTH);
		getContentPane().add(textFieldPanel, BorderLayout.CENTER);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(ok);
		controlPanel.add(cancel);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
	}
	
	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener(this);
		browse.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
		browse.removeActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(ok)) {
			okAction();
		} else if (arg0.getSource().equals(cancel)) {
			cancelAction();
		} else if (arg0.getSource().equals(browse)) {
			browseAction();
		}
	}

	private void browseAction() {
		FileChooserOutput fco = CommonGuiUtility.browseAction(this, 
				fileSelectionMode, 
				textField.getText(), 
				fileFilters, 
				JFileChooser.OPEN_DIALOG);
		if (fco.isValid()) {
			textField.setText(fco.getFilename());
		}
	}

	@Override
	public void okAction() {
		File output = new File(textField.getText());
		if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
			if (!output.isDirectory()) {
				JOptionPane.showMessageDialog(this, 
						UIControlManager.InformationMessage.IncorrectFilename.toString(), 
						UIControlManager.InformationMessageTitle.Error.toString(), 
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				isValidated = true;
				super.okAction();
			}
		} else if (fileSelectionMode == JFileChooser.FILES_ONLY) {
			if (!output.isFile()) {
				JOptionPane.showMessageDialog(this, 
						UIControlManager.InformationMessage.IncorrectFilename.toString(), 
						UIControlManager.InformationMessageTitle.Error.toString(), 
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				isValidated = true;
				super.okAction();
			}
		} else if (!output.isDirectory() && !output.isFile()) {
			if (!output.isFile()) {
				JOptionPane.showMessageDialog(this, 
						UIControlManager.InformationMessage.IncorrectFilename.toString(), 
						UIControlManager.InformationMessageTitle.Error.toString(), 
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				isValidated = true;
				super.okAction();
			}
		} 
	}


	/**
	 * This method returns true if the dialog has been cancelled.
	 * @return a boolean
	 */
	public boolean isCancelled() {
		return !isValidated;
	}
	
	/**
	 * This method the file the users selected.
	 * @return a File instance or null if the dialog has been cancelled
	 */
	public File getFile() {
		if (isValidated) {
			return new File(textField.getText());
		} else {
			return null;
		}
	}

	
//	public static void main(String[] args) {
//		GenericChooseFileDialog gcfd = new GenericChooseFileDialog(null, 
//				"titre", 
//				"message", 
//				"", 
//				new REpiceaFileFilterList(REpiceaFileFilter.JSON, REpiceaFileFilter.XML), 
//				JFileChooser.FILES_ONLY);
//		gcfd.setVisible(true);
//		System.exit(0);
//	}
	
}
