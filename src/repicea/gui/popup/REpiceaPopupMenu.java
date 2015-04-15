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
package repicea.gui.popup;

import java.awt.event.ActionListener;
import java.security.InvalidParameterException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * The REpiceaPopupMenu provides on the fly options for GUI. It is embedded in a REpiceaPopupListener.
 * @author Mathieu Fortin - January 2015
 */
@SuppressWarnings("serial")
public class REpiceaPopupMenu extends JPopupMenu {

	public REpiceaPopupMenu(ActionListener listener, JMenuItem... items) {
		if (items == null) {
			throw new InvalidParameterException("The items argument must be non null!");
		}
		for (JMenuItem item : items) {
			if (item != null) {
				add(item);
				item.addActionListener(listener);
			}
		}
	}

}


