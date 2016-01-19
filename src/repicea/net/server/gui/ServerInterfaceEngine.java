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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import repicea.app.AbstractGenericEngine;
import repicea.gui.ShowableObject;
import repicea.net.SocketWrapper;
import repicea.net.server.gui.InterfaceTask.InterfaceRelatedTask;

public class ServerInterfaceEngine extends AbstractGenericEngine implements ShowableObject {

	
	private ServerDialog mainDialog;
	private int communicationPort;
	private SocketWrapper socket;
	private InterfaceSideRemoteEventConnector connector;
	
	
	private ServerInterfaceEngine() {
		super();
	}
	

	@Override
	protected void decideWhatToDo(String taskName, Exception failureReason) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ServerDialog getGuiInterface() {
		if (mainDialog == null) {
			mainDialog = new ServerDialog(this);
		}
		return mainDialog;
	}

	@Override
	public void showInterface() {
		getGuiInterface().setVisible(true);
	}

	@Override
	protected void firstTasksToDo() {
		addTask(new InterfaceTask(this, InterfaceRelatedTask.ShowInterface));
	}
	
	
	protected void connect() throws IOException {
		InetAddress localAddress = InetAddress.getLocalHost();
		SocketAddress socketAddress = new InetSocketAddress(localAddress, communicationPort);
		Socket socket = new Socket();
		socket.connect(socketAddress, 5000);
		this.socket = new SocketWrapper(socket); 
		connector = new InterfaceSideRemoteEventConnector(this);
		try {
			connector.connectRemoteListeners();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Problem while connecting to the server : " + e.getMessage());
		}
	}

	protected void setCommunicationPort(int port) {this.communicationPort = port;}

	
	protected void disconnect() throws IOException {
		connector.close();
	}
	
	
	protected SocketWrapper getSocket() {return socket;}
	
	@Override
	public void requestShutdown() {
		queue.clear();
		if (getGuiInterface().isConnected()) {
			addTask(new InterfaceTask(this, InterfaceRelatedTask.Disconnect));
		}
		addTask(finalTask);
	}

	public static void main(String[] args) {
		ServerInterfaceEngine engine = new ServerInterfaceEngine();
		engine.startApplication();
	}

}
