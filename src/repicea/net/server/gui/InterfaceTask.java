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

import repicea.app.AbstractGenericTask;

class InterfaceTask extends AbstractGenericTask {

	private static final long serialVersionUID = 20111226L;

	enum InterfaceRelatedTask {ShowInterface, Connect, Disconnect}
		
	private InterfaceRelatedTask task;
	private ServerInterfaceEngine engine;
	
	InterfaceTask(ServerInterfaceEngine engine, InterfaceRelatedTask task) {
		this.engine = engine;
		this.task = task;
		setName(task.toString());
	}
	
	@Override
	public void doThisJob() throws Exception {
		switch(task) {
		case ShowInterface:
			engine.showUI();
			break;
		case Connect:
			engine.connect();
			break;
		case Disconnect:
			engine.disconnect();
			break;
		}
	}
	
	

}
