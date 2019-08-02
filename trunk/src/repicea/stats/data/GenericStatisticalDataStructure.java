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

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;
import repicea.util.ObjectUtility;

/**
 * The StatisticalDataStructure class is an abstract class that implements all the features to be able
 * to fit a statistical model. The structure includes a vector of dependent variables (vectorY), a matrix
 * of covariates (matrixX) and a possible hierarchical data structure (hierarchicalStructure).
 * @author Mathieu Fortin - June 2011
 */
public class GenericStatisticalDataStructure implements StatisticalDataStructure {

	protected DataSet dataSet;
	protected boolean isInterceptModel;
	protected Matrix vectorY;
	protected Matrix matrixX;
	
	/**
	 * General constructor.
	 * @param dataSet the DataSet instance from which the structure is going to be extracted
	 */
	public GenericStatisticalDataStructure(DataSet dataSet) {
		this.dataSet= dataSet;
		isInterceptModel = true;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Matrix computeDummyVariables(int fieldIndex, String refClass) throws StatisticalDataException {
		List possibleValues = dataSet.getPossibleValuesInThisField(fieldIndex);
		Collections.sort(possibleValues);
		if (refClass != null && !possibleValues.contains(refClass)) {
			throw new StatisticalDataException("Reference class category " + refClass + " does not belong to this class variable!");
		}
				
		if (isInterceptModel()) {
			if (refClass != null) {
				possibleValues.remove(possibleValues.indexOf(refClass));
			} else {
				possibleValues.remove(0);
			}
		} 
		
//		Matrix outputMatrix = new Matrix(getNumberOfObservations(), possibleValues.size());
//		for (int i = 0; i < getNumberOfObservations(); i++) {
//			int position = possibleValues.indexOf(dataSet.getValueAt(i, fieldIndex));
//			if (position >= 0 && position < outputMatrix.m_iCols) {
//				outputMatrix.m_afData[i][position] = 1d;
//			}
//		}
//		return outputMatrix;
		return dataSet.getDummyMatrix(possibleValues, fieldIndex);
	}

	
	@Override
	public boolean isInterceptModel() {return isInterceptModel;}
	
	@Override
	public void setInterceptModel(boolean isInterceptModel) {this.isInterceptModel = isInterceptModel;}
	
	
	public Matrix getMatrixX() {return matrixX;}
	public Matrix getVectorY() {return vectorY;}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void constructMatrices(String modelDefinition) throws StatisticalDataException {
		matrixX = null;
		
		if (this.isInterceptModel) {
			matrixX = new Matrix(getNumberOfObservations(), 1, 1, 0);
		}
		
		Vector<String> effectsInThisInteraction = new Vector<String>();

		List<String> responseAndFixedEffects = ObjectUtility.decomposeUsingToken(modelDefinition, "~"); 
		
		if (responseAndFixedEffects.size() != 2) {
			throw new StatisticalDataException("The model specification is incorrect!");
		}
		
		String yName = responseAndFixedEffects.get(0);
		vectorY = dataSet.getVectorOfThisField(yName);
		String modelEffects = responseAndFixedEffects.get(1);

		List<String> effects = ObjectUtility.decomposeUsingToken(modelEffects, "+");

		for (String effectName : effects) {
			StringTokenizer tkzInclusiveInteraction = new StringTokenizer(effectName, "*");
			StringTokenizer tkzExclusiveInteraction = new StringTokenizer(effectName, ":");
			effectsInThisInteraction.clear();

			int numberOfInclusiveInteraction = tkzInclusiveInteraction.countTokens();
			int numberOfExclusiveInteraction = tkzExclusiveInteraction.countTokens();

			if (numberOfInclusiveInteraction > 1 && numberOfExclusiveInteraction > 1) {
				throw new StatisticalDataException("Error : symbols * and : are being used at the same time in the model specification!");
			}

			boolean isAnInclusiveInteraction = numberOfInclusiveInteraction > 1;
			StringTokenizer selectedTokenizer;
			if (isAnInclusiveInteraction) {
				selectedTokenizer = tkzInclusiveInteraction;
			} else {
				selectedTokenizer = tkzExclusiveInteraction;
			}

			while (selectedTokenizer.hasMoreTokens()) {
				effectsInThisInteraction.add(selectedTokenizer.nextToken());
			}

			Matrix subMatrixX = null;
			Matrix matrixTmp;
			for (String effect : effectsInThisInteraction) {

				int indexOfReferenceClass = effect.indexOf("#");
				String refClass = null;
				if (indexOfReferenceClass != -1) {
					refClass = effect.substring(indexOfReferenceClass + 1);
					effect = effect.substring(0, indexOfReferenceClass);
				}

				int indexOfThisField = dataSet.getIndexOfThisField(effect);
				Class fieldType = dataSet.getFieldTypeOfThisField(indexOfThisField);
				if (fieldType == Double.class) {
					matrixTmp = dataSet.getVectorOfThisField(indexOfThisField);
				} else {
					matrixTmp = computeDummyVariables(indexOfThisField, refClass);
				}

				if (subMatrixX == null) {
					subMatrixX = matrixTmp;
				} else {
					subMatrixX = StatisticalUtility.combineMatrices(subMatrixX, matrixTmp);
				}

			}

			if (matrixX == null) {
				matrixX = subMatrixX;
			} else {
				matrixX = matrixX.matrixStack(subMatrixX, false);
			}
			
		}
		
	}

	@Override
	public int getNumberOfObservations() {return dataSet.getNumberOfObservations();}


	@Override
	public DataSet getDataSet() {return dataSet;}

	
}
