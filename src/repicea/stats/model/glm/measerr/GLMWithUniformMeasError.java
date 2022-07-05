package repicea.stats.model.glm.measerr;

import java.security.InvalidParameterException;

import repicea.stats.data.DataSet;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LikelihoodGLM;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

public class GLMWithUniformMeasError extends GeneralizedLinearModel {

	private static class LinkFunctionWithMeasError extends LinkFunction {

		public LinkFunctionWithMeasError(Type type) {
			super(type);
		}
		// TODO FP implement the link function with measurement error
	}
	
	
	protected final String effectWithMeasError;
	protected final double minEffectWithMeasError;
	
	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, String effectWithMeasError, double effectWithMeasErrorMin) {
		super(dataSet, Type.CLogLog, modelDefinition);
		if (!getDataStructure().isThisEffectInModelDefinition(effectWithMeasError)) {
			throw new InvalidParameterException("The effect with measurement error " + effectWithMeasError + " is not part of the model definition!");
		} else {
			this.effectWithMeasError = effectWithMeasError;
			this.minEffectWithMeasError = effectWithMeasErrorMin;
		}
	}

	
	protected void initializeLinkFunction(Type linkFunctionType) {
		LinkFunction lf = new LinkFunction(linkFunctionType);
		individualLLK = new IndividualLogLikelihood(new LikelihoodGLM(lf));
		setCompleteLLK();
	}

}
