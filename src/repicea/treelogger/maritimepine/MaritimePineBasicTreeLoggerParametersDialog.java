/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.treelogger.maritimepine;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaSlider;
import repicea.gui.components.REpiceaSlider.Position;
import repicea.gui.components.REpiceaSliderGroup;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;


@SuppressWarnings("serial")
class MaritimePineBasicTreeLoggerParametersDialog extends TreeLoggerParametersDialog<MaritimePineBasicTreeLogCategory> implements PropertyChangeListener {

	static {
		UIControlManager.setTitle(MaritimePineBasicTreeLoggerParametersDialog.class, 
				"Maritime Pine Basic Tree Logger", "Module de billonnage de base pour le pin maritime");
	}

	protected REpiceaSlider shortLivedSlider;
	protected REpiceaSlider longLivedSlider;
	
	
	protected MaritimePineBasicTreeLoggerParametersDialog(Window window, MaritimePineBasicTreeLoggerParameters params) {
		super(window, params);
	}
	
	@Override
	protected void instantiateVariables(TreeLoggerParameters<MaritimePineBasicTreeLogCategory> params) {
		super.instantiateVariables(params);
		shortLivedSlider = new REpiceaSlider(Position.South);
		shortLivedSlider.setEnabled(getTreeLoggerParameters().getGUIPermission().isEnablingGranted());
		longLivedSlider = new REpiceaSlider(Position.South);
		longLivedSlider.setEnabled(getTreeLoggerParameters().getGUIPermission().isEnablingGranted());
		REpiceaSliderGroup sliderGroup = new REpiceaSliderGroup(100);
		sliderGroup.add(shortLivedSlider);
		sliderGroup.add(longLivedSlider);
	}

	@Override
	protected String getTreeLoggerName() {
		return getClass().getSimpleName();
	}


	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.treelogger.TreeLoggerParametersDialog#settingsAction()
	 */
	@Override
	protected void settingsAction() {}

	
	private JPanel createWoodProductTab() {
		JPanel mainPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		mainPanel.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1d;
		c.fill = GridBagConstraints.BOTH;
		makePanel(mainPanel, FlowLayout.LEFT, UIControlManager.getLabel(MaritimePineBasicTreeLoggerParameters.MessageID.ProductType), c, layout);
		makePanel(mainPanel, FlowLayout.CENTER, UIControlManager.getLabel(MaritimePineBasicTreeLoggerParameters.MessageID.ShortLived), c, layout);
		c.gridwidth = GridBagConstraints.REMAINDER;
		makePanel(mainPanel, FlowLayout.CENTER, UIControlManager.getLabel(MaritimePineBasicTreeLoggerParameters.MessageID.LongLived), c, layout);

		c.gridwidth = 1;
		makePanel(mainPanel, FlowLayout.LEFT, UIControlManager.getLabel(MaritimePineBasicTreeLoggerParameters.MessageID.Proportion), c, layout);
		makePanel(mainPanel, FlowLayout.CENTER, shortLivedSlider, c, layout);
		c.gridwidth = GridBagConstraints.REMAINDER;
		makePanel(mainPanel, FlowLayout.CENTER, longLivedSlider, c, layout);
		
//		c.gridwidth = 1;
//		makePanel(mainPanel, FlowLayout.LEFT, UIControlManager.getLabel(MessageID.AverageLifeTime), c, layout);
//		makePanel(mainPanel, FlowLayout.CENTER, shortLivedAverageLifetime, c, layout);
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		makePanel(mainPanel, FlowLayout.CENTER, longLivedAverageLifetime, c, layout);

		return mainPanel;
	}

	private void makePanel(JComponent parentComponent, int align, JComponent comp, GridBagConstraints c, GridBagLayout layout) {
		JPanel panel = new JPanel(new FlowLayout(align));
		panel.add(comp);
		layout.setConstraints(panel, c);
		parentComponent.add(panel);
	}

	@Override
	protected void initUI() {
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(getControlPanel(), BorderLayout.SOUTH);

		getContentPane().add(createWoodProductTab(), BorderLayout.CENTER);

		setMenuBar();
		mnLogGrade.setEnabled(false);
		mnSpecies.setEnabled(false);
		mnTools.setEnabled(false);

		pack();

		Dimension minDim = new Dimension(600, 350);
		setMinimumSize(minDim);
		setSize(minDim);

		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		shortLivedSlider.addPropertyChangeListener(this);
		longLivedSlider.addPropertyChangeListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		shortLivedSlider.removePropertyChangeListener(this);
		longLivedSlider.removePropertyChangeListener(this);
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(shortLivedSlider)) {
			MaritimePineBasicTreeLogCategory logCategory = params.getLogCategory(MaritimePineBasicTree.PINE, 
					MaritimePineBasicTreeLoggerParameters.MessageID.ShortLived.toString());
			logCategory.setVolumeProportion(((Integer) evt.getNewValue()) * .01);
		} else if (evt.getSource().equals(longLivedSlider)) {
			MaritimePineBasicTreeLogCategory logCategory = params.getLogCategory(MaritimePineBasicTree.PINE, 
					MaritimePineBasicTreeLoggerParameters.MessageID.LongLived.toString());
			logCategory.setVolumeProportion(((Integer) evt.getNewValue()) * .01);
		}
	}

	@Override
	public void synchronizeUIWithOwner() {
		super.synchronizeUIWithOwner();
		MaritimePineBasicTreeLogCategory logCategory = params.getLogCategory(MaritimePineBasicTree.PINE, 
				MaritimePineBasicTreeLoggerParameters.MessageID.ShortLived.toString());
		shortLivedSlider.setValue((int) (logCategory.getVolumeProportion() * 100));
		logCategory = params.getLogCategory(MaritimePineBasicTree.PINE, 
				MaritimePineBasicTreeLoggerParameters.MessageID.LongLived.toString());
		longLivedSlider.setValue((int) (logCategory.getVolumeProportion() * 100));
	}

}
