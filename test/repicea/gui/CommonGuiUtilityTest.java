/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2023 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;

import java.awt.Window;

import javax.swing.JDialog;

import org.junit.Assert;
import org.junit.Test;


public class CommonGuiUtilityTest {

	@Test
	public void changeValueTest() throws Exception {
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Runnable doRun = new Runnable() {
			@Override
			public void run() {
				CommonGuiUtility.showErrorMessage("Test", null);
			}
		};
		robot.startGUI(doRun, JDialog.class);
		
		robot.clickThisButton("OK");
		robot.letDispatchThreadProcess();
		Window lastVisibleWindow = robot.getLastVisibleWindow();
		Assert.assertTrue("Testing that there is no visible window left", lastVisibleWindow == null);
	}

	
	
}
