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
		DataSet dataSet = new DataSet(filename);
		
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.Logit, "coupe ~ diffdhp + marchand:diffdhp + marchand:diffdhp2 +  essence");
		glm.doEstimation();
		double actualLlk = glm.getCompleteLogLikelihood().getValue();
		assertEquals(expectedLlk, actualLlk, 1E-5);
	}

}
