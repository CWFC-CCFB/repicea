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

import java.io.File;
import java.net.URISyntaxException;

import repicea.app.AbstractGenericEngine;
import repicea.app.GenericTask;
import repicea.app.Logger;
import repicea.app.SettingMemory;
import repicea.console.TriggerTask.TaskID;
import repicea.gui.REpiceaShowableUI;

public abstract class Trigger extends AbstractGenericEngine implements REpiceaShowableUI {

	protected File workingDirectory;
	protected Logger logger = new Logger();
	protected MainDialog guiInterface;
	protected TriggerSettings settings;
	
	public Trigger() {
		super();
	}
	
	protected void redirectOutputStream() {
		logger.connectToSystemOutputStream();
	}
	
	
	@Override
	public MainDialog getUI() {
		if (guiInterface == null) {
			guiInterface = new MainDialog(this, logger);
		}
		return guiInterface;
	}

	@Override
	public void showUI() {
		getUI().setVisible(true);
	}
	
	protected void findCurrentLocation() throws URISyntaxException {
		String jarLocation = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		File jarFile = new File(jarLocation);
		workingDirectory = jarFile.getParentFile();
		System.out.println("Working directory is " + workingDirectory.getAbsolutePath());
	}

	protected abstract JavaProcessWrapper createProcessWrapper();
	
	protected void loadSettings() {
		setSettingMemory(new SettingMemory(getSettingMemoryFilename()));
		setSettings();
		getSettings().setLanguage(settings.getLanguage()); // make sure the property user.language is properly set
	}

	
	protected void setSettings() {settings = new TriggerSettings(this);}
	
	protected TriggerSettings getSettings() {return settings;}

	protected abstract String getSettingMemoryFilename();

	protected abstract String getName();
	
	protected abstract String getTitle();
	

	@Override
	protected void firstTasksToDo() {
		addTask(new TriggerTask(TaskID.RedirectingOutputStream, this));
		addTask(new TriggerTask(TaskID.FindCurrentLocation, this));
		addTask(new TriggerTask(TaskID.LoadSettings, this));
		addTask(new TriggerTask(TaskID.ShowInterface, this));
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	@Override
	protected void decideWhatToDoInCaseOfFailure(GenericTask task) {
		super.decideWhatToDoInCaseOfFailure(task);
		addTask(new TriggerTask(TaskID.ExpandInterface, this));
	}

	protected void startEmbeddedApplication() throws Exception {
		getSettings().recordSettings();
		JavaProcessWrapper javaProcessWrapper = createProcessWrapper();
		if (guiInterface != null) {
			guiInterface.javaProcessWrapper = javaProcessWrapper;
			logger.clear();
//			javaProcessWrapper.addPropertyChangeListener(guiInterface.javaProcessWrapper);
			javaProcessWrapper.getInternalProcess().addPropertyChangeListener(guiInterface);
		}
		javaProcessWrapper.doThisJob();
	}

	
	
}
