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

import java.awt.BorderLayout;
import java.security.InvalidParameterException;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class REpiceaSliderGroupTest {

	private final static int WAIT_TIME = 1000;

	@Ignore
	@Test
	public void testTotal() throws InterruptedException {
		REpiceaSliderGroup sliderGroup = new REpiceaSliderGroup(100);
		REpiceaSlider slider1 = new REpiceaSlider();
		slider1.setValue(60);
		slider1.setName("slider1");
		sliderGroup.add(slider1);
		REpiceaSlider slider2 = new REpiceaSlider();
		slider2.setValue(60);
		slider2.setName("slider2");
		sliderGroup.add(slider2);

		REpiceaSlider slider3 = new REpiceaSlider();
		slider2.setValue(60);
		slider2.setName("slider3");
		try {
			sliderGroup.add(slider3);
			Assert.fail("Adding a third slider should have thrown an exception!");
		} catch (InvalidParameterException e) {}

		JFrame w = new JFrame();
		w.getContentPane().setLayout(new BorderLayout());
		w.getContentPane().add(slider1, BorderLayout.NORTH);
		w.getContentPane().add(slider2, BorderLayout.SOUTH);
		w.pack();
		w.setVisible(true);
		Thread.sleep(WAIT_TIME);  // leave some time for the DispatchThread to process the event
		Assert.assertEquals("Testing the value of slider no 1", 40, slider1.getValue());
		Assert.assertEquals("Testing label of slider no 1", "40 %", slider1.getLabelString());
		Assert.assertEquals("Testing the value of slider no 2", 60, slider2.getValue());
		Assert.assertEquals("Testing label of slider no 2", "60 %", slider2.getLabelString());
		
		slider1.setValue(45);
		Thread.sleep(WAIT_TIME); // leave some time for the DispatchThread to process the event
		Assert.assertEquals("Testing the value of slider no 1", 45, slider1.getValue());
		Assert.assertEquals("Testing label of slider no 1", "45 %", slider1.getLabelString());
		Assert.assertEquals("Testing the value of slider no 2", 55, slider2.getValue());
		Assert.assertEquals("Testing label of slider no 2", "55 %", slider2.getLabelString());
		
		slider2.setValue(40);
		Thread.sleep(WAIT_TIME); // leave some time for the DispatchThread to process the event
		Assert.assertEquals("Testing the value of slider no 1", 60, slider1.getValue());
		Assert.assertEquals("Testing label of slider no 1", "60 %", slider1.getLabelString());
		Assert.assertEquals("Testing the value of slider no 2", 40, slider2.getValue());
		Assert.assertEquals("Testing label of slider no 2", "40 %", slider2.getLabelString());
		w.setVisible(false);
		w.dispose();
	}
	
	
}
