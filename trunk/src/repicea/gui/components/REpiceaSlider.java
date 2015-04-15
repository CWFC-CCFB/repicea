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

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;

/**
 * The REpiceaSlider class is a wrapper for a JSlider and a JLabel instance. The 
 * JLabel is automatically updated with the value of the slider. Listener instances
 * should implement the PropertyChangeListener interface. The value of the slider is returned
 * with the property name equals to REpiceaSlider.SLIDER_CHANGE.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
public class REpiceaSlider extends REpiceaPanel implements ChangeListener {
	
	public static final String SLIDER_CHANGE = "SliderChange";
	
	private String token;
	private JLabel sliderLabel;
	protected JSlider slider;
	private Position selectedPosition;
	
	public static enum Position {North, South, East, West};

	/**
	 * General constructor.
	 * @param token the string that appears after the slider value in the label (e.g. "%")
	 * @param position either Position.North, Position.South, Position.East, or Position.West 
	 * @param min the minimum value of the slider
	 * @param max the maximum value of the slider
	 */
	public REpiceaSlider(String token, Position position, int min, int max) {
		super();
		setToken(token);
		setPosition(position);
		slider = new JSlider();
		int majorSpacing = (max - min) / 10;
		slider.setMajorTickSpacing(majorSpacing);
		slider.setPaintTicks(true);
		sliderLabel = UIControlManager.getLabel("");
		sliderLabel.setAlignmentX(CENTER_ALIGNMENT);
		setMinimum(min);
		setMaximum(max);
		setValue(min + (max - min) / 2);
		createUI();
	}

	/**
	 * Constructor with token set as "%" and minimum and maximum values respectively set to
	 * 0 and 100 for the slider.
	 * @param position either Position.North, Position.South, Position.East, or Position.West 
	 */
	public REpiceaSlider(Position position) {
		this(null, position, 0, 100);
	}

	/**
	 * Constructor with token set as "%" and minimum and maximum values respectively set to
	 * 0 and 100 for the slider. The label is east of the slider.
	 */
	public REpiceaSlider() {
		this(null, null, 0, 100);
	}
	
	private void setToken(String token) {
		if (token == null) {
			this.token = "%";
		} else {
			this.token = token;
		}
	}
	
	private void setPosition(Position position) {
		if (position == null) {
			selectedPosition = Position.East;
		} else {
			selectedPosition = position;
		}
	}
	
	/**
	 * This method sets the value of the slider.
	 * @param v an integer
	 */
	public void setValue(int v) {
		slider.setValue(v);
	}
	
	/**
	 * This method returns the value of the slider.
	 * @return an integer
	 */
	public int getValue() {
		return slider.getValue();
	}

	/**
	 * This method sets the minimum value of the slider.
	 * @param arg0 an integer
	 */
	public void setMinimum(int arg0) {
		slider.setMinimum(arg0);
	}

	/**
	 * This method sets the maximum value of the slider.
	 * @param arg0 an integer
	 */
	public void setMaximum(int arg0) {
		slider.setMaximum(arg0);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		slider.setEnabled(enabled);
	}
	
	private void createUI() {
		
		Component glue;
		int boxLayoutConstant;
		if (selectedPosition == Position.West || selectedPosition == Position.East) {
			boxLayoutConstant = BoxLayout.X_AXIS;
			glue = Box.createHorizontalGlue();
		} else {
			boxLayoutConstant = BoxLayout.Y_AXIS;
			glue = Box.createVerticalStrut(10);
		} 
		setLayout(new BoxLayout(this, boxLayoutConstant));
		
		if (selectedPosition == Position.North || selectedPosition == Position.West) {
			add(sliderLabel);
			add(glue);
			add(slider);
		} else {
			add(slider);
			add(glue);
			add(sliderLabel);
		}
		
		refreshInterface();
	}
	
//
//	/**
//	 * This method adds a ChangeListener instance to the slider.
//	 * @param l a ChangeListener instance  
//	 */
//	public void addChangeListener(ChangeListener l) {
//		slider.addChangeListener(l);
//	}
//	
//	/**
//	 * This method removes a ChangeListener instance to the slider.
// 	 * @param l a ChangeListener instance 
//	 */
//	public void removeChangeListener(ChangeListener l) {
//		slider.removeChangeListener(l);
//	}
	
	@Override
	public void refreshInterface() {
		int sliderValue = slider.getValue();
		sliderLabel.setText(((Integer) sliderValue).toString() + " " + token);		
	}

	@Override
	public void listenTo() {
		slider.addChangeListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		slider.removeChangeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(slider)) {
			refreshInterface();
			firePropertyChange(SLIDER_CHANGE, 0, slider.getValue());
		}
	}
	
}
