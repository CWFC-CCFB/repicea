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
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.estimators.AbstractEstimator.EstimatorCompatibleModel;

public abstract class AbstractEstimator<P extends EstimatorCompatibleModel> implements Estimator {

	
	public interface EstimatorCompatibleModel {
		
		public boolean isInterceptModel();
		
		public List<String> getEffectList();
		
		public int getNumberOfObservations();

	}

	protected final P model;
	
	protected AbstractEstimator(P model) {
		this.model = model;
	}
	
	
	
	@Override
	public DataSet getParameterEstimatesReport() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("Effect");
		fieldNames.add("Estimate");
		fieldNames.add("Std. Error");
		fieldNames.add("z value");
		fieldNames.add("Pr(>|z|)");
		fieldNames.add("Significant");
		
		DataSet dataSet = new DataSet(fieldNames);
		Matrix parameterEstimates = getParameterEstimates().getMean();
		boolean varianceAvailable = false;
		Matrix std = null;
		if (getParameterEstimates().getVariance() != null) {
			std = getParameterEstimates().getVariance().diagonalVector().elementWisePower(0.5);
			varianceAvailable = true;
		} 

		Object[] record = new Object[6];
		boolean isWithIntercept = model.isInterceptModel();
		for (int i = 0; i < parameterEstimates.m_iRows; i++) {
			int j = isWithIntercept ? i - 1 : i;
			if (j < model.getEffectList().size()) {	// otherwise we might be dealing with additional parameters like shape or scale parameters
				record[0] = j == -1 ? "intercept" : model.getEffectList().get(j);
				record[1] = parameterEstimates.getValueAt(i, 0);
				record[2] = varianceAvailable ? std.getValueAt(i, 0) : Double.NaN;
				double z = varianceAvailable ? parameterEstimates.getValueAt(i, 0) / std.getValueAt(i, 0) : Double.NaN;
				record[3] = z;
				double significanceLevel = varianceAvailable ? GaussianUtility.getCumulativeProbability(Math.abs(z), true) * 2: Double.NaN;
				record[4] = significanceLevel;
				String symbol = "";
				if (!Double.isNaN(significanceLevel)) {
					if (significanceLevel <= 0.01) {
						symbol = "**";
					} else if (significanceLevel <= 0.05) {
						symbol = "*";
					} else if (significanceLevel <= 0.10) {
						symbol = ".";
					}
				}
				record[5] = symbol;
				dataSet.addObservation(record);
			}
		}
		return dataSet;
	}

}
