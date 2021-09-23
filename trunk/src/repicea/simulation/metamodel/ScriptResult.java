/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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

package repicea.simulation.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.Observation;

/**
 * Handle the result of a simulation performed through the ExtScript class
 * @author Mathieu Fortin - January 2021
 */
public class ScriptResult {

	public static final String DateYrFieldName = "DateYr";
	public static final String NbRealizationsFieldName = "NbRealizations";
	public static final String TimeSinceInitialDateYrFieldName = "timeSinceInitialDateYr";
	public static final String OutputTypeFieldName = "OutputType";
	public static final String EstimateFieldName = "Estimate";
	public static final String TotalVarianceFieldName = "TotalVariance";
	
	final DataSet dataset;
	final List<String> outputTypes;
	final int nbRealizations;
	final int nbPlots;
	
	/**
	 * Constructor.
	 * @param nbRealizations
	 * @param dataset
	 * @param outputTypes
	 */
	public ScriptResult(int nbRealizations, 
			int nbPlots,
			DataSet dataset) {
		this.nbRealizations = nbRealizations;
		this.nbPlots = nbPlots;
		this.dataset = dataset;
		
		List<Integer> sortingIndex = new ArrayList<Integer>();
		int outputTypeFieldNameIndex = getDataSet().getFieldNames().indexOf(OutputTypeFieldName);
		sortingIndex.add(outputTypeFieldNameIndex);
		sortingIndex.add(getDataSet().getFieldNames().indexOf(DateYrFieldName));
		getDataSet().sortObservations(sortingIndex);	// the dataset is sorted according to the output type and then the date yr
		
		outputTypes = new ArrayList<String>();
		for (Observation o : getDataSet().getObservations()) {
			String outputType =  o.toArray()[outputTypeFieldNameIndex].toString();
			if (!outputTypes.contains(outputType)) {
				outputTypes.add(outputType);
			}
		}
		Collections.sort(outputTypes);
	}

	/**
	 * Return an empty data set already formatted with the appropriate field names.
	 * @return a DataSet instance
	 */
	public static DataSet createEmptyDataSet() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add(DateYrFieldName);
		fieldNames.add(NbRealizationsFieldName);
		fieldNames.add(TimeSinceInitialDateYrFieldName);
		fieldNames.add(OutputTypeFieldName);
		fieldNames.add(EstimateFieldName);
		fieldNames.add(TotalVarianceFieldName);
		DataSet outputDataSet = new DataSet(fieldNames);
		return outputDataSet;
	}
	
	public int getNbRealizations() {return nbRealizations;}
	
	public int getNbPlots() {return nbPlots;} 
	
	public DataSet getDataSet() {return dataset;}
	
	public List<String> getOutputTypes() {return outputTypes;}


	/**
	 * Sort the dataset and retrieve the variances from it.
	 * @return a Matrix
	 */
	protected Matrix getTotalVariance(String outputType) {
		int varianceIndex = getDataSet().getFieldNames().indexOf(TotalVarianceFieldName);
		int outputFieldTypeIndex = getDataSet().getFieldNames().indexOf(OutputTypeFieldName);
		
		List<Observation> selectedObservations = new ArrayList<Observation>();
		for (Observation o : getDataSet().getObservations()) {
			if (o.toArray()[outputFieldTypeIndex].equals(outputType)) {
				selectedObservations.add(o);
			}
		}
		
		int nbObs = selectedObservations.size();
		Matrix mat = new Matrix(nbObs, nbObs);
		for (int i = 0; i < nbObs; i++) {
			mat.setValueAt(i, i, (Double) selectedObservations.get(i).toArray()[varianceIndex]);
		}
		return mat;
	}
	
	/**
	 * Check the compatibility between two ScriptResult instances. To be compatible,
	 * the DataSet instances nested in the ScriptResult instances must share the 
	 * same field names and the same field types. Moreover the ScriptResult instances 
	 * must have the same output types. 
	 * @param result a ScriptResult instance
	 * @return a boolean
	 */
	protected boolean isCompatible(ScriptResult result) {
		if (dataset.getFieldNames().equals(result.dataset.getFieldNames())) {
			if (dataset.getFieldTypes().equals(result.dataset.getFieldTypes())) {
				if (outputTypes.equals(result.outputTypes)) {
					return true;
				}
			}
		}
		return false;
	}

}
