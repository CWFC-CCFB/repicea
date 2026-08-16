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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import repicea.app.UseModeProvider.UseMode;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaGUITestRobot;

public class REpiceaMatchWithEnumSelectorTest {


//	static class MyComplexObjectClass implements REpiceaMatchWithEnumObject<String, UseMode> {
//
//		String name;
//		int index;
//		
//		MyComplexObjectClass(String name, int index) {
//			this.name = name;
//			this.index = index;
//		}
//		
//		@Override
//		public int getNbAdditionalFields() {
//			return 1;
//		}
//
//		@Override
//		public List<Object> getAdditionalFields() {
//			List<Object> myList = new ArrayList<Object>();
//			myList.add(index);
//			return myList;
//		}
//		
//		@Override 
//		public String toString() {
//			return name;
//		}
//
//		@Override
//		public void setValueAt(int indexOfThisAdditionalField, Object value) {
//			if (indexOfThisAdditionalField == 0) {
//				this.index = (Integer) value;
//			}
//		}
//
//		@Override
//		public MyComplexObjectClass getDeepClone() {
//			return new MyComplexObjectClass(name, index);
//		}
//		
//	}

	
	static class MyREpiceaMatchWithEnum1 implements REpiceaMatchWithEnumObject<String, UseMode> {

		String name;
//		int index;
		UseMode useMode;
		
		MyREpiceaMatchWithEnum1(String name, UseMode useMode) {
			this.name = name;
			this.useMode = useMode;
		}
		
		@Override
		public int getNbAdditionalFields() {
			return 0;
		}

		@Override
		public List<Object> getAdditionalFields() {
			List<Object> myList = new ArrayList<Object>();
			return myList;
		}
		
		@Override 
		public String toString() {
			return name;
		}

		@Override
		public void setValueAt(int indexOfThisAdditionalField, Object value) {
			throw new UnsupportedOperationException("This class does not contain additional fields!");
		}

		@Override
		public MyREpiceaMatchWithEnum1 getDeepClone() {
			return new MyREpiceaMatchWithEnum1(name, useMode);
		}

		@Override
		public UseMode getValue() {return useMode;}

		@Override
		public void setValue(UseMode value) {useMode = value;}

		@Override
		public String getKey() {return name;}
		
	}

	
	
	@Test
	public void test01cancelOk() throws Exception {
		List<MyREpiceaMatchWithEnum1> myInstances = new ArrayList<MyREpiceaMatchWithEnum1>();
		for (String name : new String[]{"a","b","c","d","e","f"}) {
			myInstances.add(new MyREpiceaMatchWithEnum1(name, UseMode.ASSISTED_SCRIPT_MODE));
		}
		
		REpiceaMatchWithEnumSelector<String, UseMode> selector = new REpiceaMatchWithEnumSelector<String, UseMode>(
				myInstances.toArray(new MyREpiceaMatchWithEnum1[] {}),
				-1, 
				new String[]{"string", "usemode"});
		REpiceaMatchWithEnumSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());

		REpiceaMatchWithEnumSelector<String, UseMode> selector2 = new REpiceaMatchWithEnumSelector<String, UseMode>(
				myInstances.toArray(new MyREpiceaMatchWithEnum1[] {}),
				-1, 
				new String[]{"string", "usemode"});

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

	
	
	static class MyREpiceaMatchWithEnum2 implements REpiceaMatchWithEnumObject<String, UseMode> {

		String name;
		int index;
		UseMode useMode;
		
		MyREpiceaMatchWithEnum2(String name, UseMode useMode, int index) {
			this.name = name;
			this.useMode = useMode;
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
		public MyREpiceaMatchWithEnum2 getDeepClone() {
			return new MyREpiceaMatchWithEnum2(name, useMode, index);
		}

		@Override
		public UseMode getValue() {return useMode;}

		@Override
		public void setValue(UseMode value) {useMode = value;}

		@Override
		public String getKey() {return name;}
		
	}

	
	public static REpiceaMatchWithEnumSelector<String, UseMode> produceSelectorWithREpiceaMatchWithEnum2Class() {
		List<MyREpiceaMatchWithEnum2> myInstances = new ArrayList<MyREpiceaMatchWithEnum2>();
		for (String name : new String[]{"a","b","c","d","e","f"}) {
			myInstances.add(new MyREpiceaMatchWithEnum2(name, UseMode.ASSISTED_SCRIPT_MODE, 1));
		}
		
		REpiceaMatchWithEnumSelector<String, UseMode> selector = new REpiceaMatchWithEnumSelector<String, UseMode>(
				myInstances.toArray(new MyREpiceaMatchWithEnum2[] {}),
				-1, 
				new String[]{"string", "usemode", "index"});
		return selector;
	}
	
	@Test
	public void test02ChangeValueThenOk() throws Exception {
		REpiceaMatchWithEnumSelector<String, UseMode> selector = produceSelectorWithREpiceaMatchWithEnum2Class();
		REpiceaMatchWithEnumSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory).getModel();
		model.setValueAt(UseMode.GUI_MODE, 1, 1);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		MyREpiceaMatchWithEnum2 match = (MyREpiceaMatchWithEnum2) selector.getMatch(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory, "b");
		
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.GUI_MODE);
		
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				try { 
					robot.clickThisButton("Ok");
				} catch (Exception e) {}
			}
		};
		
		robot.startGUI(toRun, JDialog.class);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		robot.clickThisButton("No", REpiceaAWTProperty.WindowsJustSetToInvisible);

		dlg.dispose();
		t.join();
		robot.shutdown();
		Assert.assertTrue("Testing if the dialog has been properly accepted", !dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.GUI_MODE);

		System.out.println("Test changeValueTestThenOk successfully carried out!");
	}

	@Test
	public void test03ChangeValueThenCancel() throws Exception {
		REpiceaMatchWithEnumSelector<String, UseMode> selector = produceSelectorWithREpiceaMatchWithEnum2Class();
		REpiceaMatchWithEnumSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory).getModel();
		model.setValueAt(UseMode.PURE_SCRIPT_MODE, 1, 1);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		MyREpiceaMatchWithEnum2 match = (MyREpiceaMatchWithEnum2) selector.getMatch(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory, "b");
		
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.PURE_SCRIPT_MODE);
		
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		
		boolean b = SwingUtilities.isEventDispatchThread();
		System.out.println("Is dispatch thread = " + b);

		REpiceaGUITestRobot.letDispatchThreadProcess();
		robot.shutdown();
		
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.PURE_SCRIPT_MODE);
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());
		System.out.println("Test changeValueTestThenCancel successfully carried out!");
	}

	
	@Test
	public void test04ChangeValueTwiceThenCancel() throws Exception {
		REpiceaMatchWithEnumSelector<String, UseMode> selector = produceSelectorWithREpiceaMatchWithEnum2Class();
		REpiceaMatchWithEnumSelectorDialog dlg = selector.getUI(null);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.showWindow(selector);
		REpiceaTableModel model = (REpiceaTableModel) dlg.getTable(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory).getModel();
		model.setValueAt(UseMode.GUI_MODE, 1, 1);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		MyREpiceaMatchWithEnum2 match = (MyREpiceaMatchWithEnum2) selector.getMatch(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory, "b");
		
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.GUI_MODE);

		model.setValueAt(UseMode.ASSISTED_SCRIPT_MODE, 2, 1);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		match = (MyREpiceaMatchWithEnum2) selector.getMatch(REpiceaMatchWithEnumSelector.DefaultSingleCategory.SingleCategory, "c");
		
		Assert.assertEquals("Testing the match", match.getValue(), UseMode.ASSISTED_SCRIPT_MODE);
		
		model.setValueAt(3, 2, 2);
		Assert.assertEquals("Testing the index", match.getAdditionalFields().get(0), 3);
		
		robot.clickThisButton("Cancel", REpiceaAWTProperty.WindowsJustSetToInvisible);
		dlg.dispose();
		t.join();
		boolean b = SwingUtilities.isEventDispatchThread();
		System.out.println("Is dispatch thread = " + b);
		REpiceaGUITestRobot.letDispatchThreadProcess();
		robot.shutdown();
		
		Assert.assertTrue("Testing if the dialog has been properly cancelled", dlg.hasBeenCancelled());
		Assert.assertTrue("Testing if the dialog window has been shut down", !dlg.isVisible());

		System.out.println("Test changeValueTestThenCancel successfully carried out!");
	}
}
