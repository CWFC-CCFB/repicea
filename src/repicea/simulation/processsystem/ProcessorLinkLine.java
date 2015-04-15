/*
 * This file is part of the repicea-simulation library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.processsystem;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JLabel;

import repicea.gui.ShowableObjectWithParent;
import repicea.gui.UIControlManager;

@SuppressWarnings("serial")
public class ProcessorLinkLine extends ValidProcessorLinkLine implements ShowableObjectWithParent {

	private final JLabel label;
	private transient ProcessorLinkLineSlider guiInterface;
	
	protected ProcessorLinkLine(SystemPanel panel, Processor fatherProcessor, Processor sonProcessor) {
		super(panel, fatherProcessor.getGuiInterface(panel), sonProcessor.getGuiInterface(panel));
		label = new JLabel();
		fatherProcessor.addSubProcessor(sonProcessor);
		fatherProcessor.getGuiInterface(panel).addComponentListener(this);
		sonProcessor.getGuiInterface(panel).addComponentListener(this);
		addMouseListener(new SystemComponentMouseAdapter(this));
		panel.add(label);
		setLabel();
	}
	
	@Override
	protected void draw(Graphics g) {
		if (!getFatherAnchor().getOwner().isValid()) {
			g.setColor(Color.RED);
		}
		super.draw(g);
		g.setColor(UISetup.DefaultColor);
		Point point = getLocation();
		label.setLocation(new Point(point.x + getSize().width, point.y + getSize().height));
	}
	
	@Override
	protected void finalize() {
		super.finalize();
		panel.remove(label);
		getFatherAnchor().getOwner().removeSubProcessor(getSonAnchor().getOwner());
	}
	
	protected void setLabel() {
		Processor fatherProcessor = getFatherAnchor().getOwner();
		Processor sonProcessor = getSonAnchor().getOwner();
		int intake = fatherProcessor.getSubProcessorIntakes().get(sonProcessor);
		label.setText(intake + "%");
		panel.repaint();
	}

	@Override
	public ProcessorLinkLineSlider getGuiInterface(Container container) {
		if (guiInterface == null) {
			guiInterface = new ProcessorLinkLineSlider((Window) container, this);
		}
		return guiInterface;
	}

	@Override
	public void showInterface(Window window) {
		Point referencePoint = getLocationOnScreen();
		ProcessorLinkLineSlider dlg = getGuiInterface(window);
		Point currentLocation = new Point(referencePoint.x + 20, referencePoint.y - dlg.getSize().height - 20);
		UIControlManager.setLocation(dlg, currentLocation);
		dlg.setVisible(true);
	}
	
}
