package repicea.stats.model.glm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import repicea.stats.data.DataSet;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.copula.FGMCopulaGLModelTest;
import repicea.stats.model.glm.measerr.GLMWithUniformMeasError;
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

    public void TestWithGLModel() throws Exception {
 		String filename = ObjectUtility.getPackagePath(GLModelTest.class).concat("OccurrencePartDataset_ERS.csv");
		DataSet dataSet = new DataSet(filename, true);
		
		GLMWithUniformMeasError glm = new GLMWithUniformMeasError(dataSet, "occurred ~ lnDt + TotalPrcp + logPrcp + LowestTmin +   \r\n"
				+ "                    lnPente + hasExpo:cosExpo + \r\n"
				+ "                    dummyDrainage4hydrique +\r\n"
				+ "                    G_F + lnG_F + G_R + lnG_R + LOGdistanceToConspecific + G_SpGr + lnG_SpGr +\r\n"
				+ "                    dummyPastDist3OtherNatural +\r\n"
				+ "                    timeSince1970",
				"LOGdistanceToConspecific",
				0d);
		long initialTime = System.currentTimeMillis();
		glm.doEstimation();
		System.out.println("Time to perform the estimation = " + (System.currentTimeMillis() - initialTime) + " ms.");
		glm.getSummary();
		double actualLlk = glm.getCompleteLogLikelihood().getValue();
	}

    public static void main(String[] args) throws Exception {
    	GLModelTest o = new GLModelTest();
    	o.TestWithGLModel();
    }
}
