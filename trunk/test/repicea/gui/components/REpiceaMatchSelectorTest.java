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

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

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
		public MyComplexObjectClass copy() {
			return new MyComplexObjectClass(name, index);
		}
		
	}
	
	@Test
	public void cancelOkTest() throws InterruptedException {
		REpiceaMatchSelector<StatusClass> selector = new REpiceaMatchSelector<StatusClass>(new String[]{"a","b","c","d","e","f"},
				StatusClass.values(), 
				-1, 
				new String[]{"string", "status"});
		REpiceaMatchSelectorDialog dlg = selector.getUI(null);
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				selector.showUI(null);
			}
		};
		Thread t = new Thread(toRun);
		t.start();
		while (!dlg.isVisible()) {}
		Thread.sleep(100);
		dlg.controlPanel.doClickCancelButton();
		while (dlg.isVisible()) {}
		Thread.sleep(100);
		
		Assert.assertEquals("Testing if the dialog has been properly cancelled", true, dlg.hasBeenCancelled());
		Assert.assertEquals("Testing if the dialog window has been shut down", true, !dlg.isVisible());
		List<MyComplexObjectClass> complexObjects = new ArrayList<MyComplexObjectClass>();
		for (StatusClass sc : StatusClass.values()) {
			complexObjects.add(new MyComplexObjectClass(sc.name(), sc.ordinal()));
		}
		
		REpiceaMatchSelector<MyComplexObjectClass> selector2 = new REpiceaMatchSelector<MyComplexObjectClass>(new String[]{"a","b","c","d","e","f"},
				complexObjects.toArray(new MyComplexObjectClass[]{}), 
				new String[]{"string", "status", "index"});
		dlg = selector2.getUI(null);
		toRun = new Runnable() {
			@Override
			public void run() {
				selector2.showUI(null);
			}
		};
		t = new Thread(toRun);
		t.start();
		while (!dlg.isVisible()) {}
		Thread.sleep(100);
		dlg.controlPanel.doClickOkButton();
		while (dlg.isVisible()) {}
		Thread.sleep(100);
		Assert.assertEquals("Testing if the dialog has been properly accepted", false, dlg.hasBeenCancelled());
		Assert.assertEquals("Testing if the dialog window has been shut down", true, !dlg.isVisible());
	}

}
