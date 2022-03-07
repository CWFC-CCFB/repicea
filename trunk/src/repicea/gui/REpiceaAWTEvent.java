/*
 * This file is part of the repicea library.
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

import java.awt.AWTEvent;

import javax.swing.SwingUtilities;

/**
 * The {@code REpiceaAWTEvent} class is used to send events to REPiceaUIToolKit class. <br>
 * <br>
 * These events are used to test the behaviour of the UI.
 * @author Mathieu Fortin - March 2022
 */
@SuppressWarnings("serial")
public class REpiceaAWTEvent extends AWTEvent {
	
	public static int REPICEA_EVENT_MASK = 2024;

	protected final REpiceaAWTProperty property;
	
	/**
	 * Constructor.
	 * @param source the source of the event
	 * @param message a message 
	 */
	public REpiceaAWTEvent(REpiceaAWTProperty property) {
		super(REpiceaToolKit.getInstance(), REPICEA_EVENT_MASK);
		this.property = property;
	}
	
	/**
	 * Constructor.
	 * @param source the source of the event
	 * @param message a message 
	 */
	public REpiceaAWTEvent(Object source, REpiceaAWTProperty property) {
		super(source, REPICEA_EVENT_MASK);
		this.property = property;
	}
	
	
	/**
	 * Send the {@code REpiceaAWTEvent} instance of the event dispatch thread (EDT).
	 * @param e a {@code REpiceaAWTEvent} instance
	 */
	public static void fireEvent(REpiceaAWTEvent e) {
		Runnable doRun = new Runnable() {
			public void run() {
				REpiceaToolKit.getInstance().rEpiceaEventDispatched(e);
			}
		};
		SwingUtilities.invokeLater(doRun);
	}

}
