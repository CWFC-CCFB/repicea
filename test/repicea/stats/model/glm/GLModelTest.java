/*
 * This file is part of the repicea library.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import repicea.stats.data.DataSet;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.copula.FGMCopulaGLModelTest;
import repicea.util.ObjectUtility;

public class GLModelTest {

	@Test
    public void TestWithSimpleGLModel() throws Exception {
		double expectedLlk = -1091.9193286646055;
		String filename = ObjectUtility.getPackagePath(FGMCopulaGLModelTest.class).concat("donneesR_min.csv");
		DataSet dataSet = new DataSet(filename, true);
		
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.Logit, "coupe ~ diffdhp + marchand:diffdhp + marchand:diffdhp2 +  essence");
		glm.doEstimation();
		double actualLlk = glm.getCompleteLogLikelihood().getValue();
		assertEquals(expectedLlk, actualLlk, 1E-5);
	}

}
