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
package repicea.gui.icons;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import repicea.gui.CommonGuiUtility;
import repicea.gui.UIControlManager.CommonControlID;

/**
 * The IconFactory class handles the icon loading and keeps the icons in memory to avoid double loading.
 * @author Mathieu Fortin - February 2012
 */
public class IconFactory {
	
	
	private static Map<CommonControlID, Icon> iconMap = new HashMap<CommonControlID, Icon>();
	

	/**
	 * This method returns the icon specified through the parameter.
	 * @param iconID an IconID enum variable that represent the icon 
	 * @return an Icon instance or null if the icon was not found
	 */
	public static Icon getIcon(CommonControlID iconID) {
		if (iconMap.containsKey(iconID)) {
			return iconMap.get(iconID);
		} else {
			if (iconID.getIconFilename() == null) {
				iconMap.put(iconID, null);
				return null;
			} else {
				return CommonGuiUtility.retrieveIcon(IconFactory.class, iconID.getIconFilename());
//				ImageIcon iconImage = null;
//				try {
//					String path = ObjectUtility.getRelativePackagePath(IconFactory.class);
//					String iconFilename = path + iconID.getIconFilename();
//					InputStream in = ClassLoader.getSystemResourceAsStream(iconFilename);
//					Image image = ImageIO.read(in);
//					iconImage = new ImageIcon(image);
//					iconMap.put(iconID, iconImage);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				return iconImage;
			}
		}
	}

}
