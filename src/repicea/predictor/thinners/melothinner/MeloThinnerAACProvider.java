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
package repicea.predictor.thinners.melothinner;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class MeloThinnerAACProvider {

	private static MeloThinnerAACProvider Instance;

	private final Map<Integer, Map<String, Map<Integer, Double>>> aacMap; // region : land ownership : year : aac/ha

	private int minimumYearDate;
	private int maximumYearDate;
	
	private MeloThinnerAACProvider() {
		aacMap = new HashMap<Integer, Map<String, Map<Integer, Double>>>();
		init();
	}
	
	private void init() {
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "dataAAC.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				int region = Integer.parseInt(record[0].toString());
				if (!aacMap.containsKey(region)) {
					aacMap.put(region, new HashMap<String, Map<Integer, Double>>());
				}
				Map<String, Map<Integer, Double>> innerMap1 = aacMap.get(region);

				String ownership = record[2].toString();
				if (!innerMap1.containsKey(ownership)) {
					innerMap1.put(ownership, new HashMap<Integer, Double>());
				}
				Map<Integer, Double> innerMap2 = innerMap1.get(ownership);
				
				int year = Integer.parseInt(record[1].toString());
				if (minimumYearDate == 0 || year < minimumYearDate) {
					minimumYearDate = year;
				}
				if (maximumYearDate == 0 || year > maximumYearDate) {
					maximumYearDate = year;
				}
				double aac = Double.parseDouble(record[5].toString());
				innerMap2.put(year, aac);
			}
		} catch (Exception e) {
			System.out.println("Unable to load the aac table in MeloThinnerAACProvider!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		

	}
	
	/**
	 * This method returns the singleton instance of this class.
	 * @return the only MeloThinnerAACProvider instance 
	 */
	public static MeloThinnerAACProvider getInstance() {
		if (Instance == null) {
			Instance = new MeloThinnerAACProvider();
		}
		return Instance;
	}

	/**
	 * This method returns the array of aac for particular years. If the year is smaller than 1988, it is assumed that the
	 * AAC is that of 1988. If the year is larger than 2014, it assumated that the AAC is that of 2014.
	 * @param regionCode the code of the region from 1 to 10
	 * @param ownership PU for public or PR for private
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @return an array of double
	 */
	public double[] getAACValues(int regionCode, String ownership, int startingYear, int endingYear) {
		if (endingYear <= startingYear) {
			throw new InvalidParameterException("The ending year must be greater than the starting year!");
		}
		int length = endingYear - startingYear;
		double[] aacArray = new double[length];
		for (int yearIndex = 0; yearIndex < aacArray.length; yearIndex++) {
			int yearDate = startingYear + yearIndex + 1;
			if (yearDate < minimumYearDate) {
				yearDate = minimumYearDate;
			}
			if (yearDate > maximumYearDate) {
				yearDate = maximumYearDate;
			}
			aacArray[yearIndex] = aacMap.get(regionCode).get(ownership).get(yearDate);
		}
		return aacArray;
	}

	/**
	 * This method returns the array of aac for particular years on public lands. If the year is smaller than 1988, it is assumed that the
	 * AAC is that of 1988. If the year is larger than 2014, it assumated that the AAC is that of 2014.
	 * @param regionCode the code of the region from 1 to 10
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @return an array of double
	 */
	public double[] getAACValues(int regionCode, int startingYear, int endingYear) {
		return getAACValues(regionCode, "PU", startingYear, endingYear);
	}

//	public static void main(String[] args) {
//		double[] aacValue = getInstance().getAACValues(5, "PU", 2012, 2018);
//		int u = 0;
//	}
	
	
}
