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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the layout of the SystemPanel class.
 * @author Mathieu Fortin - April 2014
 */
public class SystemLayout implements LayoutManager {

	private int height;
	private int width;
	
	protected SystemLayout() {
		height = 100;
		width = 100;
	}

	@Override
	public void addLayoutComponent(String arg0, Component arg1) {}

	@Override
	public void layoutContainer(Container container) {
		width = container.getWidth();
		height = container.getHeight();
		List<Processor> knownProcessors = new ArrayList<Processor>();
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component comp = container.getComponent(i);
			if (comp.isVisible()) {
				Dimension size = comp.getPreferredSize();
				comp.setSize(size);
				if (comp instanceof ProcessorButton) {
					ProcessorButton processUnitButton = (ProcessorButton) comp;
					Processor thisProcessor = processUnitButton.getOwner();
					Point locationOfThisButton = thisProcessor.getOriginalLocation();
					if (locationOfThisButton == null) {
						boolean hasAFather = false;
						int farY = 0;
						for (Processor fatherProcessor : knownProcessors) {
							Point fatherLocation = fatherProcessor.getOriginalLocation();	
							if (fatherLocation.y > farY) {
								farY = fatherLocation.y;
							}
							if (fatherProcessor.getSubProcessors().contains(thisProcessor)) {
								hasAFather = true;
								int index = fatherProcessor.getSubProcessors().indexOf(thisProcessor);
								Point tmpPoint = new Point(fatherLocation.x + UISetup.XGap, 
										fatherLocation.y + index * UISetup.YGap);
								boolean isIn = false;
								do {
									for (Processor knownProcessor : knownProcessors) {
										isIn = false;
										if (knownProcessor.getOriginalLocation().equals(tmpPoint)) {
											isIn = true;
											break;
										}
									}
									if (isIn) {
										tmpPoint = new Point(tmpPoint.x + UISetup.XGap, tmpPoint.y);
									}
								} while (isIn);
								locationOfThisButton = tmpPoint;
								break;
							}
						}
						if (!hasAFather) {
							locationOfThisButton = new Point(UISetup.XOrigin,  farY + UISetup.YGap);
						}
					}
					setInternalSize(locationOfThisButton);
					processUnitButton.setLocation(locationOfThisButton);
					knownProcessors.add(thisProcessor);
				}
			}
		}
	}

	
	protected void setInternalSize(Point lastPoint) {
		if (lastPoint.x > width - UISetup.XGap) {
			width = lastPoint.x + UISetup.XGap;
		}
		if (lastPoint.y > height - UISetup.YGap) {
			height = lastPoint.y + UISetup.YGap;
		}
	}
	
	@Override
	public Dimension minimumLayoutSize(Container arg0) {
		return new Dimension(width, height);
	}

	@Override
	public Dimension preferredLayoutSize(Container arg0) {
		return new Dimension(width, height);
	}

	@Override
	public void removeLayoutComponent(Component arg0) {}

}
