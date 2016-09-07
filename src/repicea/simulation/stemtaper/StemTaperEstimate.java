/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.simulation.stemtaper;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.CentralMomentsSettable;
import repicea.stats.Distribution;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.distributions.NonparametricDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.stats.integral.TrapezoidalRule;

/**
 * The StemTaperEstimate class extends the Estimate class. It handles MonteCarlo or maximum likelihood based estimates.
 * likelihood based. The method getMean() and getVariance() are overriden in order to select which super methods apply.
 * The StemTaperEstimate handles the volume integration.
 * @author Mathieu Fortin - March 2012
 */
@SuppressWarnings({ "rawtypes", "serial" })
public abstract class StemTaperEstimate extends Estimate<Distribution> implements CentralMomentsSettable  {
		
	private final Distribution alternateDistribution;

	private List<Double> heights;
	private Estimate<?> volumeEstimate;

	/**
	 * Constructor 1.
	 * @param computedHeights a List instance containing the heights (m) of the cross sections
	 */
	public StemTaperEstimate(List<Double> computedHeights) {
		this(false, computedHeights);
	}

	/**
	 * Constructor 2.
	 * @param numberOfRealizations the number of realizations (an integer)
	 * @param isMonteCarlo a boolean 
	 * @param computedHeights a List instance containing the heights (m) of the cross sections
	 */
	public StemTaperEstimate(boolean isMonteCarlo, List<Double> computedHeights) {
		super(new NonparametricDistribution());
		alternateDistribution = new GaussianDistribution(null, null);
		if (isMonteCarlo) {
			estimatorType = EstimatorType.MonteCarlo;
		} else {
			estimatorType = EstimatorType.LikelihoodBased;
		}
		heights = computedHeights;
	}

	
	/**
	 * This method post processes the predictions. If a particular height appears twice in the originalHeightsToEvaluate
	 * member, the matrix row (and column) corresponding to this heights is doubled. 
	 * @param originalPredictions the original predictions (Matrix instance)
	 * @return the post processing predictions (also Matrix instance)
	 */
	private Matrix reshapeMatrixAccordingToSegments(StemTaperSegmentList segments, Matrix originalPredictions) {
		List<Double> originalHeightsToEvaluate = segments.getHeights();
		
		if (originalHeightsToEvaluate.size() == heights.size() && heights.containsAll(originalHeightsToEvaluate)) {
			return originalPredictions;
		} else {
			Matrix outputMatrix;
			boolean isColumnVector = false;
			if (originalPredictions.isColumnVector()) {
				isColumnVector = true;
				outputMatrix = new Matrix(originalHeightsToEvaluate.size(), 1);
			} else if (originalPredictions.isSquare()) {
				outputMatrix = new Matrix(originalHeightsToEvaluate.size(), originalHeightsToEvaluate.size());
			} else {
				throw new InvalidParameterException("Matrix originalPredictions is not square nor a row vector!");
			}
			int i_index, j_index;
			for (int i = 0; i < outputMatrix.m_iRows; i++) {
				i_index = heights.indexOf(originalHeightsToEvaluate.get(i));
				if (isColumnVector) {
					j_index = 0;
					outputMatrix.m_afData[i][0] = originalPredictions.m_afData[i_index][j_index];
				} else {		// is square then
					for (int j = i; j < outputMatrix.m_iCols; j++) {
						j_index = heights.indexOf(originalHeightsToEvaluate.get(j));
						outputMatrix.m_afData[i][j] = originalPredictions.m_afData[i_index][j_index];
						outputMatrix.m_afData[j][i] = originalPredictions.m_afData[i_index][j_index];
					}
				}
			}
			return outputMatrix;
		}
	}
	

	/**
	 * This method returns a volume estimate from the integrated taper all along the heights contained
	 * in the heights member.
	 * @return an Estimate instance 
	 */
	public Estimate<?> getVolumeEstimate() {
		return getVolumeEstimate(null);
	}
	
	
	/**
	 * This method returns a volume estimate from the integrated taper all along the selected segments.
	 * @param segments a StemTaperSegmentList that represents the selected taper (can be null, in such case the integration is carried out
	 * all along the heights, by default)
	 * @return an Estimate instance 
	 */
	public Estimate<?> getVolumeEstimate(StemTaperSegmentList segments) {
		if (segments == null) {	// means it has to be integrated all along the sections
			if (volumeEstimate == null) {
				segments = getDefaultSegments();
				volumeEstimate = getVolumeEstimateForTheseSegments(segments);
			} 
			return volumeEstimate;
		} else {
			return getVolumeEstimateForTheseSegments(segments);
		}
	}

	
	private StemTaperSegmentList getDefaultSegments() {
		StemTaperSegmentList segments = new StemTaperSegmentList();
		segments.add(new StemTaperSegment(new TrapezoidalRule(heights)));
		return segments;
	}


	/**
	 * This method returns a volume estimate from the integrated taper all along the selected segments.
	 * @param segments a StemTaperSegmentList instance
	 * @return an Estimate instance 
	 */
	private Estimate<?> getVolumeEstimateForTheseSegments(StemTaperSegmentList segments) {
		if (!heights.containsAll(segments.getHeights())) {
			throw new InvalidParameterException("There is a mismatch between the requested sections and the heights that have been computed!");
		}

		Matrix weights = new Matrix(segments.getWeightsAcrossSegments());
		Matrix rescalingFactors = new Matrix(segments.getRescalingFactorsAcrossSegments());
//		Matrix volumeFactor = rescalingFactors.elementWiseMultiply(weights).scalarMultiply(Math.PI / 4 * 1E-3); 							// 1E-3 is a factor to express the result in dm3
		Matrix volumeFactor = rescalingFactors.elementWiseMultiply(weights).scalarMultiply(getScalingFactor());
		Matrix varianceFactor = volumeFactor.multiply(volumeFactor.transpose());

		Estimate result;
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			result = new MonteCarloEstimate();
		} else {
			result = new GaussianEstimate();
			((GaussianEstimate) result).setMean(null);
			((GaussianEstimate) result).setVariance(null);
		}

		Matrix volumeEstim;
		Matrix taper;
		Matrix variance;

		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			for (int iter = 0; iter < getNumberOfRealizations(); iter++) {
				taper = getSquaredDiameters(getRealizations().get(iter));
				taper = reshapeMatrixAccordingToSegments(segments, taper);
				volumeEstim = taper.elementWiseMultiply(volumeFactor);
				((MonteCarloEstimate) result).addRealization(volumeEstim);
			}
		} else {

			taper = getSquaredDiameters(reshapeMatrixAccordingToSegments(segments, getMean()));

			volumeEstim = taper.elementWiseMultiply(volumeFactor);
			((GaussianEstimate) result).setMean(volumeEstim);
			if (getVariance() != null) {
				variance = getVarianceOfSquaredDiameter(reshapeMatrixAccordingToSegments(segments, getVariance()));
				((GaussianEstimate) result).setVariance(variance.elementWiseMultiply(varianceFactor));
			}
		}
		return result;
	}


	/**
	 * This method returns the heights of the cross sections for which the square diameters were predicted.
	 * @return a List of Double instances
	 */
	public List<Double> getCrossSectionHeights() {return heights;}
	
	protected abstract Matrix getSquaredDiameters(Matrix predictedDiameters);
	
	protected abstract Matrix getVarianceOfSquaredDiameter(Matrix variancePredictedDiameters);
	
	protected abstract double getScalingFactor();
	
	@Override
	public Distribution getDistribution() {
		if (estimatorType == EstimatorType.MonteCarlo) {
			return super.getDistribution();
		} else {
			return alternateDistribution;
		}
	}
	
	protected int getNumberOfRealizations() {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			return ((NonparametricDistribution) getDistribution()).getNumberOfRealizations();
		} else {
			return 0;		// TODO check if this works
		}
	}

	protected List<Matrix> getRealizations() {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			return ((NonparametricDistribution) getDistribution()).getRealizations();
		} else {
			return null;		// TODO check if this works
		}
	}

	@Override
	public void setMean(Matrix mean) {
		if (getEstimatorType() == EstimatorType.LikelihoodBased) {
			((GaussianDistribution) getDistribution()).setMean(mean);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to likelihood based!");
		}
	}

	@Override
	public void setVariance(Matrix variance) {
		if (getEstimatorType() == EstimatorType.LikelihoodBased) {
			((GaussianDistribution) getDistribution()).setVariance(variance);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to likelihood based!");
		}
	}

	/**
	 * This method records the realization of a particular Monte Carlo iteration
	 * @param realization the realization as a Matrix instance
	 */
	public void addRealization(Matrix realization) {
		if (getEstimatorType() == EstimatorType.MonteCarlo) {
			((NonparametricDistribution) getDistribution()).addRealization(realization);
		} else {
			throw new InvalidParameterException("The HybridEstimate was set to Monte Carlo!");
		}
	}		

}
