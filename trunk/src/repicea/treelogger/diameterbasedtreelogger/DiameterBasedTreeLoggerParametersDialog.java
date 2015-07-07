/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.treelogger.diameterbasedtreelogger;

import java.awt.Window;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
public class DiameterBasedTreeLoggerParametersDialog extends TreeLoggerParametersDialog<DiameterBasedTreeLogCategory> {

	static {
		UIControlManager.setTitle(DiameterBasedTreeLoggerParametersDialog.class, "Tree Logger based on small-end diameter", "Module de billonnage bas\u00E9 sur le diam\u00E8tre au fin bout");
	}

	
	protected DiameterBasedTreeLoggerParametersDialog(Window window, DiameterBasedTreeLoggerParameters params) {
		super(window, params);
		logGradePriorityChangeEnabled = false; 
		mnFile.setEnabled(false);
		mnEdit.setEnabled(false);
		mnSpecies.setEnabled(false);			//	the species cannot be changed 
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}
	
	@Override
	protected void instantiateVariables(TreeLoggerParameters<DiameterBasedTreeLogCategory> params) {
		super.instantiateVariables(params);
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
	

}

