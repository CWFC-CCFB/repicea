/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge-Epicea
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
package repicea.io;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractButton;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import repicea.io.GFileFilter.FileType;

/**
 * An implementation of the REpiceaSaveAsHandlerUI abstract class for
 * SVG files.
 * @author Mathieu Fortin - May 2021
 *
 */
public class REpiceaOSVGFileHandlerUI extends REpiceaSaveAsHandlerUI implements ActionListener {

	protected static boolean TestMode = false;
	protected static String Filename = "";
	
	private final AbstractButton saveAsButton;
	private final Component componentToExport;

	/**
	 * Constructor.
	 * @param mainWindow the main window the FileChooser instance depends on.
	 * @param saveAsButton a JMenuItem or other control that triggers the save as action.
	 * @param componentToExport the component to be drawn into the SVG file
	 */
	public REpiceaOSVGFileHandlerUI(Component mainWindow, AbstractButton saveAsButton, Component componentToExport) {
		super(mainWindow, FileType.SVG.getFileFilter());
		this.saveAsButton = saveAsButton;
		this.saveAsButton.addActionListener(this);
		this.componentToExport = componentToExport;
	}

	@Override
	protected void internalSaveAction(String filename) throws Exception {
		saveAsSVN(componentToExport, filename);
//	    // Get a DOMImplementation.
//	    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
//	    // Create an instance of org.w3c.dom.Document.
//	    String svgNS = "http://www.w3.org/2000/svg";
//	    Document document = domImpl.createDocument(svgNS, "svg", null);
//	    // Create an instance of the SVG Generator.
//	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
//	    componentToExport.paint(svgGenerator);
//	    // Finally, stream out SVG to the standard output using
//	    // UTF-8 encoding.
//	    boolean useCSS = true; // we want to use CSS style attributes
//	    FileWriter writer = new FileWriter(filename);
//	    svgGenerator.stream(writer, useCSS);
//	    writer.close();
	}
	
	
	protected static void saveAsSVN(Component component, String filename) throws IOException {
	    // Get a DOMImplementation.
	    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = "http://www.w3.org/2000/svg";
	    Document document = domImpl.createDocument(svgNS, "svg", null);
	    // Create an instance of the SVG Generator.
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
	    component.paint(svgGenerator);
	    // Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
	    boolean useCSS = true; // we want to use CSS style attributes
	    FileWriter writer = new FileWriter(filename);
	    svgGenerator.stream(writer, useCSS);
	    writer.close();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(saveAsButton)) {
			if (TestMode)
				try {
					internalSaveAction(Filename);
				} catch (Exception e) {
					e.printStackTrace();
				}
			else saveAsAction();
		}
	}

}
