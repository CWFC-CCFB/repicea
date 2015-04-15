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

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import repicea.math.EvaluableFunction;
import repicea.stats.AbstractStatisticalExpression;

/**
 * The GaussHermiteQuadrature class provides the x values and their weights for numerical integration.
 * The class implements the 5-point, 10-point and 15-point quadrature.
 * @author Mathieu Fortin - July 2012
 */
@SuppressWarnings("serial")
public class GaussHermiteQuadrature extends GaussQuadrature implements Serializable {
	
	private static Map<NumberOfPoints, Set<QuadratureNode>> NODE_MAP = new HashMap<NumberOfPoints, Set<QuadratureNode>>();
	static {
		Set<QuadratureNode> nodes = new HashSet<QuadratureNode>();
		nodes.add(new QuadratureNode(0d, 0.945308720482942));
		nodes.add(new QuadratureNode(0.958572464613819, 0.393619323152241));
		nodes.add(new QuadratureNode(0.202018287045609E1, 0.199532420590459E-1));
		NODE_MAP.put(NumberOfPoints.N5, nodes);
		
		nodes = new HashSet<QuadratureNode>();
		nodes.add(new QuadratureNode(0.342901327223704609, 0.610862633735325799));
		nodes.add(new QuadratureNode(0.103661082978951365E1, 0.240138611082314686));
		nodes.add(new QuadratureNode(0.175668364929988177E1, 0.338743944554810631E-1));
		nodes.add(new QuadratureNode(0.253273167423278980E1, 0.134364574678123269E-2));
		nodes.add(new QuadratureNode(0.343615911883773760E1, 0.764043285523262063E-5));
		NODE_MAP.put(NumberOfPoints.N10, nodes);
		
		nodes = new HashSet<QuadratureNode>();
		nodes.add(new QuadratureNode(0d, 0.564100308726417532853));
		nodes.add(new QuadratureNode(0.565069583255575748526, 0.412028687498898627026));
		nodes.add(new QuadratureNode(0.113611558521092066632E1, 0.158488915795935746884));
		nodes.add(new QuadratureNode(0.171999257518648893242E1, 0.307800338725460822287E-1));
		nodes.add(new QuadratureNode(0.232573248617385774545E1, 0.277806884291277589608E-2));
		nodes.add(new QuadratureNode(0.296716692790560324849E1, 0.100004441232499868127E-3));
		nodes.add(new QuadratureNode(0.366995037340445253473E1, 0.105911554771106663578E-5));
		nodes.add(new QuadratureNode(0.449999070730939155366E1, 0.152247580425351702016E-8));
		NODE_MAP.put(NumberOfPoints.N15, nodes);
		  			
	}
	
	private NumberOfPoints numberOfPoints;
	
	/**
	 * Constructor.
	 * @param numberOfPoints a NumberOfPoints enum variable (either NumberOfPoints.N5, NumberOfPoints.N10, or NumberOfPoints.N15) 
	 */
	public GaussHermiteQuadrature(NumberOfPoints numberOfPoints) {
		if (!NODE_MAP.containsKey(numberOfPoints)) {
			throw new InvalidParameterException("The Gauss-Hermite quadrature with this number of points is not implemented!");
		}
		this.numberOfPoints = numberOfPoints;
	}
	
	@Override
	public List<Double> getWeights() {
		if (weights == null) {
			getXValues();
		}
		return weights;
	}



	@Override
	public List<Double> getXValues() {
		if (xValues == null) {
			xValues = new ArrayList<Double>();
			weights = new ArrayList<Double>();
			List<QuadratureNode> orderedNodes = getOrderedNodes(GaussHermiteQuadrature.NODE_MAP.get(numberOfPoints));
			for (QuadratureNode node : orderedNodes) {
				xValues.add(node.getValue());
				weights.add(node.getWeight());
			}
		}
		return xValues;
	}



	@Override
	public List<Double> getRescalingFactors() {
		if (rescalingFactors == null) {
			rescalingFactors = new ArrayList<Double>();
			List<Double> xValues = getXValues();
			for (int i = 0; i < xValues.size(); i++) {
				rescalingFactors.add(1d);
			}
		}
		return rescalingFactors;
	}

	/**
	 * This method makes it possible to integrate an AbstractStatisticalExpression through Gauss-Hermite quadrature.
	 * @param functionToEvaluate an EvaluableFunction instance that returns Double 
	 * @param functionWithTheVariableToChange the AbstractStatisticalExpression with the variable that changes
	 * @param variableIndex the index of the variable over which the integration is made
	 * @param standardDeviation the standard deviation of this variable
	 * @return the approximation of the integral
	 */
	public double getOneDimensionIntegral(EvaluableFunction<Double> functionToEvaluate,
											AbstractStatisticalExpression functionWithTheVariableToChange,
											Integer variableIndex, 
											double standardDeviation) {
		double originalValue = functionWithTheVariableToChange.getVariableValue(variableIndex);
		double sum = 0;
		double value;
		for (int i = 0; i < getXValues().size(); i++) {
			double tmp = getXValues().get(i) * standardDeviation * Math.sqrt(2d);
			functionWithTheVariableToChange.setVariableValue(variableIndex, originalValue + tmp);
			value = 1d / Math.sqrt(Math.PI) * functionToEvaluate.getValue() * getWeights().get(i);
			sum += value;
		}
		functionWithTheVariableToChange.setVariableValue(variableIndex, originalValue);
		return sum;
	}
	
	
}
