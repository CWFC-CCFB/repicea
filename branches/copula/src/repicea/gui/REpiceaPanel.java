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

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * The REpiceaPanel implements the AncestorListener interface. As soon as the panel
 * is shown the method listenTo is called. When the panel is no onger visible, the
 * method doNotListenToAnymore is called. This ensures consistency among the listeners.
 * @author Mathieu Fortin - June 2012
 */
@SuppressWarnings("serial")
public abstract class REpiceaPanel extends JPanel implements AncestorListener, 
															SynchronizedListening,
															Refreshable {

	/**
	 * General constructor.
	 */
	public REpiceaPanel() {
		super();
		addAncestorListener(this);
	}
	
	@Override
	public abstract void listenTo();

	@Override
	public abstract void doNotListenToAnymore();


	@Override
	public void ancestorAdded(AncestorEvent event) {		// as soon as the component shows up the listeners are enabled
		refreshInterface();
		listenTo();
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {}		// no need for this one

	@Override
	public void ancestorRemoved(AncestorEvent event) {
		doNotListenToAnymore();							// as soon as the component is hidden the listeners are disabled
	}

}
