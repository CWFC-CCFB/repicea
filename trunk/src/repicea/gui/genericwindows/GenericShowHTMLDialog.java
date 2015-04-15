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
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import repicea.gui.REpiceaDialog;

@SuppressWarnings("serial")
public abstract class GenericShowHTMLDialog extends REpiceaDialog {

	private String htmlPath;
	
	/**
	 * Constructor.
	 * @param parent a Window instance
	 * @param htmlPath the html file to show
	 */
	protected GenericShowHTMLDialog(Window parent, String htmlPath) {
		super(parent);
		this.htmlPath = htmlPath;
	}

	/**
	 * This method edits the html file into a scroll panel.
	 * @return a JScrollPane instance
	 */
	protected JScrollPane getHTMLFileInAScrollPane() {
		JEditorPane editorPane = new JEditorPane();
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setBorder(BorderFactory.createLineBorder(this.getBackground(), 10));

		try {
			URL urlAddress;		// address of the HTML file that contains the license
			if (new File(htmlPath).exists()) {
				urlAddress = new File(htmlPath).toURI().toURL();
			} else {
				urlAddress = ClassLoader.getSystemResource(htmlPath);
			}
			editorPane.setEditable(false);					
			HTMLEditorKit kit = new HTMLEditorKit();		// HTML kit for rendering of the HTML file
			StyleSheet styleSheet = new StyleSheet();	
			styleSheet.importStyleSheet(urlAddress);		// The style sheet is set to the one defined in the HTML file
			kit.setStyleSheet(styleSheet);					// The style sheet is set into the HTML kit
			editorPane.setEditorKit(kit);					// The kit is set into the Editor Pane
			editorPane.setPage(urlAddress);					// The page of the Editor is set to the address of the HTML file
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scrollPane;
	}
	

}
