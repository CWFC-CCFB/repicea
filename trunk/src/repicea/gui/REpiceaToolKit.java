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
package repicea.gui;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The UIToolKit class provides some static methods that are useful to interact with the User Interface.
 * @author Mathieu Fortin - May 2014
 */
public class REpiceaToolKit implements AWTEventListener {

	public static interface WindowTrackerListener {
		public void receiveThisWindow(Window retrievedWindow);
	}
	
	public static interface REpiceaEventListener {
		public void receiveREpiceaEvent(REpiceaAWTEvent e);
	}

	private static final REpiceaToolKit UIToolKit = new REpiceaToolKit();
	
	private final List<WindowTrackerListener> windowListeners = new CopyOnWriteArrayList<WindowTrackerListener>();
	private final List<REpiceaEventListener> repiceaListeners = new CopyOnWriteArrayList<REpiceaEventListener>();
	
	/**
	 * 
	 * @param windowClass the class of the window to expect
	 */
	private REpiceaToolKit() {
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
	}

	public static REpiceaToolKit getInstance() {
		return UIToolKit;
	}
	
   /**
     * Invoked when an event is dispatched in the AWT.
     */
	@Override
    public void eventDispatched(AWTEvent event) {
    	if (event instanceof WindowEvent) {
    		WindowEvent we = (WindowEvent) event;
    		if (we.getID() == WindowEvent.WINDOW_OPENED || we.getID() == WindowEvent.WINDOW_CLOSING) {
    			if (we.getSource() instanceof Window) {
        			for (WindowTrackerListener listener : windowListeners) {
        				listener.receiveThisWindow((Window) we.getSource()); 
        			}
    			}
    		} 
    	}
    }

	public void rEpiceaEventDispatched(REpiceaAWTEvent e) {
		for (REpiceaEventListener listener : repiceaListeners) {
			listener.receiveREpiceaEvent(e);
		}
	}

    public static void addWindowTrackerListener(WindowTrackerListener listener) {
    	if (!UIToolKit.windowListeners.contains(listener)) {
    		UIToolKit.windowListeners.add(listener);
    	}
    }
    
    public static void removeWindowTrackerListener(WindowTrackerListener listener) {
    	if (UIToolKit.windowListeners.contains(listener)) {
    		UIToolKit.windowListeners.remove(listener);
    	}
    }

    
    public static void addREpiceaEventListener(REpiceaEventListener listener) {
    	if (!UIToolKit.repiceaListeners.contains(listener)) {
    		UIToolKit.repiceaListeners.add(listener);
    	}
    }
    
    public static void removeREpiceaEventListener(REpiceaEventListener listener) {
    	if (UIToolKit.repiceaListeners.contains(listener)) {
    		UIToolKit.repiceaListeners.remove(listener);
    	}
    }

}
