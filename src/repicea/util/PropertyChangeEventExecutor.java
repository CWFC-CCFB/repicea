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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class handles a combination of listener:event. It finds the matching method in the listener for
 * the event object. If the object belongs to a component
 * @author mfortin
 *
 */
public class PropertyChangeEventExecutor extends AbstractEventExecutor<PropertyChangeListener, PropertyChangeEvent> {
	
	protected PropertyChangeEventExecutor(PropertyChangeListener listener, PropertyChangeEvent event) {
		super(listener, event);
	}
	
	@Override
	public void run() {
		listener.propertyChange(event);
	}

}
