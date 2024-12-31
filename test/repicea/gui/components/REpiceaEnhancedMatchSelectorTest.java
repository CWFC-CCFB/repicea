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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.app.UseModeProvider.UseMode;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaGUITestRobot;
import repicea.util.ObjectUtility;
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
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof MyComplexObjectClass) {
				MyComplexObjectClass oo = (MyComplexObjectClass) o;
				if (this.index == oo.index) {
					if (this.name.equals(oo.name)) {
						return true;
					}
				}
			} 
			return false;
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
		REpiceaGUITestRobot.letDispatchThreadProcess();
		
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());

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
		Assert.assertTrue("Testing if the dialog has been properly accepted", !dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		System.out.println("Test cancelOkTest successfully carried out!");
	}

	@Test
	public void changeValueThenOkTest() throws Exception {
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
		REpiceaGUITestRobot.letDispatchThreadProcess();
		
		dlg.tabbedPane.setSelectedIndex(1);
		robot.letDispatchThreadProcess();
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(Language.French).getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		MyComplexObjectClass match = selector.matchMaps.get(Language.French).get("b");
		
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		robot.clickThisButton("Ok", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		robot.shutdown();
		
		Assert.assertTrue("Testing if the dialog has been properly accepted", !dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		
		match = selector.matchMaps.get(Language.French).get("b");
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		System.out.println("Test changeValueThenOk successfully carried out!");
	}

	@Test
	public void changeValueThenCancelTest() throws Exception {
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
		REpiceaGUITestRobot.letDispatchThreadProcess();
		
		dlg.tabbedPane.setSelectedIndex(1);
		robot.letDispatchThreadProcess();
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(Language.French).getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		MyComplexObjectClass match = selector.matchMaps.get(Language.French).get("b");
		
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		robot.shutdown();
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		
		match = selector.matchMaps.get(Language.French).get("b");
		Assert.assertEquals("Testing the match", UseMode.PURE_SCRIPT_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.PURE_SCRIPT_MODE.ordinal(), match.index);

		System.out.println("Test changeValueThenCancel successfully carried out!");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveAndLoadTest() throws IOException {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaEnhancedMatchSelector<MyComplexObjectClass> selector = new REpiceaEnhancedMatchSelector<MyComplexObjectClass>(Arrays.asList(Language.values()),
				new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});

		String filename = ObjectUtility.getPackagePath(getClass()) + "testSavingEnhancedSelector.zml";
		
		selector.save(filename);
		
		REpiceaEnhancedMatchSelector<MyComplexObjectClass> myNewSelector = (REpiceaEnhancedMatchSelector) REpiceaEnhancedMatchSelector.Load(filename);
		
		Assert.assertEquals("Testing column names", selector.columnNames.length, myNewSelector.columnNames.length);
		for (int i = 0; i < selector.columnNames.length; i++) {
			Assert.assertEquals("Testing entry " + i, 
					selector.columnNames[i].toString(), 
					myNewSelector.columnNames[i].toString());
		}
		Assert.assertEquals("Testing matchMap sizes", selector.matchMaps.size(), myNewSelector.matchMaps.size());
		for (Enum<?> key : selector.matchMaps.keySet()) {
			Map<Object, MyComplexObjectClass> expectedInnerMap = selector.matchMaps.get(key);
			Map<Object, MyComplexObjectClass> actualInnerMap = myNewSelector.matchMaps.get(key);
			Assert.assertEquals("Testing inner matchMap sizes", expectedInnerMap.size(), actualInnerMap.size());
			for (Object key2 : expectedInnerMap.keySet()) {
				Assert.assertEquals("Testing entries of inner matchMap instances", 
						expectedInnerMap.get(key2), 
						actualInnerMap.get(key2));
			}
		}

		Assert.assertEquals("Testing potentialMatchMap sizes", selector.potentialMatchesMap.size(), myNewSelector.potentialMatchesMap.size());
		for (Enum<?> key : selector.matchMaps.keySet()) {
			List<MyComplexObjectClass> expectedInnerMap = selector.potentialMatchesMap.get(key);
			List<MyComplexObjectClass> actualInnerMap = myNewSelector.potentialMatchesMap.get(key);
			Assert.assertEquals("Testing inner matchMap sizes", expectedInnerMap.size(), actualInnerMap.size());
			for (int i = 0; i < expectedInnerMap.size(); i++) {
				Assert.assertEquals("Testing entries of inner potentialMatchMap instances", 
						expectedInnerMap.get(i), 
						actualInnerMap.get(i));
			}
		}
	}
	
}
