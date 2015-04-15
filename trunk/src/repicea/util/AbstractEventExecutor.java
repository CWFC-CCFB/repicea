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
package repicea.util;

import java.awt.Component;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.SwingUtilities;

public abstract class AbstractEventExecutor<L extends EventListener, E extends EventObject> implements Runnable {

	protected L listener;
	protected E event;
	
	protected AbstractEventExecutor(L listener, E event) {
		this.listener = listener;
		this.event = event;
		if (listener instanceof Component) {
			SwingUtilities.invokeLater(this);
		} else {
			run();
		}
	}

}
