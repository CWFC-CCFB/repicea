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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The REpiceaPopupListener shows its REpiceaPopupMenu when invoked.
 * @author Mathieu Fortin - January 2015
 */
public class REpiceaPopupListener extends MouseAdapter {

	private final REpiceaPopupMenu popupMenu;
	
	public REpiceaPopupListener(REpiceaPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (!popupMenu.isVisible()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	
	
}

