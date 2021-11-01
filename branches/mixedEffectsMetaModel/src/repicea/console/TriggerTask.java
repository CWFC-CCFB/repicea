/*
 * This file is part of the repicea-console library.
 *
 * Copyright (C) 2012 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.console;

import javax.swing.JFrame;

import repicea.app.AbstractGenericTask;

@SuppressWarnings("serial")
class TriggerTask extends AbstractGenericTask {

	protected static enum TaskID {
		RedirectingOutputStream,
		FindCurrentLocation,
		LoadSettings,
		ShowInterface,
		ReduceInterface,
		ExpandInterface,
		StartEmbeddedApplication
	}

	TaskID taskID;
	Trigger trigger;
	
	TriggerTask(TaskID taskID, Trigger trigger) {
		super();
		this.taskID = taskID;
		this.trigger = trigger;
	}
	
	@Override
	public String getName() {return taskID.name();}
	
	
	@Override
	public void doThisJob() throws Exception {
		MainDialog gui;
		switch(taskID) {
		case RedirectingOutputStream:
			trigger.redirectOutputStream();
			break;
		case FindCurrentLocation:
			trigger.findCurrentLocation();
			break;
		case LoadSettings:
			trigger.loadSettings();
			break;
		case ShowInterface:
			trigger.showUI();
			break;
		case ReduceInterface:
			gui = trigger.getUI();
			if (gui.isVisible()) {
				gui.setExtendedState(JFrame.ICONIFIED);
			}
			break;
		case ExpandInterface:
			gui = trigger.getUI();
			if (gui.getExtendedState() == JFrame.ICONIFIED) {
				gui.setExtendedState(JFrame.NORMAL);
			}
			break;
		case StartEmbeddedApplication:
			trigger.startEmbeddedApplication();
			break;
		}
		
	}


}
