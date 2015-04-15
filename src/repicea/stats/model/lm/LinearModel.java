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
package repicea.stats.model.lm;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.model.AbstractStatisticalModel;
import repicea.stats.optimizers.OLSOptimizer;
import repicea.stats.optimizers.Optimizer;

/**
 * The LinearModel is a traditional model fitted with an Ordinary Least Squares estimator.
 * @author Mathieu Fortin - November 2012
 */
public class LinearModel extends AbstractStatisticalModel<StatisticalDataStructure> {

	/**
	 * Constructor.
	 * @param dataSet a DataSet instance
	 * @param modelDefinition a model definition
	 */
	public LinearModel(DataSet dataSet, String modelDefinition) {
		super(dataSet);
		try {
			setModelDefinition(modelDefinition);
		} catch (StatisticalDataException e) {
			System.out.println("Unable to define this model : " + modelDefinition);
			e.printStackTrace();
		}
	}

	
	/*
	 * Useless (non-Javadoc)
	 * @see repicea.stats.model.StatisticalModel#setParameters(repicea.math.Matrix)
	 */
	@Override
	public void setParameters(Matrix beta) {}

	@Override
	public Matrix getParameters() {
		return getOptimizer().getParameters().getMean();
	}
	
	/**
	 * This method returns the residual variance only if the optimizer is an instance
	 * of OLSOptimizer.
	 * @return a Matrix with a single element
	 */
	public double getResidualVariance() {
		if (getOptimizer() instanceof OLSOptimizer) {
			return ((OLSOptimizer) getOptimizer()).getResidualVariance().getMean();
		} else {
			return -1d;
		}
	}

	@Override
	public Matrix getPredicted() {
		return getDataStructure().getMatrixX().multiply(getParameters());
	}

	@Override
	public Matrix getResiduals() {
		return getDataStructure().getVectorY().subtract(getPredicted());
	}

	/*
	 * Useless (non-Javadoc)
	 * @see repicea.stats.model.AbstractStatisticalModel#setOverallLLK()
	 */
	@Override
	protected void setOverallLLK() {}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.stats.model.AbstractStatisticalModel#instantiateDefaultOptimizer()
	 */
	@Override
	protected Optimizer instantiateDefaultOptimizer() {
		return new OLSOptimizer();
	}

	@Override
	protected GenericStatisticalDataStructure getDataStructureFromDataSet(DataSet dataSet) {
		return new GenericStatisticalDataStructure(dataSet);
	}

}
