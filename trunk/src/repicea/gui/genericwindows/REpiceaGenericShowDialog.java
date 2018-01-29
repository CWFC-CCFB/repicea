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

import java.awt.Window;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import repicea.gui.REpiceaDialog;
import repicea.io.GFileFilter;

@SuppressWarnings("serial")
public abstract class REpiceaGenericShowDialog extends REpiceaDialog {

	private String filePath;
	
	/**
	 * Constructor.
	 * @param parent a Window instance
	 * @param filePath the html file to show
	 */
	protected REpiceaGenericShowDialog(Window parent, String filePath) {
		super(parent);
		this.filePath = filePath;
	}

	/**
	 * This method edits the html file into a scroll panel.
	 * @return a JScrollPane instance
	 */
	protected JScrollPane getFileInAScrollPane() {
		JEditorPane editorPane = new JEditorPane();
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setBorder(BorderFactory.createLineBorder(this.getBackground(), 10));

		try {
			URL urlAddress;		// address of the HTML file that contains the license
			if (new File(filePath).exists()) {
				urlAddress = new File(filePath).toURI().toURL();
			} else {
				urlAddress = ClassLoader.getSystemResource(filePath);
			}
			editorPane.setEditable(false);		
			StyledEditorKit kit;
			if (filePath.endsWith(GFileFilter.HTML.getExtension())) {
				kit = new HTMLEditorKit();		// HTML kit for rendering of the HTML file
				StyleSheet styleSheet = new StyleSheet();	
				styleSheet.importStyleSheet(urlAddress);		// The style sheet is set to the one defined in the HTML file
				((HTMLEditorKit) kit).setStyleSheet(styleSheet);					// The style sheet is set into the HTML kit
			} else {
				kit = new StyledEditorKit();	// default kit otherwise
			}
			
			editorPane.setEditorKit(kit);					// The kit is set into the Editor Pane
			editorPane.setPage(urlAddress);					// The page of the Editor is set to the address of the HTML file
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scrollPane;
	}
	

}
