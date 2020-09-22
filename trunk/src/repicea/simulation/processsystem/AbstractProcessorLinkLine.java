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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;

@SuppressWarnings("serial")
public abstract class AbstractProcessorLinkLine extends AbstractPermissionProviderButton {

	protected static enum AnchorPosition {
		TOP,
		RIGHT,
		LEFT,
		BOTTOM;
	}
	
	protected final SystemPanel panel;
	private final AnchorProvider fatherAnchor;
	private AnchorProvider sonAnchor;
	
	private static final int BezierFactor = 50;

	
	private AnchorPosition fatherAnchorPosition;
	private AnchorPosition sonAnchorPosition;
	
	protected AbstractProcessorLinkLine(SystemPanel panel, AnchorProvider fatherAnchor, AnchorProvider sonAnchor) {
		super(panel.getListManager().getGUIPermission());
		this.panel = panel;
		this.fatherAnchor = fatherAnchor;
		setSonAnchor(sonAnchor);
		setAnchorPositions(AnchorPosition.RIGHT, AnchorPosition.LEFT);
	}

	protected void setAnchorPositions(AnchorPosition fatherAnchorPosition, AnchorPosition sonAnchorPosition) {
		this.fatherAnchorPosition = fatherAnchorPosition;
		this.sonAnchorPosition = sonAnchorPosition;
	}

	private final Point getAnchor(AnchorProvider provider, AnchorPosition position) {
		switch(position) {
		case RIGHT:
			return provider.getRightAnchor();
		case LEFT:
			return provider.getLeftAnchor();
		case TOP:
			return provider.getTopAnchor();
		case BOTTOM:
			return provider.getBottomAnchor();
		}
		return null;
	}
	
	protected void setSonAnchor(AnchorProvider sonAnchor) {this.sonAnchor = sonAnchor;}
	
	
	protected void setStroke(Graphics2D g2) {
		if (isSelected()) {
			g2.setStroke(UISetup.BoldStroke);
		} else {
			g2.setStroke(UISetup.DefaultStroke);
		}
	}
	
	protected void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Point fatherLocation = getAnchor(fatherAnchor, fatherAnchorPosition);
		Point sonLocation = getAnchor(sonAnchor, sonAnchorPosition);
		if (sonLocation != null) {
			int midX = (int) ((fatherLocation.x + sonLocation.x) * .5);
			int midY = (int) ((fatherLocation.y + sonLocation.y) * .5);
			setLocation(midX - getSize().width / 2, midY - getSize().height / 2);
			setStroke(g2);
			int controlX1 = midX;
			int controlX2 = midX;
			int controlY1 = fatherLocation.y;
			int controlY2 = sonLocation.y;
			if (sonLocation.x - fatherLocation.x < SystemLayout.convertToRelative(BezierFactor)) {
				controlX1 = fatherLocation.x + SystemLayout.convertToRelative(BezierFactor);
				controlX2 = sonLocation.x - SystemLayout.convertToRelative(BezierFactor);
				int offset = SystemLayout.convertToRelative(BezierFactor);
				if (fatherLocation.y > sonLocation.y) {
					offset = -SystemLayout.convertToRelative(BezierFactor);
				}
				controlY1 = fatherLocation.y + offset;
				controlY2 = sonLocation.y - offset;
			}
			Path2D.Double curve = new Path2D.Double();
			curve.moveTo(fatherLocation.x, fatherLocation.y);
			curve.curveTo(controlX1, controlY1,
					controlX2, controlY2,
					sonLocation.x, sonLocation.y);
			g2.draw(curve);
			g2.setStroke(UISetup.DefaultStroke);		// to the default value

		}
	}

	protected AnchorProvider getFatherAnchor() {return fatherAnchor;}
	protected AnchorProvider getSonAnchor() {return sonAnchor;}
	
	protected boolean contains(Object button) {
		return getFatherAnchor().equals(button) || getSonAnchor().equals(button);
	}
	
	@Override
	protected void finalize() {}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractProcessorLinkLine) {
			AbstractProcessorLinkLine thatLinkLine = (AbstractProcessorLinkLine) obj;
			if (getFatherAnchor().equals(thatLinkLine.getFatherAnchor())) {
				if (getSonAnchor().equals(thatLinkLine.getSonAnchor())) {
					return true;
				}
			}
		}
		return false;
	}

}
