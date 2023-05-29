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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


/**
 * GenericSplashWindow - A welcome window to show acknowledgements and logo
 * Stays visible during the specified time in the constructor.
 * @author Jean-Francois Lavoie and Mathieu Fortin  - May 2009
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
	protected final String imagePath;
	private final String bottomMessage;
	private final int fontSize;
	private final int imageWidth;
	
	
	/**
	 * The constructor requires a file that contains a logo and a number of seconds.
	 * @param imagePath a file that contains the logo to be displayed
	 * @param nbSec a double that represents the number of seconds the logo appears on screen.
	 * @param parent the parent component which can be null
	 * @deprecated Use the constructor REpiceaSplashWindow(String imagePath, double nbSec, Component parent, int imageWidth, String bottomMessage, int fontSize) instead
	 */
	@Deprecated
	public REpiceaSplashWindow(String imagePath, double nbSec, Component parent) {
		this(imagePath, nbSec, parent, -1, null, 10);
	}

	/**
	 * The constructor requires a file that contains a logo and a number of seconds.
	 * @param imagePath a file that contains the logo to be displayed
	 * @param nbSec a double that represents the number of seconds the logo appears on screen.
	 * @param parent the parent component which can be null
	 * @param bottomMessage a message to appear just below the splash window
	 * @deprecated Use the constructor REpiceaSplashWindow(String imagePath, double nbSec, Component parent, int imageWidth, String bottomMessage, int fontSize) instead
	 */ 
	@Deprecated
	public REpiceaSplashWindow(String imagePath, double nbSec, Component parent, String bottomMessage) {
		this(imagePath, nbSec, parent, -1, bottomMessage, 10);
	}

	/**
	 * The constructor requires a file that contains a logo and a number of seconds.
	 * @param imagePath a file that contains the logo to be displayed
	 * @param nbSec a double that represents the number of seconds the logo appears on screen.
	 * @param parent the parent component which can be null
	 * @param imageWidth an integer to set the splash window width, if set to a negative value the original width is used
	 * @param bottomMessage a message to be displayed below the splash window (if null, no message will be displayed)
	 * @param fontSize the font size of the message below the splash window
	 */
	public REpiceaSplashWindow(String imagePath, double nbSec, Component parent, int imageWidth, String bottomMessage, int fontSize) {
		if (nbSec <= 0) 
			throw new InvalidParameterException("The nbSec parameter must be greater than 0!");
		if (fontSize <= 0) 
			throw new InvalidParameterException("The size parameter must be greater than 0!");
		this.imagePath = imagePath;
		this.bottomMessage = bottomMessage;
		this.fontSize = fontSize;
		this.imageWidth = imageWidth;
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
			InputStream in = getClass().getResourceAsStream("/" + imagePath);
			Image image = ImageIO.read(in);
			Image resizedImage = image.getScaledInstance(imageWidth, -1, Image.SCALE_DEFAULT); // -1 to keep the proportion between height and width
			icon = new ImageIcon(resizedImage);
		}
	
		JLabel image = new JLabel(icon);
		Border blackline = BorderFactory.createLineBorder(Color.DARK_GRAY, 3);
		image.setBorder(blackline);
		image.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		JPanel imagePanel = new JPanel();
		imagePanel.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

		imagePanel.add(image);
		
		int width = icon.getIconWidth();

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(imagePanel);
		
		int height = icon.getIconHeight();
		
		if (bottomMessage != null) {
			JLabel label = new JLabel(bottomMessage);
			Font myFont = new Font("Serif", Font.ITALIC, fontSize);
			label.setFont(myFont);
			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			bottomPanel.add(label);
			FontMetrics metric = bottomPanel.getFontMetrics(label.getFont());
			int ht = metric.getHeight();
			bottomPanel.setSize(new Dimension(width, ht));
			getContentPane().add(bottomPanel);
			height += ht;
		}

		setSize(new Dimension(width, height));
		setLocationRelativeTo(parent);	// put the window in the center of the screen

		setResizable(false);
		setUndecorated(true);	// plain window
		setModal(true);			// modal window to avoid any interaction with other windows
	}
	
	
	
}




