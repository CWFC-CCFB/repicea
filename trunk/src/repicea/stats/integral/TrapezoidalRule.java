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
package repicea.stats.integral;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * The TrapezoidalRule class implements the trapezoidal rule integration method.
 * The resolution parameter in the constructor sets the distance between the x values.
 * The class is adapted to resolution that does not perfectly match the range of the integral.
 * @author Mathieu Fortin - July 2012
 */
@SuppressWarnings("serial")
public final class TrapezoidalRule extends NumericalIntegrationMethod {

	private double resolution;
	
	/**
	 * Constructor.
	 * @param resolution the distance between the x points (must be larger than 0)
	 */
	public TrapezoidalRule(double resolution) {
		if (resolution <= 0) {
			throw new InvalidParameterException("The resolution must be larger than 0!");
		}
		this.resolution = resolution;
	}
	
	
	/**
	 * This constructor is set through a list of double instances that represents the point along the
	 * integral. Any replicate is eliminated in the process.
	 * @param points a List of Double instances
	 */
	public TrapezoidalRule(List<Double> points) {
		setXValuesFromListOfPoints(points);
	}

	private List<Double> setXValues() {
		if (Double.isNaN(getLowerBound()) || Double.isNaN(getUpperBound())) {
			return null;
		} else {
			double difference = getUpperBound() - getLowerBound();
			double numberOfSectionsWithDigits = difference / resolution;
			int numberOfSections = (int) (numberOfSectionsWithDigits);
			if (numberOfSectionsWithDigits > numberOfSections) {
				numberOfSections++;			// add one section for the top section
			}
			numberOfSections++;
			List<Double> output = new ArrayList<Double>();
			double height;
			int i = 0;
			while (i < numberOfSections) {
				height = getLowerBound() + i * resolution;
				if (height >= getUpperBound()) {
					height = getUpperBound();
				}
				output.add(height);
				i++;
			}
			return output;
		}
	}

	/*
	 * Calculates the weights based on the distance between the sections. The distances does not need to be even. 
	 * (non-Javadoc)
	 * @see repicea.stats.integral.NumericalIntegrationMethod#getWeights()
	 */
	@Override
	public List<Double> getWeights() {
		if (weights == null) {
			List<Double> xValues = getXValues();
			if (xValues != null) {
				weights = new ArrayList<Double>();
				double diff;
				double lag = 0;
				for (int i = 0; i < xValues.size(); i++) {
					if (i == xValues.size() - 1) {
						diff = 0;
					} else {
						diff = (xValues.get(i + 1) - xValues.get(i)) * .5;
					}
					weights.add(diff + lag);
					lag = diff;
				}
			}
		}
		return weights;
	}


	@Override
	public List<Double> getXValues() {
		if (xValues == null) {
			xValues = setXValues();
		}
		return xValues;
	}

	@Override
	public List<Double> getRescalingFactors() {
		if (rescalingFactors == null) {
			List<Double> xValues = getXValues();
			if (xValues != null) {
				rescalingFactors = new ArrayList<Double>();
				for (int i = 0; i < xValues.size(); i++) {
					rescalingFactors.add(1d);
				}
			}
		}
		return rescalingFactors;
	}


	
//	public static void main(String[] args) {
//		TrapezoidalRule tr = new TrapezoidalRule(3);
//		tr.setLowerBound(0);
//		tr.setUpperBound(20);
//		List<Double> xValues = tr.getXValues();
//		List<Double> weights = tr.getWeights();
//		List<Double> mf = tr.getRescalingFactors();
//		int u = 0;
//	}
	
}
