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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import repicea.app.SettingMemory;

/**
 * The WindowSettings class handles the last loaded filename and so on for a more 
 * friendly use of the interface.
 * @author Mathieu Fortin - September 2014
 */
public final class WindowSettings extends SettingMemory implements PropertyChangeListener {
	
	public WindowSettings(String filename, Window window) {
		super(filename);
		window.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals(REpiceaAWTProperty.WindowAcceptedConfirmed.name()) || propertyName.equals(REpiceaAWTProperty.WindowCancelledConfirmed.name())) {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	
}
