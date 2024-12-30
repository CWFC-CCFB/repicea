/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package repicea.gui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.app.UseModeProvider.UseMode;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaGUITestRobot;
import repicea.util.REpiceaTranslator.Language;

public class REpiceaEnhancedMatchSelectorTest {


	static class MyComplexObjectClass implements REpiceaMatchComplexObject<MyComplexObjectClass> {

		String name;
		int index;
		
		MyComplexObjectClass(String name, int index) {
			this.name = name;
			this.index = index;
		}
		
		@Override
		public int getNbAdditionalFields() {
			return 1;
		}

		@Override
		public List<Object> getAdditionalFields() {
			List<Object> myList = new ArrayList<Object>();
			myList.add(index);
			return myList;
		}
		
		@Override 
		public String toString() {
			return name;
		}

		@Override
		public void setValueAt(int indexOfThisAdditionalField, Object value) {
			if (indexOfThisAdditionalField == 0) {
				this.index = (Integer) value;
			}
		}

		@Override
		public MyComplexObjectClass getDeepClone() {
			return new MyComplexObjectClass(name, index);
		}
		
	}
	
	@Test
	public void cancelOkTest() throws Exception {
		REpiceaEnhancedMatchSelector<UseMode> selector = new REpiceaEnhancedMatchSelector<UseMode>(Arrays.asList(Language.values()),
				new String[]{"a","b","c","d","e","f"},
				UseMode.values(), 
				-1, 
				new String[]{"string", "usemode"});
		REpiceaEnhancedMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		
		Assert.assertEquals("Testing if the dialog has been properly cancelled", true, dlg.hasBeenCancelled());
		Assert.assertEquals("Testing if the dialog window has been shut down", true, !dlg.isVisible());

		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaEnhancedMatchSelector<MyComplexObjectClass> selector2 = new REpiceaEnhancedMatchSelector<MyComplexObjectClass>(Arrays.asList(Language.values()),
				new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "usemode", "index"});
		dlg = selector2.getUI(null);
		
		t = robot.showWindow(selector2);
		robot.clickThisButton("Ok", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		robot.shutdown();
		Assert.assertEquals("Testing if the dialog has been properly accepted", false, dlg.hasBeenCancelled());
		Assert.assertEquals("Testing if the dialog window has been shut down", true, !dlg.isVisible());
		System.out.println("Test cancelOkTest successfully carried out!");
	}

	@Test
	public void changeValueTest() throws Exception {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaEnhancedMatchSelector<MyComplexObjectClass> selector = new REpiceaEnhancedMatchSelector<MyComplexObjectClass>(Arrays.asList(Language.values()),
				new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		REpiceaEnhancedMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		dlg.tabbedPane.setSelectedIndex(1);
		robot.letDispatchThreadProcess();
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(Language.French).getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		MyComplexObjectClass match = selector.matchMap.get(Language.French).get("b");
		
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		robot.clickThisButton("Ok", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		robot.shutdown();
		Assert.assertEquals("Testing if the dialog has been properly accepted", false, dlg.hasBeenCancelled());
		Assert.assertEquals("Testing if the dialog window has been shut down", true, !dlg.isVisible());
		System.out.println("Test changeValue successfully carried out!");
	}

}
