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
package repicea.stats.model.glm.copula;


import java.util.List;

import repicea.math.Matrix;
import repicea.math.ParameterBound;
import repicea.stats.data.HierarchicalSpatialDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.model.StatisticalModel;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class contains many different copula forms.
 * @author Mathieu Fortin - June 2011
 */
@SuppressWarnings("serial")
public class CopulaLibrary {
	
	/**
	 * This interface only identifies the copulas that take into account the distance between the individual.
	 * @author Mathieu Fortin - October 2011
	 */
	static interface DistanceCopula {}
	
	/**
	 * This copula is a simple copula with one constant parameter.
	 * @author Mathieu Fortin - June 2011
	 */
	public static class SimpleCopulaExpression extends CopulaExpression {

		public SimpleCopulaExpression(double value, String hierarchicalLevelSpecifications) {
			super(hierarchicalLevelSpecifications);
			setBeta(new Matrix(1,1,value,0));
			setX(new Matrix(1,1,1d,0));
		}
		
		@Override
		public Double getValue() {return getOriginalFunction().getValue();}

		@Override
		public Matrix getGradient() {return getOriginalFunction().getGradient();}

		@Override
		public Matrix getHessian() {return getOriginalFunction().getHessian();}

		/**
		 * This method is not necessary for this copula since the parameter is constant
		 */
		@Override
		protected void setX(int indexFirstObservation, int indexSecondObservation) {}

		@Override
		protected void initialize(StatisticalModel<?> model, HierarchicalStatisticalDataStructure data) throws StatisticalDataException {
			super.initialize(model, data);
			ParameterBound bound = new ParameterBound(-1d, 1d);
			getOriginalFunction().setBounds(0, bound);
		}
		
	}
	
	
	/**
	 * This copula is a logistic copula with a single intercept parameter. Equivalent to the
	 * SimpleCopulaExpression, but under a logistic form.
	 * @author Mathieu Fortin - June 2011
	 */
	public static class SimpleLogisticCopulaExpression extends CopulaExpression {

		private LinkFunction linkFunction;
				
		public SimpleLogisticCopulaExpression(double origin, String hierarchicalLevelSpecifications) throws StatisticalDataException {
			super(hierarchicalLevelSpecifications);
			setX(new Matrix(1,1,1d,0));
			setBeta(new Matrix(1,1,origin,0));
		}
		
		@Override
		public Double getValue() {return linkFunction.getValue();}

		@Override
		public Matrix getGradient() {return linkFunction.getGradient();}

		@Override
		public Matrix getHessian() {return linkFunction.getHessian();}

		@Override
		protected void setX(int indexFirstObservation, int indexSecondObservation) {}
		
		@Override
		public int getNumberOfVariables() {return getOriginalFunction().getNumberOfVariables();}

		@Override
		protected void initialize(StatisticalModel<?> model, HierarchicalStatisticalDataStructure data) throws StatisticalDataException {
			super.initialize(model, data);
			linkFunction = new LinkFunction(Type.Logit, getOriginalFunction());
		}

	}
	
	
	/**
	 * A copula based on a link function and the euclidian distance between the two observations.
	 * @author Mathieu Fortin - June 2011
	 */
	public static class DistanceLinkFunctionCopulaExpression extends CopulaExpression implements DistanceCopula {

		protected LinkFunction linkFunction;
		protected Matrix matrixX;
		protected String distanceFieldsEnumeration;
		protected HierarchicalSpatialDataStructure data;
				
		public DistanceLinkFunctionCopulaExpression(Type linkFunctionType,	String hierarchicalLevelSpecifications, 
				String distanceFieldsEnumeration, double parameter) throws StatisticalDataException {
			super(hierarchicalLevelSpecifications);
			this.distanceFieldsEnumeration = distanceFieldsEnumeration;
			
			Matrix beta = new Matrix(1,1,parameter,0);
			setBeta(beta);
		
			linkFunction = new LinkFunction(linkFunctionType, getOriginalFunction());
		}
		
		
		@Override
		public Double getValue() {return linkFunction.getValue();}

		@Override
		public Matrix getGradient() {return linkFunction.getGradient();}

		@Override
		public Matrix getHessian() {return linkFunction.getHessian();}
		
		@Deprecated
		@Override
		public void setX(Matrix x) {}
		
		@Override
		protected void setX(int indexFirstObservation, int indexSecondObservation) {
			double distance = data.getDistancesBetweenObservations().get(indexFirstObservation).get(indexSecondObservation);
			getOriginalFunction().setVariableValue(0, distance);
		}
		
		@Override
		public int getNumberOfVariables() {return getOriginalFunction().getNumberOfVariables();}

		public void setBounds(Integer parameterName, ParameterBound bound) {
			getOriginalFunction().setBounds(parameterName, bound);
		}


		@Override
		protected void initialize(StatisticalModel<?> model, HierarchicalStatisticalDataStructure data) throws StatisticalDataException {
			super.initialize(model, data);
			List<String> distanceFieldNames = ObjectUtility.decomposeUsingToken(distanceFieldsEnumeration, "+");
			if (!(data instanceof HierarchicalSpatialDataStructure)) {
				throw new StatisticalDataException("The data are not spatialized.");
			} else {
				this.data = (HierarchicalSpatialDataStructure) data;
				this.data.setDistanceFields(distanceFieldNames);
				this.data.getDistancesBetweenObservations();
			}
		}
	
	}

	
//	/**
//	 * A copula based on a constant and a function depending on the euclidian distance and the angle between the two observations.
//	 * @author Mathieu Fortin - June 2011
//	 */
//	public static class DistanceAndAngleFunctionCopulaExpression extends CopulaExpression implements DistanceCopula {
//
//		protected Matrix matrixX;
//		protected String distanceFieldsEnumeration;
//		protected HierarchicalSpatialDataStructure data;
//		protected Matrix beta;
//		protected double distance;
//		protected double angle;		// in radians
//		protected Matrix gradient;
//		protected Matrix hessian;
//		
//		public DistanceAndAngleFunctionCopulaExpression(String hierarchicalLevelSpecifications, 
//				String distanceFieldsEnumeration,
//				double parameter1,
//				double parameter2) throws StatisticalDataException {
//			super(hierarchicalLevelSpecifications);
//			this.distanceFieldsEnumeration = distanceFieldsEnumeration;
//			Matrix beta = new Matrix(2,1,0,0);
//			beta.m_afData[0][0] = parameter1;
//			beta.m_afData[1][0] = parameter2;
//			setBeta(beta);
//			gradient = new Matrix(beta.m_iRows,1);
//			hessian = new Matrix(beta.m_iRows, beta.m_iRows);
//		}
//		
//		
//		@Override
//		public Double getValue() {
//			double sinTerm = Math.sin(angle - beta.m_afData[1][0]);
//			double innerTerm = beta.m_afData[0][0] * sinTerm * sinTerm * distance;
//			return Math.exp(innerTerm);
//		}
//
//		@Override
//		public Matrix getGradient() {
//			double sinTerm = Math.sin(angle - beta.m_afData[1][0]);
//			double innerTerm = beta.m_afData[0][0] * sinTerm * sinTerm * distance;
//			double value = Math.exp(innerTerm);
//			gradient.m_afData[0][0] = value * sinTerm * sinTerm * distance;
//			gradient.m_afData[1][0] = value * beta.m_afData[0][0] * 2 * sinTerm * distance * Math.cos(angle - beta.m_afData[1][0]) * -1;
//			return gradient;
//		}
//
//		@Override
//		public Matrix getHessian() {
//			double sinTerm = Math.sin(angle - beta.m_afData[1][0]);
//			double cosTerm = Math.cos(angle - beta.m_afData[1][0]);
//			double innerTerm = beta.m_afData[0][0] * sinTerm * sinTerm * distance;
//			double value = Math.exp(innerTerm);
//			hessian.m_afData[0][0] = value * sinTerm * sinTerm * distance * sinTerm * sinTerm * distance;
//			hessian.m_afData[1][0] = value * 2 * sinTerm * cosTerm * -1 * (sinTerm * sinTerm * distance * beta.m_afData[0][0] + 1);
//			hessian.m_afData[0][1] = hessian.m_afData[1][0];
//			hessian.m_afData[1][1] =  -2 * beta.m_afData[0][0] * distance * value * (-2 * beta.m_afData[0][0] * distance * sinTerm * sinTerm * cosTerm * cosTerm - cosTerm * cosTerm + sinTerm * sinTerm);
//			return hessian;
//		}
//
//		@Deprecated
//		@Override
//		public void setX(Matrix x) {}
//		
//		@Override
//		public void setBeta(Matrix beta) {this.beta = beta;}
//		
//		@Override
//		public Matrix getBeta() {return beta;}
//
//		@Override
//		protected void setX(int indexFirstObservation, int indexSecondObservation) {
//			distance = data.getDistancesBetweenObservations().get(indexFirstObservation).get(indexSecondObservation);
//			angle = data.getAngleBetweenObservations().get(indexFirstObservation).get(indexSecondObservation).m_afData[0][1];		// distance on the horizontal plane
//		}
//		
//		@Override
//		public int getNumberOfVariables() {return 2;}
//
//		@Override
//		protected void initialize(StatisticalModel<?> model, HierarchicalStatisticalDataStructure data) throws StatisticalDataException {
//			super.initialize(model, data);
//			List<String> distanceFieldNames = ObjectUtility.decomposeUsingToken(distanceFieldsEnumeration, "+");
//			if (!(data instanceof HierarchicalSpatialDataStructure)) {
//				throw new StatisticalDataException("The data are not spatialized.");
//			} else {
//				this.data = (HierarchicalSpatialDataStructure) data;
//				this.data.setDistanceFields(distanceFieldNames);
//				this.data.getDistancesBetweenObservations();
//				this.data.getAngleBetweenObservations();
//			}
//		}
//	}


}
