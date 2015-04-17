/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import java.awt.Window;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
public class WBirchProdVolTreeLoggerParametersDialog extends TreeLoggerParametersDialog<WBirchProdVolTreeLogCategory> {

	static {
		UIControlManager.setTitle(WBirchProdVolTreeLoggerParametersDialog.class, "White Birch Tree Logger", "Module de billonnage du Bouleau blanc");
	}

	protected WBirchProdVolTreeLoggerParametersDialog(Window owner, WBirchProdVolTreeLoggerParameters params) {
		super(owner, params);
		logGradePriorityChangeEnabled = false; 
	}


	@Override
	protected String getTreeLoggerName() {
		return "White Birch Tree Logger";
	}

	@Override
	protected void initUI() {
		super.initUI();
		mnFile.setEnabled(false);
		mnEdit.setEnabled(false);
		mnSpecies.setEnabled(false);			//	the species cannot be changed in SybilleTreeLogger
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}


	@Override
	protected void settingsAction() {} // useless for this class


}
