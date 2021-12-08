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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This ActivityMonitor class monitors the activity of the ClientThread instances. If any thread remains inactive for a long period of time, the connection is closed.
 * @author Mathieu Fortin - October 2011
 */
class ActivityMonitor implements PropertyChangeListener {

	/**
	 * Subclass CheckTask is a timer that checks the activity repeatedly.
	 */
	private class CheckTask extends TimerTask  {
		
	    private ActivityMonitor activityMonitor;
	    
		private CheckTask(ActivityMonitor activityMonitor) {
	    	this.activityMonitor = activityMonitor;
	    }
		
		public void run () {
			activityMonitor.checkActivity();
	    }
		
	}

	
	private AbstractServer server;
	private Map<ClientThread, Long> lastTimeActivityMap;
	private int numberOfSecondsBetweenChecks;
	protected Timer timer;

	
	protected ActivityMonitor(AbstractServer server, int numberOfSecondsBetweenChecks) {
		this.server = server;
		this.numberOfSecondsBetweenChecks = numberOfSecondsBetweenChecks;
		timer = new Timer();
		lastTimeActivityMap = new HashMap<ClientThread, Long>();
		for (ClientThread thread : server.getClientThreads()) {
			thread.addPropertyChangeListener(this);
			lastTimeActivityMap.put(thread, -1l);
		}
		CheckTask task = new CheckTask(this);
		timer.scheduleAtFixedRate(task, 0, numberOfSecondsBetweenChecks * 1000);
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof ClientThread) {
			ClientThread thread = (ClientThread) evt.getSource();
			if (server.getClientThreads().contains(thread)) {
				if (evt.getPropertyName().equals("status")) {
					if (evt.getNewValue().equals("Connected") || evt.getNewValue().equals("Interrupted") || evt.getNewValue().equals("Processing request")) {
						recordTime(thread, System.currentTimeMillis());
					} else if (evt.getNewValue().equals("Waiting")) {
						recordTime(thread, -1L);
					}
				}
			}
		}
	}
	
	
	private void recordTime(ClientThread thread, long time) {
		synchronized(lastTimeActivityMap) {
			lastTimeActivityMap.put(thread, time);
		}
	}

	private void checkActivity() {
		Long elapsedTime;
		Long currentTime = System.currentTimeMillis();
		Collection<ClientThread> connectionsToBeClosed = new ArrayList<ClientThread>();
		synchronized(lastTimeActivityMap) {
			for (ClientThread thread : lastTimeActivityMap.keySet()) {
				if (lastTimeActivityMap.get(thread) != -1L) {
					elapsedTime = currentTime - lastTimeActivityMap.get(thread);
					if (elapsedTime > numberOfSecondsBetweenChecks * 1000) {
						connectionsToBeClosed.add(thread);
					}
				}
			}
		}
		if (!connectionsToBeClosed.isEmpty()) {
			server.closeAndRestartTheseThreads(connectionsToBeClosed);
		}
	}
	
	
}
