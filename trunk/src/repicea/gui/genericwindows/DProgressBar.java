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
package repicea.gui.genericwindows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import repicea.app.AbstractGenericTask;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;


/**
 * This dialog only contains a progress bar that listen to the SwingWorker progress.
 * @author Mathieu Fortin - April 2010
 */
@SuppressWarnings("serial")
public class DProgressBar extends REpiceaDialog implements PropertyChangeListener {

	protected JProgressBar progressBar;
	private String titleString;
	private String labelString;
	@SuppressWarnings("rawtypes")
	private SwingWorker jobToRun;


	/**
	 * Constructor 1 with a Frame instance as owner.
	 * @param owner the parent frame that calls the progress bar
	 * @param titleString the title of the progress bar
	 * @param labelString the message
	 * @param jobToRun the SwingWorker instance that contains the job to be executed
	 */
	@SuppressWarnings("rawtypes")
	public DProgressBar(Window owner, String titleString, String labelString, SwingWorker jobToRun) {
		super(owner);
		init(titleString, labelString, jobToRun);
	}


	/**
	 * Constructor 2 with no owner.
	 * @param titleString the title of the progress bar
	 * @param labelString the message
	 * @param jobToRun the SwingWorker instance that contains the job to be executed
	 */
	@SuppressWarnings("rawtypes")
	public DProgressBar(String titleString, String labelString, SwingWorker jobToRun) {
		this(null, titleString, labelString, jobToRun);
	}
	
	
	@SuppressWarnings("rawtypes")
	private void init(String titleString, String labelString, SwingWorker jobToRun) {
		this.jobToRun = jobToRun;
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);

		this.titleString = titleString;
		this.labelString = labelString;
		
		initUI();
		setModal(true);
		pack();
		
		this.jobToRun.execute();
		
		setVisible(true);
	}
	

	protected void initUI() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		setLayout(new BorderLayout());
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel label = UIControlManager.getLabel(labelString);
		labelPanel.add(label);
		
		JPanel progressBarPanel = new JPanel(new BorderLayout());
		progressBarPanel.add(progressBar, BorderLayout.CENTER);
		
		mainPanel.add(labelPanel);
		mainPanel.add(progressBarPanel);
		add(mainPanel, BorderLayout.NORTH);
		setTitle(titleString);
		
		Dimension dim = new Dimension(250,100);
//		setPreferredSize(dim);
		setMinimumSize(dim);
		pack();
	}

	@Override
	public void cancelAction() {
		if (jobToRun instanceof AbstractGenericTask) {
			((AbstractGenericTask) jobToRun).cancel();
		} else {
			jobToRun.cancel(true);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("state".equals(evt.getPropertyName())) {
			if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
				setVisible(false);
				jobToRun = null;
				dispose();
			} 
        } else if ("progress".equals(evt.getPropertyName())) {
			progressBar.setValue((Integer) evt.getNewValue());
		}  
	}

	@Override
	public void listenTo() {
		jobToRun.addPropertyChangeListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		jobToRun.removePropertyChangeListener(this);
	}


}
