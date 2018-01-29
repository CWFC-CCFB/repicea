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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


/**
 * GenericSplashWindow - A welcome window to show acknowledgements and logo
 * Stays visible during the specified time in the constructor.
 * @author Jean-Fran�ois Lavoie and Mathieu Fortin  - May 2009
 */
@SuppressWarnings("serial")
public class REpiceaSplashWindow extends JDialog {
	
	/**
	 * Subclass ToDoTask is a timer that makes the window appears for a specified number of seconds. 
	 */
	private class ToDoTask extends TimerTask  {
		
	    private REpiceaSplashWindow window;
	    
		public ToDoTask(REpiceaSplashWindow window) {
	    	this.window = window;
	    }
		
		public void run ()   {
	      splashTimer.cancel(); //Terminate the thread
	      window.dispose();
	    }
		
	}

	protected Timer splashTimer;
	protected String imagePath;
	
	/**
	 * The constructor requires a file that contains a logo and a number of seconds.
	 * @param imagePath a file that contains the logo to be displayed
	 * @param nbSec a double that represents the number of seconds the logo appears on screen.
	 * @param parent the parent component which can be null
	 */
	public REpiceaSplashWindow (String imagePath, double nbSec, Component parent) {
		this.imagePath = imagePath;
		try  {
			splashWindow(parent);
			splashTimer = new Timer();
			int nbMMSec = (int) (nbSec * 1000);
			splashTimer.schedule(new ToDoTask(this), nbMMSec) ;
			pack();
			setVisible(true);
		} catch (Exception e) {
			System.out.println("Unable to initialize the splash window!");
			if (isVisible()) {
				dispose();
			}
		} 
	}
	
	/** 
	 * Fills the window with the logo contained in the file
	 */
	private void splashWindow(Component parent) throws Exception {
		ImageIcon icon;
		if (new File(imagePath).exists()) {
			icon = new ImageIcon(this.imagePath.toString());		 
		} else {
			InputStream in = ClassLoader.getSystemResourceAsStream(imagePath);
			Image image = ImageIO.read(in);
			icon = new ImageIcon(image);
		}
	
		JLabel image = new JLabel(icon);
		Border blackline = BorderFactory.createLineBorder(Color.DARK_GRAY, 3);
		image.setBorder(blackline);
		image.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		JPanel imagePanel = new JPanel();
		imagePanel.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

		imagePanel.add(image);

		setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		add(imagePanel);
		setLocationRelativeTo(parent);	// put the window in the center of the screen

		setResizable(false);
		setUndecorated(true);	// plain window
		setModal(true);			// modal window to avoid any interaction with other windows
	}
}




