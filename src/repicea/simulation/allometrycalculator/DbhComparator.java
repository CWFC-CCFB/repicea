/*
 * This file is part of the repicea-foresttools library.
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
package repicea.simulation.allometrycalculator;

import java.util.Comparator;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;

/**
 * The DbhComparator class implements the Comparator interface for DbhCmProvider instances.
 * @author Mathieu Fortin - May 2014
 */
public class DbhComparator implements Comparator<DbhCmProvider> {

	private final boolean ascending;
	
	/**
	 * General constructor.
	 * @param ascending true for ascending sorting or false for descending sorting
	 */
	public DbhComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	/**
	 * Default constructor for ascending comparison.
	 */
	public DbhComparator() {
		this(true);
	}
	
	@Override
	public int compare(DbhCmProvider arg0, DbhCmProvider arg1) {
		if (ascending) {
			if (arg0.getDbhCm() < arg1.getDbhCm()) {
				return -1;
			} else if (arg0.getDbhCm() == arg1.getDbhCm()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (arg0.getDbhCm() < arg1.getDbhCm()) {
				return 1;
			} else if (arg0.getDbhCm() == arg1.getDbhCm()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

}
