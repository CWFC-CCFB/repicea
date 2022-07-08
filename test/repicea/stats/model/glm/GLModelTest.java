package repicea.stats.model.glm;

import static org.junit.Assert.assertEquals;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.Test;

import repicea.math.ExponentialIntegralFunction;
import repicea.math.optimizer.AbstractOptimizer.LineSearchMethod;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.data.DataSet;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.copula.FGMCopulaGLModelTest;
import repicea.stats.model.glm.measerr.GLMWithUniformMeasError;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

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
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(Level.FINE);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.FINE);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);
//		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "occurred ~ lnDt + TotalPrcp + logPrcp + LowestTmin +   \r\n"
//				+ "                    lnPente + hasExpo:cosExpo + \r\n"
//				+ "                    dummyDrainage4hydrique +\r\n"
//				+ "                    G_F + lnG_F + G_R + lnG_R + distanceToConspecific + G_SpGr + lnG_SpGr +\r\n"
//				+ "                    dummyPastDist3OtherNatural +\r\n"
//				+ "                    timeSince1970");
		GeneralizedLinearModel glm = new GeneralizedLinearModel(dataSet, Type.CLogLog, "occurred ~ lnDt + distanceToConspecific");
		glm.doEstimation();
		glm.getSummary();

		GLMWithUniformMeasError glmWithMeasError = new GLMWithUniformMeasError(dataSet, "occurred ~ lnDt + distanceToConspecificOLD",
				"distanceToConspecificOLD", glm.getParameters());
//		glmWithMeasError.gridSearch(1, -10.00, -3.00, 0.5);
		((MaximumLikelihoodEstimator) glmWithMeasError.getEstimator()).setLineSearchMethod(LineSearchMethod.HALF_STEP);
		glmWithMeasError.doEstimation();
		glmWithMeasError.getSummary();
	}

    public static void main(String[] args) throws Exception {
    	GLModelTest o = new GLModelTest();
    	o.TestWithGLModel();
    }
}
