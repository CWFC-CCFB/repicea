/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * The REpiceaWindowHandler ensures the cancelAction() method is called in any REpiceaFrame or 
 * REpiceaDialog instance when the "x" in the upper window is clicked.
 * @author Mathieu Fortin - April 2014
 */
class REpiceaWindowHandler {
	
	protected class InternalWindowAdapter implements WindowListener {
		/**
		 * Called when the window is closed using the right corner x.
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			if (e.getSource() instanceof REpiceaWindow) {
				REpiceaWindow window = (REpiceaWindow) e.getSource();
				if (window.isCancelOnClose()) {
					window.cancelAction();
				} else {
					window.okAction();
				}
			} 
		}

		@Override
		public void windowActivated(WindowEvent e) {}

		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowDeactivated(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowOpened(WindowEvent e) {}
	}
	
	
	private final InternalWindowAdapter windowAdapter;
	
	protected REpiceaWindowHandler(Window window) {
		windowAdapter = new InternalWindowAdapter();
		window.addWindowListener(windowAdapter);
	}
	
}
