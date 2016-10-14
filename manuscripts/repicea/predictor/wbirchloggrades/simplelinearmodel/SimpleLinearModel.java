package repicea.predictor.wbirchloggrades.simplelinearmodel;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
class SimpleLinearModel extends REpiceaPredictor {

	protected SimpleLinearModel(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		init();
	}

	@Override
	protected void init() {
		Matrix beta = new Matrix(2,1);
		beta.m_afData[0][0] = 4d;
		beta.m_afData[1][0] = 3d;
		Matrix omega = new Matrix(2,2);
		omega.m_afData[0][0] = 0.025;
		omega.m_afData[1][1] = 0.0005;
		omega.m_afData[0][1] = Math.sqrt(omega.m_afData[0][0] * omega.m_afData[1][1]) * .1;
		omega.m_afData[1][0] = omega.m_afData[0][1];
		setParameterEstimates(new GaussianEstimate(beta, omega));
		Matrix residualVariance = new Matrix(1,1);
		residualVariance.m_afData[0][0] = 2d;
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(residualVariance));
		oXVector = new Matrix(1, beta.m_iRows);
	}
	
	protected double predictY(SamplePlot plot) {
		Matrix currentBeta = getParametersForThisRealization(plot);
		oXVector.resetMatrix();
		oXVector.m_afData[0][0] = 1d;
		oXVector.m_afData[0][1] = plot.getX();
		double pred = oXVector.multiply(currentBeta).m_afData[0][0];
		pred += getResidualError().m_afData[0][0] * Math.sqrt(plot.getX());
		return pred;
	}

	protected void replaceBeta() {
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		Matrix variance = getParameterEstimates().getVariance();
		setParameterEstimates(new GaussianEstimate(newMean, variance));
	}

}
