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
package repicea.stats.model.lmm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.optimizer.AbstractOptimizer.OptimizationException;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.data.DataBlock;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericHierarchicalStatisticalDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.estimators.Estimator;
import repicea.stats.estimators.Estimator.EstimatorException;
import repicea.stats.estimators.GLSEstimator;
import repicea.stats.model.AbstractStatisticalModel;
import repicea.stats.model.IndividualLikelihood;
import repicea.stats.model.lm.LinearModel;
import repicea.util.ObjectUtility;

/**
 * The LinearMixedModel class implements a linear model with random effects.
 * @author Mathieu Fortin - November 2012
 */
public class LinearMixedModel extends AbstractStatisticalModel<HierarchicalStatisticalDataStructure> {
	
	
//	protected static class ModelLogLikelihood extends AbstractStatisticalModel.ModelLogLikelihood {
//		
//		final LinearMixedModel model;
//		final InternalLogLikelihood llk;
//		
//		ModelLogLikelihood(LinearMixedModel model) {
//			this.model = model;
//			llk = new InternalLogLikelihood();
//			llk.setParameterValue(InternalLogLikelihood.ParameterID.MatrixV, model.getMatrixVFunction());
//		}
//		
//	
//		/*
//		 * Useless for this class (non-Javadoc)
//		 * @see repicea.stats.model.LogLikelihood#getLikelihoodFunction()
//		 */
//		@Override
//		public IndividualLikelihood getLikelihoodFunction() {return null;}
//
//		@Override
//		public Double getValue() {
//			double llkValue = 0;
//			Matrix residuals = model.getResiduals();
//			Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
//			for (String subject : dataBlocks.keySet()) {
//				DataBlock db = dataBlocks.get(subject);
//				model.matrixVFunction.setDataBlock(db);
//				Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.Residuals, r_i);
//				Matrix matrixX_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.MatrixX, matrixX_i);
//
//				llkValue += llk.getValue();
//			}
//			
//			// profile the likelihood
//			return -.5 * (llkValue + (model.getDataStructure().getNumberOfObservations() - model.le.getNumberOfParameters()) * Math.log(2* Math.PI));
//		}
//	
//
//		@Override
//		public Matrix getGradient() {
//			Matrix gradient = null;
//			Matrix residuals = model.getResiduals();
//			Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
//			for (String subject : dataBlocks.keySet()) {
//				DataBlock db = dataBlocks.get(subject);
//				model.matrixVFunction.setDataBlock(db);
//				Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.Residuals, r_i);
//				Matrix X_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.MatrixX, X_i);
//
//				if (gradient == null) {
//					gradient = llk.getGradient();
//				} else {
//					gradient = gradient.add(llk.getGradient());
//				}
//			}
//			return gradient.scalarMultiply(-.5);
//		}
//
//		@Override
//		public Matrix getHessian() {
//			Matrix hessian = null;
//			Matrix residuals = model.getResiduals();
//			Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
//			for (String subject : dataBlocks.keySet()) {
//				DataBlock db = dataBlocks.get(subject);
//				model.matrixVFunction.setDataBlock(db);
//				Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.Residuals, r_i);
//				Matrix X_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
//				llk.setVariableValue(InternalLogLikelihood.VariableID.MatrixX, X_i);
//
//				if (hessian == null) {
//					hessian = llk.getHessian();
//				} else {
//					hessian = hessian.add(llk.getHessian());
//				}
//			}
//			return hessian;
//		}
//	}

	
	private final LinearStatisticalExpression le;		// beta and X vector
	private final MatrixVFunction matrixVFunction;

	
	/**
	 * Constructor using a vector of 0s as starting values for the parameters
	 * @param dataSet the fitting data
	 * @param modelDefinition a String that defines the dependent variable and the effects of the model
	 * @throws StatisticalDataException 
	 */
	public LinearMixedModel(DataSet dataSet, String modelDefinition) throws StatisticalDataException {
		super(dataSet);
		setModelDefinition(modelDefinition);
		le = new LinearStatisticalExpression();

		List<AbstractVComponent> vComponents = new ArrayList<AbstractVComponent>();
		for (String level : getDataStructure().getMatrixZ().keySet()) {
			int size = getDataStructure().getMatrixZ().get(level).m_iCols;
			vComponents.add(new RandomEffectsMatrixComposition(level, getDataStructure().getMatrixZ(), size));
		}

		WithinSubjectComposition rMatrix = new WithinSubjectComposition(getDataStructure().getMatrixZ().keySet().iterator().next());
		rMatrix.setSigma2(1d);
		vComponents.add(rMatrix);
		matrixVFunction = new MatrixVFunction(vComponents);
		setCompleteLLK();
	}
	
	/**
	 * This method returns the matrix V of the linear mixed model.
	 * @return a MatrixVFunction instance
	 */
	public MatrixVFunction getMatrixVFunction() {return matrixVFunction;}
	
	/**
	 * This method evaluates the model parameters with an ordinary least squares estimator.
	 * @throws OptimizationException
	 */
	public void evaluateParametersUnderOLS() throws EstimatorException {
		String modelDefinitionWithoutRandomEffect = ObjectUtility.extractSequences(getModelDefinition(), "(", ")").get(0);
		LinearModel lm = new LinearModel(getDataStructure().getDataSet(), modelDefinitionWithoutRandomEffect);
		lm.doEstimation();
		le.setBeta(lm.getParameters());
		Matrix covParms = new Matrix(matrixVFunction.getNumberOfParameters(), 1);
		covParms.m_afData[covParms.m_iRows - 1][0] = lm.getResidualVariance();
		matrixVFunction.setParameters(covParms);
	}

	@Override
	public void setParameters(Matrix theta) {
		matrixVFunction.setParameters(theta);
	}

	/**
	 * This method returns the inverse matrix V. The inversion is performed by block following the data block scheme.
	 * @return a Matrix instance
	 */
	public Matrix getInverseMatrixV() {
		Matrix output = null;
		Map<String, DataBlock> dataBlocks = getDataStructure().getHierarchicalStructure();
		for (String subject : dataBlocks.keySet()) {
			DataBlock db = dataBlocks.get(subject);
			matrixVFunction.setDataBlock(db);
			if (output == null) {
				output = matrixVFunction.getValue().getInverseMatrix();
			} else {
				output = output.matrixDiagBlock(matrixVFunction.getValue().getInverseMatrix());
			}
		}
		return output;
	}
	
	public void setResidualVariance(double d) {
		Matrix covParms = matrixVFunction.getParameters();
		covParms.m_afData[covParms.m_iRows - 1][0] = d;
		matrixVFunction.setParameters(covParms);
	}
	
	
	@Override
	public Matrix getParameters() {
		return matrixVFunction.getParameters();
	}

	@Override
	public Matrix getPredicted() {
		return getDataStructure().getMatrixX().multiply(le.getBeta());
	}

	@Override
	public Matrix getResiduals() {
		return getDataStructure().getVectorY().subtract(getPredicted());
	}
	
	/**
	 * This method sets the fixed effects parameters.
	 * @param beta a Matrix instance
	 */
	public void setFixedEffectsParameters(Matrix beta) {
		le.setBeta(beta);
	}
	
	@Override
	protected void setCompleteLLK() {
		completeLLK = new ModelLogLikelihood(this);
	}

	@Override
	protected Estimator instantiateDefaultEstimator() {
		return new GLSEstimator();
	}


	@Override
	protected HierarchicalStatisticalDataStructure getDataStructureFromDataSet(DataSet dataSet) {
		return new GenericHierarchicalStatisticalDataStructure(dataSet);
	}

}
