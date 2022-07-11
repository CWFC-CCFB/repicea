/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.estimators;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataStructure;

public abstract class AbstractEstimator implements Estimator {

	protected StatisticalDataStructure dataStruct;

	
	@Override
	public DataSet getParameterEstimatesReport() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("Effect");
		fieldNames.add("Estimate");
		fieldNames.add("Standard Error");
		DataSet dataSet = new DataSet(fieldNames);
		Matrix parameterEstimates = getParameterEstimates().getMean();
		boolean varianceAvailable = false;
		Matrix std = null;
		if (getParameterEstimates().getVariance() != null) {
			std = getParameterEstimates().getVariance().diagonalVector().elementWisePower(0.5);
			varianceAvailable = true;
		} 

		Object[] record = new Object[3];
		boolean isWithIntercept = dataStruct.isInterceptModel();
		for (int i = 0; i < parameterEstimates.m_iRows; i++) {
			int j = isWithIntercept ? i - 1 : i;
			record[0] = j == -1 ? "intercept" : dataStruct.getEffectList().get(j);
			record[1] = parameterEstimates.getValueAt(i, 0);
			record[2] = varianceAvailable ? std.getValueAt(i, 0) : Double.NaN;
			dataSet.addObservation(record);
		}
		return dataSet;
	}

}
