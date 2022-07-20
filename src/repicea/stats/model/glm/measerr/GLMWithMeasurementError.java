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
package repicea.stats.model.glm.measerr;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.model.CompositeLogLikelihoodWithExplanatoryVariables;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

/**
 * A class implementing the generalized linear model with measurement
 * error on one variable.
 * @author Mathieu Fortin - July 2022
 */
public class GLMWithMeasurementError extends GeneralizedLinearModel {

	protected final GLMMeasErrorDefinition measError;
//	private LinkFunctionWithMeasError linkFunction;
	
	public GLMWithMeasurementError(DataSet dataSet, String modelDefinition, Matrix startingValues, GLMMeasErrorDefinition measError) {
		super(dataSet, Type.CLogLog, modelDefinition, null, startingValues, measError);
		measError.validate(this);
		this.measError = measError;
	}
	
	public GLMWithMeasurementError(DataSet dataSet, String modelDefinition, GLMMeasErrorDefinition measError) {
		this(dataSet, modelDefinition, null, measError);
	}

	/*
	 * For extended visibility.
	 */
	@Override
	protected StatisticalDataStructure getDataStructure() {
		return super.getDataStructure();
	}

	LinkFunction getLinkFunction() {return lf;}
	
	IndividualLogLikelihood getIndividualLogLikelihood() {return individualLLK;}
	
	Matrix getMatrixX() {return matrixX;}
	
	Matrix getVectorY() {return y;}
	
	@Override
	protected final CompositeLogLikelihoodWithExplanatoryVariables createCompleteLLK(Object addParm) {
		CompositeLogLikelihoodWithExplanatoryVariables cLL = ((GLMMeasErrorDefinition) addParm).createCompositeLikelihoodFromModel(this);
		return cLL != null ? cLL : super.createCompleteLLK(addParm);
	}
	
	@Override
	protected final StatisticalDataStructure createDataStructure(DataSet dataSet, Object addParm) {
		StatisticalDataStructure sds = ((GLMMeasErrorDefinition) addParm).createDataStructureFromDataSet(dataSet);
		return sds != null ? sds : super.createDataStructure(dataSet, addParm);
	}

	@Override
	protected LinkFunction createLinkFunction(Type linkFunctionType, Object addParm) {
		LinkFunction lf = ((GLMMeasErrorDefinition) addParm).createLinkFunction(linkFunctionType);
		return lf != null ? lf : super.createLinkFunction(linkFunctionType, addParm);
	}

	@Override 
	protected final IndividualLogLikelihood createIndividualLLK(Object addParm) {
		IndividualLogLikelihood llk = ((GLMMeasErrorDefinition) addParm).createIndividualLogLikelihoodFromModel(this);
		return llk != null ? llk : super.createIndividualLLK(addParm);
	}

}
