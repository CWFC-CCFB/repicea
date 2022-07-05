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
package repicea.stats.model.glm;

import repicea.math.Matrix;
import repicea.stats.model.IndividualLikelihood;

/**
 * This class simply handles the exponent y and 1-y of the likelihood function.  
 * @author Mathieu Fortin - July 2022
 */
@SuppressWarnings("serial")
public class LikelihoodGLM extends IndividualLikelihood {

	protected final LinkFunction linkFunction;
	
	public LikelihoodGLM(LinkFunction linkFunction) {
		super(linkFunction.getOriginalFunction());
		this.linkFunction = linkFunction;
	}

	@Override
	public Matrix getPredictionVector() {
		Matrix mat = new Matrix(1,1);
		mat.setValueAt(0, 0, linkFunction.getValue());
		return mat;
	}
	
	
	@Override
	public Double getValue() {
		double predicted = getPredictionVector().getValueAt(0, 0);
		if (observedValues.getValueAt(0, 0) == 1d) {
			return predicted;
		} else {
			return 1d - predicted; 
		}
	}

	@Override
	public Matrix getGradient() {
		Matrix lfGradient = linkFunction.getGradient();
		if (observedValues.getValueAt(0, 0) == 1d) {
			return lfGradient;
		} else {
			return lfGradient.scalarMultiply(-1d);
		}
	}

	@Override
	public Matrix getHessian() {
		Matrix lfHessian = linkFunction.getHessian();
		if (observedValues.getValueAt(0, 0) == 1d) {
			return lfHessian;
		} else {
			return lfHessian.scalarMultiply(-1d);
		}
	}


}
