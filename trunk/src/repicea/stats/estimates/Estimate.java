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
package repicea.stats.estimates;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repicea.math.Matrix;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.stats.Distribution;
import repicea.stats.RandomVariable;

/**
 * The Estimate class is the basic class for all estimates.
 * @author Mathieu Fortin - March 2012
 * @param <D> a Distribution derived instance which represents the assumed distribution for the estimate
 */
public abstract class Estimate<D extends Distribution> extends RandomVariable<D> {
	
	static {
		XmlSerializerChangeMonitor.registerEnumNameChange("repicea.stats.estimates.Estimate$EstimatorType", "MonteCarlo", "Resampling");
	}
	
	
	private static final long serialVersionUID = 20120825L;
	
	protected EstimatorType estimatorType;

	protected final List<String> rowIndex;
	
	protected final List<List<String>> collapseIndexList;
	
	/**
	 * The type of estimator.
	 * @author Mathieu Fortin - March 2012
s	 */
	public static enum EstimatorType {
		Resampling, 
		LeastSquares, 
		LikelihoodBased, 
		MomentBased, 
		Unknown}

	protected Estimate(D distribution) {
		super(distribution);
		rowIndex = new ArrayList<String>();
		collapseIndexList = new ArrayList<List<String>>();
	}
	
	
	/**
	 * This method returns the type of the estimator.
	 * @return an EstimatorType instance
	 */
	public EstimatorType getEstimatorType() {return estimatorType;}

	/**
	 * This method makes it possible to set an optional row index. This is useful when the 
	 * response is a vector. 
	 * @param newRowIndex a List of String instance. A null value reset the 
	 */
	public void setRowIndex(List<String> newRowIndex) {
		this.rowIndex.clear();
		if (newRowIndex != null && !newRowIndex.isEmpty()) {
			this.rowIndex.addAll(newRowIndex);
		}
		collapseIndexList.clear();
	}

	
	/**
	 * This method returns a copy of the row index. 
	 * @return a List of String instance or null if the row index has not been set.
	 */
	public List<String> getRowIndex() {
		List<String> rowIndexCopy = new ArrayList<String>();
		rowIndexCopy.addAll(rowIndex);
		return rowIndexCopy;
	}
	
	/**
	 * This method returns a random deviate from this estimate. This method
	 * is useful for Monte Carlo simulations.
	 * @return a deviate from the underlying distribution as a Matrix instance
	 */
	public Matrix getRandomDeviate() {
		return getDistribution().getRandomRealization();
	}

	
	/**
	 * This method returns an difference estimate.
	 * @param estimate2 an Estimate to be subtracted from this estimate.
	 * @return an Estimate
	 */
	public Estimate<?> getDifferenceEstimate(Estimate<?> estimate2) {
		Matrix diff = getMean().subtract(estimate2.getMean());
		Matrix variance = getVariance().add(estimate2.getVariance());
		return new GaussianEstimate(diff, variance);
	}
	
	/**
	 * This method returns a sum of two estimates.
	 * @param estimate2 an Estimate to be added to this estimate.
	 * @return an Estimate
	 */
	public Estimate<?> getSumEstimate(Estimate<?> estimate2) {
		Matrix diff = getMean().add(estimate2.getMean());
		Matrix variance = getVariance().add(estimate2.getVariance());
		return new GaussianEstimate(diff, variance);
	}

	
	/**
	 * This method returns an difference estimate.
	 * @param scalar a double to be multiplied by this estimate
	 * @return an Estimate
	 */
	public Estimate<?> getProductEstimate(double scalar) {
		Matrix diff = getMean().scalarMultiply(scalar);
		Matrix variance = getVariance().scalarMultiply(scalar * scalar);
		return new GaussianEstimate(diff, variance);
	}

	/**
	 * This method returns the probability of getting a lower valueand upper bound of a confidence intervals at probability
	 * level 1 - alpha
	 * @param oneMinusAlpha is 1 minus the probability of Type I error
	 * @return a ConfidenceInterval instance 
	 */
	public abstract ConfidenceInterval getConfidenceIntervalBounds(double oneMinusAlpha);
	
	/**
	 * This method checks if the two point estimates are compatible. The basic
	 * check consists of comparing the classes. Then, the matrix data is checked
	 * for consistency with previous data.
	 * @param estimate
	 * @return a boolean
	 */
	protected boolean isMergeableEstimate(Estimate<?> estimate) {
		return false;
	}
	

	/**
	 * Returns an estimate of the product of two parametric univariate estimate. The variance
	 * estimator is based on Goodman's estimator.
	 * @param estimate an Estimate instance
	 * @return a SimpleEstimate instance
	 */
	public SimpleEstimate getProductEstimate(Estimate<?> estimate) {
		if (estimate.getDistribution().isUnivariate() && getDistribution().isUnivariate()) {
			Matrix alphaMean = getMean();
			Matrix betaMean = estimate.getMean();
			Matrix alphaVariance = getVariance();
			Matrix betaVariance = estimate.getVariance();
			Matrix newMean = alphaMean.multiply(betaMean);
			Matrix newVariance = alphaMean.elementWisePower(2d).multiply(betaVariance).
					add(betaMean.elementWisePower(2d).multiply(alphaVariance)).
					subtract(alphaVariance.multiply(betaVariance));
			return new SimpleEstimate(newMean, newVariance);
		}
		throw new InvalidParameterException("The getProductEstimate is only implemented for parametric univariate distribution ");
	}
	
	public static SimpleEstimate getProductOfManyEstimates(List<Estimate> estimates) {
		Estimate currentEstimate = null;
		for (int i = 1; i < estimates.size(); i++) {
			if (i == 1) {
				currentEstimate = estimates.get(i-1);
			} 
			currentEstimate = currentEstimate.getProductEstimate(estimates.get(i));
		}
		return (SimpleEstimate) currentEstimate;
	}
	
	/**
	 * Set collapse index list. This list enabled the collapsing of the estimate into 
	 * smaller mean and variance matrices. The desiredIndicesForCollapsing is checked
	 * to make sure it complies with the current index.
	 * @param desiredIndicesForCollapsing a List of List of String
	 */
	public void setCollapseIndexList(List<List<String>> desiredIndicesForCollapsing) {
		List<String> copyOfIndex = new ArrayList<String>();
		copyOfIndex.addAll(getRowIndex());
		Collections.sort(copyOfIndex);
		List<String> completeList = new ArrayList<String>();
		for (List<String> l : desiredIndicesForCollapsing) {
			completeList.addAll(l);
		}
		Collections.sort(completeList);
		if (!completeList.equals(copyOfIndex)) {
			throw new InvalidParameterException("Some indices are missing!");
		} else {
			collapseIndexList.clear();
			collapseIndexList.addAll(desiredIndicesForCollapsing);
		}
	}
	
	protected final List<Integer> convertIndexIntoInteger(List<String> selectedIndices) {
		List<Integer> outputList = new ArrayList<Integer>();
		for (String s : selectedIndices) {
			outputList.add(getRowIndex().indexOf(s));
		}
		return outputList;
	}
	
	@Override
	public Matrix getMean() {
		Matrix basicMean = super.getMean();
		if (!rowIndex.isEmpty()) {
			if (rowIndex.size() == basicMean.m_iRows && !collapseIndexList.isEmpty()) {
				Matrix newMean = new Matrix(collapseIndexList.size(), 1);
				for (int i = 0; i < newMean.m_iRows; i++) {
					List<String> requestedIndices = collapseIndexList.get(i);
					newMean.m_afData[i][0] = basicMean.getSubMatrix(convertIndexIntoInteger(requestedIndices), null).getSumOfElements();
				}
				return newMean;
			}			
		}
		return basicMean;
	}

	@Override
	public Matrix getVariance() {
		Matrix basicVariance = super.getVariance();
		if (!rowIndex.isEmpty()) {
			if (rowIndex.size() == basicVariance.m_iRows && !collapseIndexList.isEmpty()) {
				Matrix newVariance = new Matrix(collapseIndexList.size(), collapseIndexList.size());
				for (int i = 0; i < newVariance.m_iRows; i++) {
					List<String> requestedIndices_i = collapseIndexList.get(i);
					for (int j = 0; j < newVariance.m_iRows; j++) {
						List<String> requestedIndices_j = collapseIndexList.get(j);
						newVariance.m_afData[i][0] = basicVariance.getSubMatrix(convertIndexIntoInteger(requestedIndices_i), 
								convertIndexIntoInteger(requestedIndices_j)).getSumOfElements();
					}
				}
				return newVariance;
			} 
		}
		return basicVariance;
	}
	
}
