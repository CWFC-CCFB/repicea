/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import repicea.app.UseModeProvider.UseMode;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaGUITestRobot;
import repicea.util.ObjectUtility;

public class REpiceaMatchSelectorTest {


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
	public void test01cancelOk() throws Exception {
		REpiceaMatchSelector<UseMode> selector = new REpiceaMatchSelector<UseMode>(new String[]{"a","b","c","d","e","f"},
				UseMode.values(), 
				-1, 
				new String[]{"string", "usemode"});
		REpiceaMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());

		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaMatchSelector<MyComplexObjectClass> selector2 = new REpiceaMatchSelector<MyComplexObjectClass>(new String[]{"a","b","c","d","e","f"},
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
	public void test02ChangeValueThenOk() throws Exception {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaMatchSelector<MyComplexObjectClass> selector = new REpiceaMatchSelector<MyComplexObjectClass>(new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		REpiceaMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable().getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		MyComplexObjectClass match = selector.matchMap.get("b");
		
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		robot.clickThisButton("Ok", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		robot.shutdown();
		Assert.assertTrue("Testing if the dialog has been properly accepted", !dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);

		System.out.println("Test changeValueTestThenOk successfully carried out!");
	}

	@Test
	public void test03ChangeValueThenCancel() throws Exception {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaMatchSelector<MyComplexObjectClass> selector = new REpiceaMatchSelector<MyComplexObjectClass>(new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		REpiceaMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		robot.letDispatchThreadProcess();  // to make sure the reference package has the time to be stored in memory
		
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable().getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		
		MyComplexObjectClass match = selector.matchMap.get("b");
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);
		
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		boolean b = SwingUtilities.isEventDispatchThread();
		System.out.println("Is dispatch thread = " + b);

		robot.letDispatchThreadProcess();
		robot.shutdown();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		match = selector.matchMap.get("b");
		Assert.assertEquals("Testing the match", UseMode.PURE_SCRIPT_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.PURE_SCRIPT_MODE.ordinal(), match.index);
		System.out.println("Test changeValueTestThenCancel successfully carried out!");
	}

	@Test
	public void test04ChangeValueTwiceThenCancel() throws Exception {
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (UseMode sc : UseMode.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaMatchSelector<MyComplexObjectClass> selector = new REpiceaMatchSelector<MyComplexObjectClass>(new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		REpiceaMatchSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		robot.letDispatchThreadProcess();  // to make sure the reference package has the time to be stored in memory

		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable().getModel();
		model.setValueAt(complexObjects.get(0), 1, 1);
		robot.letDispatchThreadProcess();
		MyComplexObjectClass match = selector.matchMap.get("b");
		
		Assert.assertEquals("Testing the match", UseMode.GUI_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.GUI_MODE.ordinal(), match.index);

		model.setValueAt(complexObjects.get(1), 2, 1);
		robot.letDispatchThreadProcess();
		match = selector.matchMap.get("c");
		
		Assert.assertEquals("Testing the match", UseMode.ASSISTED_SCRIPT_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.ASSISTED_SCRIPT_MODE.ordinal(), match.index);

				robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		boolean b = SwingUtilities.isEventDispatchThread();
		System.out.println("Is dispatch thread = " + b);
		robot.letDispatchThreadProcess();
		robot.shutdown();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		match = selector.matchMap.get("b");
		Assert.assertEquals("Testing the match", UseMode.PURE_SCRIPT_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.PURE_SCRIPT_MODE.ordinal(), match.index);
		match = selector.matchMap.get("c");
		Assert.assertEquals("Testing the match", UseMode.PURE_SCRIPT_MODE.name(), match.name);
		Assert.assertEquals("Testing the match index", UseMode.PURE_SCRIPT_MODE.ordinal(), match.index);

		System.out.println("Test changeValueTestThenCancel successfully carried out!");
	}

	@Test
	public void test05Load() throws IOException {
		String[] firstColumn = new String[]{"g","b","c","h","e","f"};
		REpiceaMatchSelector<UseMode> selector = new REpiceaMatchSelector<UseMode>(firstColumn,
				UseMode.values(), 
				-1, 
				new String[]{"string", "usemode"});
		String filename = ObjectUtility.getRelativePackagePath(REpiceaMatchSelectorTest.class) + "testMatch.xml";
		selector.load(filename);
		Assert.assertEquals("Check map size", firstColumn.length, selector.matchMap.size());
		for (String k : firstColumn) {
			Assert.assertTrue("Check if match map contains key", selector.matchMap.containsKey(k));
		}
	}
}
