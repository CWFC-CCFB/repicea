/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.covariateproviders.standlevel;

import java.util.HashMap;
import java.util.Map;

public interface QuebecForestRegionProvider {

	public static enum QuebecForestRegion {
		BasStLaurentGaspesie,
		SaguenayLacSaintJean,
		Quebec,
		TroisRivieres,
		Estrie,
		Montreal,
		Outaouais,
		AbitibiTemiscamingue,
		CoteNord;
		
		private static Map<Integer, QuebecForestRegion> RegionMap;
		
		/**
		 * This method returns the QuebecForestRegion enum associated to the region code parameter
		 * @param regionCode an integer
		 * @return a QuebecForestRegion enum 
		 */
		public static QuebecForestRegion getRegion(int regionCode) {
			if (RegionMap == null) {
				RegionMap = new HashMap<Integer, QuebecForestRegion>();
				for (QuebecForestRegion region : QuebecForestRegion.values()) {
					RegionMap.put(region.getRegionCode(), region);
				}
			}
			return RegionMap.get(regionCode);
		}
		
		/**
		 * This method returns the region code. 
		 * @return an integer
		 */
		public int getRegionCode() {return this.ordinal() + 1;};
	}
	
	/**
	 * This method ensures the instance can return a QuebecForestRegion enum representing the administrative region where
	 * it is located.
	 * @return a QuebecForestRegion enum
	 */
	public QuebecForestRegion getQuebecForestRegion();
	

}
