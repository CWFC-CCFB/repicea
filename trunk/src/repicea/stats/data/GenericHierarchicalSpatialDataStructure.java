/*
 * This file is part of the repicea-statistics library.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import repicea.math.Matrix;

/**
 * This class is the basic class for a hierarchical and spatial data structure.
 * @author Mathieu Fortin - October 2011
 */
public class GenericHierarchicalSpatialDataStructure extends GenericHierarchicalStatisticalDataStructure implements HierarchicalSpatialDataStructure {

	protected Map<Integer, Map<Integer, Double>> distanceMap;
	protected List<String> distanceFields;
	protected boolean distanceCalculated;
	
	protected Map<Integer, Map<Integer, Matrix>> angleMap;
	protected boolean angleCalculated;
		
	/**
	 * General constructor. To be implemented in derived class.
	 * @param dataSet a DataSet instance
	 */
	public GenericHierarchicalSpatialDataStructure(DataSet dataSet) {
		super(dataSet);
		distanceMap = new HashMap<Integer, Map<Integer, Double>>();
		angleMap = new HashMap<Integer, Map<Integer, Matrix>>();
		distanceFields = new ArrayList<String>();
	}

	@Override
	public void setDistanceFields(List<String> fields) {
		distanceFields= fields;
	}

	@Override
	public Map<Integer, Map<Integer, Double>> getDistancesBetweenObservations() {
		if (distanceCalculated) {
			return distanceMap;
 		} else {
 			if (isThereAnyHierarchicalStructure() && !distanceFields.isEmpty()) {
 				distanceMap.clear();
 				Map<String, DataBlock> hierarchicalStructure = getHierarchicalStructure();
 				for (String levelID : hierarchicalStructure.keySet()) {
 					List<Integer> observationIndex = hierarchicalStructure.get(levelID).getIndices();
 					if (observationIndex != null && !observationIndex.isEmpty()) {
 						int nbObs = observationIndex.size();
 						TreeMap<Integer, Double> tmpMap;
 						int indexA;
 						int indexB;
 						for (int i = 0; i < nbObs; i++) {
 							indexA = observationIndex.get(i);
 							tmpMap = new TreeMap<Integer, Double>();
 							distanceMap.put(indexA, tmpMap);
 							for (int j = 0; j < nbObs; j++) {
 								indexB = observationIndex.get(j);
 								double distance = getDistanceBetweenObservations(indexA, indexB);
 								tmpMap.put(indexB, distance);
 							}
 						}
 					}
 				}
 				distanceCalculated = true;
 				return distanceMap;
 			} else {
 				return null;
 			}
 		}
	}

	
	@Override
	public Map<Integer, Map<Integer, Matrix>> getAngleBetweenObservations() {
		if (angleCalculated) {
			return angleMap;
 		} else {
 			if (isThereAnyHierarchicalStructure() && !distanceFields.isEmpty()) {
 				angleMap.clear();
 				Map<String, DataBlock> hierarchicalStructure = getHierarchicalStructure();
 				for (String levelID : hierarchicalStructure.keySet()) {
 					List<Integer> observationIndex = hierarchicalStructure.get(levelID).getIndices();
 					if (observationIndex != null && !observationIndex.isEmpty()) {
 						int nbObs = observationIndex.size();
 						TreeMap<Integer, Matrix> tmpMap;
 						int indexA;
 						int indexB;
 						for (int i = 0; i < nbObs; i++) {
 							indexA = observationIndex.get(i);
 							tmpMap = new TreeMap<Integer, Matrix>();
 							angleMap.put(indexA, tmpMap);
 							for (int j = 0; j < nbObs; j++) {
 								indexB = observationIndex.get(j);
 								Matrix angles = getAnglesBetweenObservations(indexA, indexB);
 								tmpMap.put(indexB, angles);
 							}
 						}
 					}
 				}
 				angleCalculated = true;
 				return angleMap;
 			} else {
 				return null;
 			}
 		}
	}

	
	
	
	protected Matrix getAnglesBetweenObservations(int indexA, int indexB) {
		int nbDimensions = distanceFields.size();
		String fieldName;
		double diff;
		Object valueA;
		Object valueB;

		List<Double> listDiff = new ArrayList<Double>();
		
		for (int i = 0; i < nbDimensions; i++) {
			fieldName = distanceFields.get(i);
			int indexOfThisField = dataSet.getIndexOfThisField(fieldName);
			valueA = dataSet.getValueAt(indexA, indexOfThisField);
			valueB = dataSet.getValueAt(indexB, indexOfThisField);
			diff = (Double) valueA - (Double) valueB;
			listDiff.add(Math.abs(diff));
		}

		double angleRadians;
		Matrix outputMatrix = new Matrix(nbDimensions,nbDimensions);
		for (int i = 0; i < nbDimensions - 1; i++) {
			for (int j = i + 1; j < nbDimensions; j++) {
				angleRadians = Math.atan(listDiff.get(j) / listDiff.get(i));
				outputMatrix.m_afData[i][j] = angleRadians;
				outputMatrix.m_afData[j][i] = angleRadians;
			}
		}
		
		return outputMatrix;
	}

	/**
	 * This method computes the Euclidian distance between two observations regardless of the dimensions. 
	 * @param indexA the index of the first observation
	 * @param indexB the index of the second observation
	 * @return a double the Euclidian distance
	 */
	protected double getDistanceBetweenObservations(int indexA, int indexB) {
		int nbDimensions = distanceFields.size();
		double ssDifferences = 0;
		String fieldName;
		double diff;
		Object valueA;
		Object valueB;
		
		for (int i = 0; i < nbDimensions; i++) {
			fieldName = distanceFields.get(i);
			int indexOfThisField = dataSet.getIndexOfThisField(fieldName);
			valueA = dataSet.getValueAt(indexA, indexOfThisField);
			valueB = dataSet.getValueAt(indexB, indexOfThisField);
			diff = (Double) valueA - (Double) valueB;
			ssDifferences += diff * diff;
		}
		
		return Math.sqrt(ssDifferences);
	}
	
}
