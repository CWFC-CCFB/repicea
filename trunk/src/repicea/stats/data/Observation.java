/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Observation implements Comparable {

	static List<Integer> comparableFields = new ArrayList<Integer>();
	
	List<Object> values;
	
	protected Observation(Object[] obj) {
		values = new ArrayList<Object>();
		values.addAll(Arrays.asList(obj));
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Object o) {
		for (Integer index : comparableFields) {
			Comparable thisValue = (Comparable) values.get(index);
			Comparable thatValue = (Comparable) ((Observation) o).values.get(index);
			int comparisonResult = thisValue.compareTo(thatValue);
			if (comparisonResult < 0) {
				return -1;
			} else if (comparisonResult > 0) {
				return 1;
			}
		}
		return 0;
	}

}
