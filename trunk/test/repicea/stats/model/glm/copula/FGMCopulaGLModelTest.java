package repicea.stats.model.glm.copula;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import repicea.stats.ParameterBound;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.copula.CopulaExpression;
import repicea.stats.model.glm.copula.CopulaLibrary;
import repicea.stats.model.glm.copula.FGMCopulaGLModel;
import repicea.util.ObjectUtility;

public class FGMCopulaGLModelTest {

	/**
	 * This test implements a constant parameter copula model.
	 * @throws Exception
	 */
	@Test
    public void TestWithSimpleCopula() throws Exception {
		double expectedCopulaValue = 0.18072198394164396;
		double expectedLlk = -1077.1623243630413;
		String filename = ObjectUtility.getPackagePath(FGMCopulaGLModelTest.class).concat("donneesR_min.csv");
		DataSet dataSet = new DataSet(filename);
		
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.Logit, "coupe ~ diffdhp + marchand:diffdhp + marchand:diffdhp2 +  essence");
		
		try {
			CopulaExpression copula = new CopulaLibrary.SimpleCopulaExpression(0.0, "IDENT");
			FGMCopulaGLModel copulaModel = new FGMCopulaGLModel(glm, copula);
			copulaModel.doEstimation();
			double actual = copula.getValue();
			assertEquals(expectedCopulaValue, actual, 1E-5);
			double actualLlk = copulaModel.getCompleteLogLikelihood().getValue();
			assertEquals(expectedLlk, actualLlk, 1E-5);
		} catch (StatisticalDataException e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	/**
	 * This test implements a constant parameter copula model.
	 * @throws Exception
	 */
	@Test
    public void TestWithDistanceCopula() throws Exception {
		double expectedCopulaValue = -0.17037528040263983;
		double expectedLlk = -1062.135150078125;
		String filename = ObjectUtility.getPackagePath(FGMCopulaGLModelTest.class).concat("donneesR_min.csv");
		DataSet dataSet = new DataSet(filename);
		
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.Logit, "coupe ~ diffdhp + marchand:diffdhp + marchand:diffdhp2 +  essence");
		
		try {
			CopulaExpression distanceCopula = new CopulaLibrary.DistanceLinkFunctionCopulaExpression(Type.Log, "IDENT", "X + Y", -.15);		// no intercept in the linear expression
			distanceCopula.setBounds(0, new ParameterBound(null, 0d));
			FGMCopulaGLModel copulaModel = new FGMCopulaGLModel(glm, distanceCopula);
			copulaModel.setConvergenceCriterion(1E-8);
			copulaModel.gridSearch(copulaModel.getParameters().m_iRows - 1, -.25d, -.15d, .01);
			copulaModel.doEstimation();
			double actual = distanceCopula.getBeta().m_afData[0][0];
			assertEquals(expectedCopulaValue, actual, 1E-5);
//			double actualLlk = copulaModel.getLogLikelihood().getValue();
//			assertEquals(expectedLlk, actualLlk, 1E-5);
		} catch (StatisticalDataException e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
}
