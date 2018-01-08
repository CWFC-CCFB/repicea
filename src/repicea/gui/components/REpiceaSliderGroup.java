/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.gui.SynchronizedListening;

/**
 * The REpiceaSliderGroup class acts like a ButtonGroup but for REpiceaSlider instances instead 
 * of JButton instances. Only two REpiceaSlider instances are allowed. 
 * @author Mathieu Fortin - November 2012
 */
public class REpiceaSliderGroup implements ChangeListener, SynchronizedListening {

	private final List<JSlider> sliders;
	private int total;

	/**
	 * Constructor.
	 * @param total the total value of the two sliders.
	 */
	public REpiceaSliderGroup(int total) {
		this.total = total;
		sliders = new ArrayList<JSlider>();
	}
	
	/**
	 * This method adds a REpiceaSlider instance to the group.
	 * @param repiceaSlider a REpiceaSlider instance 
	 */
	public void add(REpiceaSlider repiceaSlider) {
		if (!sliders.contains(repiceaSlider.slider)) {
			sliders.add(repiceaSlider.slider);
			repiceaSlider.slider.addChangeListener(this);
		}
	}
	
	private int sumUp() {
		int sum = 0;
		for (JSlider slider : sliders) {
			sum += slider.getValue();
		}
		return sum;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		doNotListenToAnymore();
		int sum;
		int indexSourceSlider = sliders.indexOf(arg0.getSource());
		int sliderIndexToAdjust = indexSourceSlider + 1;
		while ((sum = sumUp()) != total) {
			if (sliderIndexToAdjust == sliders.size()) {
				sliderIndexToAdjust = 0;
			}
			JSlider currentSlider = sliders.get(sliderIndexToAdjust);
			int formerValue = currentSlider.getValue();
			int newValue = formerValue - (sum - total);
			if (newValue > currentSlider.getMaximum()) {
				newValue = currentSlider.getMaximum();
			} 
			if (newValue < currentSlider.getMinimum()) {
				newValue = currentSlider.getMinimum();
			}
			currentSlider.setValue(newValue);
			sliderIndexToAdjust++;
		}
		listenTo();
	}

	@Override
	public void doNotListenToAnymore() {
		for (JSlider slider : sliders) {
			slider.removeChangeListener(this);
		}
	}

	@Override
	public void listenTo() {
		for (JSlider slider : sliders) {
			slider.addChangeListener(this);
		}
	}
	
	
}
