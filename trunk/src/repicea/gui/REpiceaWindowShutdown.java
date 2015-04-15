/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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

class REpiceaWindowShutdown implements PropertyChangeListener {

	private final REpiceaWindow owner;
	
	protected REpiceaWindowShutdown(REpiceaWindow window) {
		this.owner = window;
		((Window) owner).addPropertyChangeListener(this);
	}
	
		
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getSource().equals(owner)) {
			if (arg0.getPropertyName().equals(REpiceaAWTProperty.WindowAcceptedConfirmed.name())) {
				owner.setVisible(false);
			} else if (arg0.getPropertyName().equals(REpiceaAWTProperty.WindowCancelledConfirmed.name())) {
				owner.setVisible(false);
			} else if (arg0.getPropertyName().equals(REpiceaAWTProperty.DisconnectAutoShutdown.name())) {
				finalize();
			}
		}
	}

	@Override
	public void finalize() {
		((Window) owner).removePropertyChangeListener(this);
	}
}
