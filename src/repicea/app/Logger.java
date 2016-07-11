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
package repicea.app;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import repicea.gui.REpiceaUIObject;

/**
 * The Logger class includes a JTextArea instance in which any OutputStream can write.
 * The class also implements the UserInterfaceable interface, which means it returns 
 * a JScrollPane that contains the JTextArea.
 * @author Mathieu Fortin - October 2012
 */
public class Logger extends OutputStream implements Runnable, REpiceaUIObject {

	private JScrollPane guiInterface;
	private JTextArea textArea;
	private StringWriter writer;
	private final Object lock = new Object();
	private boolean isFlushing;
	
	/**
	 * Constructor.
	 */
	public Logger(int nbRows) {
		writer = new StringWriter();
		textArea = new JTextArea();
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setWrapStyleWord(true);
		textArea.setRows(nbRows);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
	}
	
	/**
	 * Default constructor with 40 rows.
	 */
	public Logger() {
		this(40);
	}
	
	/**
	 * This method connects this instance to the System output streams (error and output).
	 * As a result, all exception.printStackTrace() calls are reported in the JTextArea.
	 */
	public void connectToSystemOutputStream() {
		PrintStream ps = new PrintStream(this, true);
		System.setOut(ps);
		System.setErr(ps);
	}

	/**
	 * This method clears the JTextArea.
	 */
	public void clear() {
		textArea.setText("");
	}
	
	@Override
	public void run() {
		writeTextIntoTextArea();
	}
	
	private void writeTextIntoTextArea() {
		synchronized(lock) {
			isFlushing = true;
			textArea.append(writer.toString());
			textArea.setCaretPosition(textArea.getDocument().getLength());
			writer.getBuffer().setLength(0);
			isFlushing = false;
			lock.notify();
		}
	}
	
	
	@Override
	public void flush() {
		SwingUtilities.invokeLater(this);
	}
	
	@Override
	public void write(int arg0) throws IOException {
		synchronized(lock) {							// we dont want to store characters in the writer while the writer is flushing (otherwise these characters are lost)
			while (isFlushing) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		writer.write(arg0);
	}

	@Override
	public JScrollPane getUI() {
		if (guiInterface == null) {
			guiInterface = new JScrollPane();
			guiInterface.setViewportView(textArea);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}
	
	

}
