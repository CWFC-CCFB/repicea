/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.disturbances;

/**
 * An empty class that contains the parameter id for REpiceaBinaryEventPredictor derived
 * class emulating natural or human-made disturbances.
 * @author Mathieu Fortin - January 2021
 */
public final class DisturbanceParameter {
	
	public final static int ParmCurrentDateYr = 1;
	public final static int ParmDisturbanceOccurrences = 2;
	public final static int ParmSimpleRecurrenceBasedParameters = 3;
	public final static int ParmTreatment = 4;
	public final static int ParmAAC = 5;
	public final static int ParmYear0 = 6;
	public final static int ParmYear1 = 7;
	public final static int ParmModulation = 8;


}
