/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge Epicea.
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
package repicea.net.server.gui;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;

import repicea.net.RemoteEventManager;
import repicea.net.RemoteListener;
import repicea.net.server.AbstractServer;
import repicea.net.server.ClientThread;

class InterfaceSideRemoteEventConnector extends RemoteEventManager {

	private ServerInterfaceEngine engine;
	
	protected InterfaceSideRemoteEventConnector(ServerInterfaceEngine engine) {
		super(engine.getSocket());
		this.engine = engine;
	}


	@Override
	public void connectRemoteListeners() throws Exception {
		List<EventListener> localListeners = new ArrayList<EventListener>();
		List<RemoteListener> remoteListeners = getRemoteListeners();
		for (RemoteListener listener : remoteListeners) {
			if (listener.isTheOwnerOfThisClass(ClientThread.class)) {
				ClientThreadPanel panel = engine.getUI().registerNewClientThreadPanel();
				JButton button = panel.getRestartButton();
				connectListenerAndObject(button, listener);
				localListeners.add(panel);
			} else if (listener.isTheOwnerOfThisClass(AbstractServer.class)) {
				connectListenerAndObject(engine.getUI(), listener);
			}
		}
		localListeners.add(engine.getUI());
		registerListeners(localListeners);
	}

	
}
