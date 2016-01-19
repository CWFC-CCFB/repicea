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
package repicea.net.server;

import java.io.IOException;
import java.util.List;

import repicea.net.RemoteEventManager;
import repicea.net.RemoteListener;
import repicea.net.SocketWrapper;
import repicea.net.server.gui.ClientThreadPanel;
import repicea.net.server.gui.ServerDialog;

public class ServerSideRemoteEventConnector extends RemoteEventManager {

	private AbstractServer server;

	protected ServerSideRemoteEventConnector(SocketWrapper socket, AbstractServer server) throws IOException {
		super(socket);
		this.server = server;
	}


	@Override
	public void connectRemoteListeners() throws Exception {
		List<RemoteListener> listeners = getRemoteListeners();	// waiting for the interface to send its listeners
		int i = 0;
		for (RemoteListener listener : listeners) {
			if (listener.isTheOwnerOfThisClass(ClientThreadPanel.class)) {
				ClientThread thread = server.getClientThreads().get(i);
				connectListenerAndObject(thread, listener);
				thread.fireCurrentStatus();
				i++;
			} else if (listener.isTheOwnerOfThisClass(ServerDialog.class)) {
				connectListenerAndObject(server, listener);
			}
		}
		
		server.firePropertyChange("connected", null, null);

		System.out.println("Interface connected to server");
		waitForCloseCall();
	}

}
