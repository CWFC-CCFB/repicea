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
package repicea.app;

import java.io.Serializable;

import javax.swing.SwingWorker;

/**
 * This class is the basic class for all the GenericTask classes. It extends the SwingWorker class. The
 * result of the task as whether or not it was correclty terminated is set during the done() method.
 * NOTE: The super methods get(), and cancel(boolean) are not recommended. The user should use the methods
 * isCorrectlyTerminated() and cancel() instead.
 * @author Mathieu Fortin - November 2011
 */
public abstract class AbstractGenericTask extends SwingWorker<Boolean, Object> implements GenericTask, Serializable {

	private static final long serialVersionUID = 20111219L;
	
	private boolean correctlyTerminated;
	private Exception failureReason;
	private String name;

	private boolean isOver = false; 
	private final Object lock = new Object();
	
	/**
	 * Empty constructor.
	 */
	protected AbstractGenericTask() {}

	@Override
	public boolean isCorrectlyTerminated() {
		while (!isOver) {
			synchronized(lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {}
			}
		}
		return correctlyTerminated;
	}
	
	/**
	 * This methods sets the correctlyTerminated member.
	 * @param correctlyTerminated a boolean
	 */
	protected void setCorrectlyTerminated(boolean correctlyTerminated) {this.correctlyTerminated = correctlyTerminated;}

	@Override
	public Exception getFailureReason() {return failureReason;}
	
	/**
	 * This method sets the failureReason member. 
	 * @param failureReason an Exception instance
	 */
	protected void setFailureReason(Exception failureReason) {this.failureReason = failureReason;}

	/**
	 * This method sets the name of the task.
	 * @param name a String
	 */
	protected void setName(String name) {this.name = name;}
	
	@Override
	public String getName() {return name;}
	
	@Override
	public String toString() {
		if (getName() != null && !getName().isEmpty()) {
			return getName();
		} else {
			return super.toString();
		}
	}
	
	@Override
	public void cancel() {
		if (!isCancelled()) {
			cancel(true);
		}
	}
	
	
	/**
	 * Compared to the super class, the doInBackground() no longer throws exceptions.
	 * The exceptions are rather catched and registred as failure reasons.
	 * @return a Boolean true if the task ended correctly or false otherwise
	 */
	@Override
	protected final Boolean doInBackground() {
		try {
			if (getName() != null && !getName().isEmpty()) {
				Thread.currentThread().setName(getName());
			}
			doThisJob();
			return true;
		} catch (Exception e) {
			setFailureReason(e);
			return false;
		}
	}
	
	/**
	 * This method is the inner part of the doInBackground method. 
	 * It should be defined in derived classes.
	 * @throws Exception if an error has occurred
	 */
	protected abstract void doThisJob() throws Exception;

	@Override
	protected final void done() {
		try {
			setCorrectlyTerminated(get());
		} catch (Exception e) {
			setCorrectlyTerminated(false);
			setFailureReason(e);
		} 
		synchronized(lock) {
			isOver = true;
			lock.notifyAll();
		}
	}
	
	@Override
	public boolean isVerbose() {return false;}

}
