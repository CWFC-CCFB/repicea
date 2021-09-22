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
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;

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
//	Matrix y;
//	Matrix modelVariance;
//	Matrix samplingVariance;
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
			DataSet dataset, 
			List<String> outputTypes) {
		this.nbRealizations = nbRealizations;
		this.nbPlots = nbPlots;
		this.dataset = dataset;
		this.outputTypes = outputTypes;
		List<Integer> sortingIndex = new ArrayList<Integer>();
		sortingIndex.add(getDataSet().getFieldNames().indexOf(OutputTypeFieldName));
		sortingIndex.add(getDataSet().getFieldNames().indexOf(DateYrFieldName));
		getDataSet().sortObservations(sortingIndex);	// the dataset is sorted according to the output type and then the date yr
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
	
	public List<String> getOutputTypes() {return this.outputTypes;}


	/**
	 * Sort the dataset and retrieve the variances from it.
	 * @return a Matrix
	 */
	protected Matrix getTotalVariance() {
		// TODO the outputType must be set somewhere before calling this method here MF20210922.
//		Matrix formerTotalVariance = this.samplingVariance.add(modelVariance);
		int varianceIndex = getDataSet().getFieldNames().indexOf(TotalVarianceFieldName);
		int nbObs = getDataSet().getNumberOfObservations();
		Matrix mat = new Matrix(nbObs, nbObs);
		for (int i = 0; i < nbObs; i++) {
			mat.setValueAt(i, i, (Double) getDataSet().getObservations().get(i).toArray()[varianceIndex]);
		}
		return mat;
	}
	

	public boolean isCompatible(ScriptResult result) {
		if (dataset.getFieldNames().equals(result.dataset.getFieldNames())) {
			if (dataset.getFieldTypes().equals(this.dataset.getFieldTypes())) {
				if (outputTypes.equals(result.outputTypes)) {
					return true;
				}
			}
		}
		return false;
	}

}
