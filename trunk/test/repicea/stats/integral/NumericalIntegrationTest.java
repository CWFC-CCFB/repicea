package repicea.stats.integral;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

public class NumericalIntegrationTest {

	
	@Test
    public void TestWithUnivariateLaplacianApproximation() throws Exception {
		
		Random random = new Random();
		LinkFunction logit = new LinkFunction(LinkFunction.Type.Logit);
		double xBeta = -1.5;
		logit.setParameterValue(0, xBeta);
		logit.setVariableValue(0, 1d);
		double mean = 0;
		int nbIter = 1000000;
		double factor = 1d / nbIter;
		double stdDev = 1d;
		for (int i = 0; i < nbIter; i++) {
			logit.setParameterValue(0, xBeta + random.nextGaussian() * stdDev);
			mean += logit.getValue() * factor;
		}

		Matrix lowerCholeskyTriangle = new Matrix(1,1);
		lowerCholeskyTriangle.setValueAt(0, 0, 1d);
		
		System.out.println("Simulated mean =  " + mean);

		logit.setParameterValue(0, xBeta);
		
		List<Integer> parameterIndices = new ArrayList<Integer>();
		parameterIndices.add(0);

		LaplacianApproximation la = new LaplacianApproximation();
		double sum = la.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);

		System.out.println("Mean with Laplace Approximation =  " + sum);
		assertEquals(mean, sum, 5E-3);


		xBeta = 2.5;
		logit.setParameterValue(0, xBeta);
		logit.setVariableValue(0, 1d);
		mean = 0;
		for (int i = 0; i < nbIter; i++) {
			logit.setParameterValue(0, xBeta + random.nextGaussian() * stdDev);
			mean += logit.getValue() * factor;
		}

		System.out.println("Simulated mean =  " + mean);
		logit.setParameterValue(0, xBeta);
		
		sum = la.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);

		System.out.println("Mean with Laplace Approximation =  " + sum);
		assertEquals(mean, sum, 5E-3);
	}


	@Test
    public void TestWithBivariateLaplacianApproximation() throws Exception {
		
		LinkFunction logit = new LinkFunction(LinkFunction.Type.Logit);
		double xBeta = -1.5;
		logit.setParameterValue(0, xBeta);
		logit.setVariableValue(0, 1d);
		logit.setParameterValue(1, 0d);
		logit.setVariableValue(1, 1d);

		Matrix gMatrix = new Matrix(2,2);
		gMatrix.setValueAt(0, 0, 1d);
		gMatrix.setValueAt(1, 0, .5);
		gMatrix.setValueAt(0, 1, .5);
		gMatrix.setValueAt(1, 1, .5);
		
		Matrix lowerCholeskyTriangle = gMatrix.getLowerCholTriangle();
		
		double mean = 0;
		int nbIter = 1000000;
		double factor = 1d / nbIter;
		for (int i = 0; i < nbIter; i++) {
			Matrix u = lowerCholeskyTriangle.multiply(StatisticalUtility.drawRandomVector(lowerCholeskyTriangle.m_iRows, Distribution.Type.GAUSSIAN));
			logit.setParameterValue(0, xBeta + u.getValueAt(0, 0));
			logit.setParameterValue(1, u.getValueAt(1, 0));
			mean += logit.getValue() * factor;
		}
		
		System.out.println("Simulated mean =  " + mean);

		logit.setParameterValue(0, xBeta);
		logit.setParameterValue(1, 0d);
		
		List<Integer> parameterIndices = new ArrayList<Integer>();
		parameterIndices.add(0);
		parameterIndices.add(1);

		LaplacianApproximation la = new LaplacianApproximation();
		double sum = la.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);

		System.out.println("Mean with Laplace Approximation =  " + sum);
		assertEquals(mean, sum, 1E-2);


		xBeta = 2.5;
		logit.setParameterValue(0, xBeta);
		logit.setParameterValue(1, 0d);
		
		mean = 0;
		for (int i = 0; i < nbIter; i++) {
			Matrix u = lowerCholeskyTriangle.multiply(StatisticalUtility.drawRandomVector(lowerCholeskyTriangle.m_iRows, Distribution.Type.GAUSSIAN));
			logit.setParameterValue(0, xBeta + u.getValueAt(0, 0));
			logit.setParameterValue(1, u.getValueAt(1, 0));
			mean += logit.getValue() * factor;
		}

		System.out.println("Simulated mean =  " + mean);
		logit.setParameterValue(0, xBeta);
		logit.setParameterValue(1, 0d);
		
		sum = la.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);

		System.out.println("Mean with Laplace Approximation =  " + sum);
		assertEquals(mean, sum, 1E-2);
	}

	
	@Test
    public void TestWithUnivariateAdaptativeGaussHermiteQuadrature() throws Exception {
		AdaptativeGaussHermiteQuadrature ghq5 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N5);
		AdaptativeGaussHermiteQuadrature ghq10 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N10);
		AdaptativeGaussHermiteQuadrature ghq15 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N15);
		GaussHermiteQuadrature rghq5 = new GaussHermiteQuadrature(NumberOfPoints.N5);
		GaussHermiteQuadrature rghq10 = new GaussHermiteQuadrature(NumberOfPoints.N10);
		GaussHermiteQuadrature rghq15 = new GaussHermiteQuadrature(NumberOfPoints.N15);
		
		Random random = new Random();
		LinkFunction logit = new LinkFunction(LinkFunction.Type.Logit);
		double xBeta = -1.5;
		logit.setParameterValue(0, xBeta);
		logit.setVariableValue(0, 1d);
		double mean = 0;
		int nbIter = 1000000;
		double factor = 1d / nbIter;
		double stdDev = 1d;
		for (int i = 0; i < nbIter; i++) {
			logit.setParameterValue(0, xBeta + random.nextGaussian() * stdDev);
			mean += logit.getValue() * factor;
		}

		Matrix lowerCholeskyTriangle = new Matrix(1,1);
		lowerCholeskyTriangle.setValueAt(0, 0, 1d);
		
		System.out.println("Simulated mean =  " + mean);

		logit.setParameterValue(0, xBeta);
		
		
		List<Integer> parameterIndices = new ArrayList<Integer>();
		parameterIndices.add(0);

		double sum2 = rghq5.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		double sum = ghq5.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		
		System.out.println("Mean with 5 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-3);

		logit.setParameterValue(0, xBeta);
		sum2 = rghq10.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		sum = ghq10.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		
		System.out.println("Mean with 10 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-3);

		logit.setParameterValue(0, xBeta);
		sum2 = rghq15.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		sum = ghq15.getIntegralApproximation(logit, parameterIndices, lowerCholeskyTriangle);
		
		System.out.println("Mean with 15 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-3);

	}

	@Test
    public void TestWithTwoDimensionAdaptativeGaussHermiteQuadratureAndStatisticalFunction() throws Exception {
		AdaptativeGaussHermiteQuadrature ghq5 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N5);
		AdaptativeGaussHermiteQuadrature ghq10 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N10);
		AdaptativeGaussHermiteQuadrature ghq15 = new AdaptativeGaussHermiteQuadrature(NumberOfPoints.N15);
		GaussHermiteQuadrature rghq5 = new GaussHermiteQuadrature(NumberOfPoints.N5);
		GaussHermiteQuadrature rghq10 = new GaussHermiteQuadrature(NumberOfPoints.N10);
		GaussHermiteQuadrature rghq15 = new GaussHermiteQuadrature(NumberOfPoints.N15);

		Matrix matG = new Matrix(2,2);
		matG.setValueAt(0, 0, 1d);
		matG.setValueAt(1, 0, .2);
		matG.setValueAt(0, 1, .2);
		matG.setValueAt(1, 1, .5);
		Matrix chol = matG.getLowerCholTriangle();
		
		
		LinkFunction linkFunction = new LinkFunction(Type.Logit);
		
		double xBeta = -1;
		linkFunction.setParameterValue(0, 1d);
		linkFunction.setVariableValue(0, xBeta);
		linkFunction.setParameterValue(1, 1d);
		linkFunction.setVariableValue(1, 1d);
		linkFunction.setParameterValue(2, 2d);
		linkFunction.setVariableValue(2, 1d);
		
		double mean = 0;
		int nbIter = 1000000;
		double factor = 1d / nbIter;
		double oriVal1 = linkFunction.getParameterValue(1);
		double oriVal2 = linkFunction.getParameterValue(2);
		for (int i = 0; i < nbIter; i++) {
			Matrix u = chol.multiply(StatisticalUtility.drawRandomVector(chol.m_iRows, Distribution.Type.GAUSSIAN));
			linkFunction.setParameterValue(1, oriVal1 + u.getValueAt(0, 0));
			linkFunction.setParameterValue(2, oriVal2 + u.getValueAt(1, 0));
			mean += linkFunction.getValue() * factor;
		}
		
		System.out.println("Simulated mean =  " + mean);

		linkFunction.setParameterValue(1, oriVal1);
		linkFunction.setParameterValue(2, oriVal2);

		List<Integer> indices = new ArrayList<Integer>();
		indices.add(1);
		indices.add(2);
		double sum2 = rghq5.getIntegralApproximation(linkFunction, indices, chol);
		double sum = ghq5.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 5 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-2);

		linkFunction.setParameterValue(1, oriVal1);
		linkFunction.setParameterValue(2, oriVal2);
		
		sum2 = rghq10.getIntegralApproximation(linkFunction, indices, chol);
		sum = ghq10.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 10 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-2);

		linkFunction.setParameterValue(1, oriVal1);
		linkFunction.setParameterValue(2, oriVal2);

		sum2 = rghq15.getIntegralApproximation(linkFunction, indices, chol);
		sum = ghq15.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 15 points =  " + sum + " compared with " + sum2);
		assertEquals(mean, sum, 1E-2);

	}

	
	
	@Test
    public void TestWithGaussHermiteQuadrature() throws Exception {
		GaussHermiteQuadrature ghq5 = new GaussHermiteQuadrature(NumberOfPoints.N5);
		GaussHermiteQuadrature ghq10 = new GaussHermiteQuadrature(NumberOfPoints.N10);
		GaussHermiteQuadrature ghq15 = new GaussHermiteQuadrature(NumberOfPoints.N15);
		
		Random random = new Random();
		double xBeta = -1.5;
		double mean = 0;
		int nbIter = 1000000;
		double eta;
		double factor = 1d / nbIter;
		double stdDev = 1d;
		for (int i = 0; i < nbIter; i++) {
			eta = xBeta + random.nextGaussian() * stdDev;
			mean += Math.exp(eta) / (1 + Math.exp(eta)) * factor;
		}

		System.out.println("Simulated mean =  " + mean);

		double sum = 0;
		double value;
		for (int i = 0; i < ghq5.getXValues().size(); i++) {
			eta = Math.exp(xBeta + ghq5.getXValues().get(i) * stdDev * Math.sqrt(2d));
			value = 1d / Math.sqrt(Math.PI) * eta / (1 + eta) * ghq5.getWeights().get(i);
			sum += value;
		}
		
		System.out.println("Mean with 5 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

		sum = 0;
		for (int i = 0; i < ghq10.getXValues().size(); i++) {
			eta = Math.exp(xBeta + ghq10.getXValues().get(i) * stdDev * Math.sqrt(2d));
			value = 1d / Math.sqrt(Math.PI) * eta / (1 + eta) * ghq10.getWeights().get(i);
			sum += value;
		}
		
		System.out.println("Mean with 10 points =  " + sum);
		assertEquals(mean, sum, 1E-3);
		
		sum = 0;
		for (int i = 0; i < ghq15.getXValues().size(); i++) {
			eta = Math.exp(xBeta + ghq15.getXValues().get(i) * stdDev * Math.sqrt(2d));
			value = 1d / Math.sqrt(Math.PI) * eta / (1 + eta) * ghq15.getWeights().get(i);
			sum += value;
		}
		
		System.out.println("Mean with 15 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

	}

	
	@Test
    public void TestWithTwoDimensionGaussHermiteQuadratureAndStatisticalFunction() throws Exception {
		GaussHermiteQuadrature ghq5 = new GaussHermiteQuadrature(NumberOfPoints.N5);
		GaussHermiteQuadrature ghq10 = new GaussHermiteQuadrature(NumberOfPoints.N10);
		GaussHermiteQuadrature ghq15 = new GaussHermiteQuadrature(NumberOfPoints.N15);
		
		Matrix matG = new Matrix(2,2);
		matG.setValueAt(0, 0, 1d);
		matG.setValueAt(1, 0, .2);
		matG.setValueAt(0, 1, .2);
		matG.setValueAt(1, 1, .5);
		Matrix chol = matG.getLowerCholTriangle();
		
		
		LinkFunction linkFunction = new LinkFunction(Type.Logit);
//		LinearStatisticalExpression eta = new LinearStatisticalExpression();
//		linkFunction.setParameterValue(LFParameter.Eta, eta);
		double xBeta = -1;
		linkFunction.setParameterValue(0, 1d);
		linkFunction.setVariableValue(0, xBeta);
		linkFunction.setParameterValue(1, 0d);
		linkFunction.setVariableValue(1, 1d);
		linkFunction.setParameterValue(2, .3);
		linkFunction.setVariableValue(2, 1d);
		
		double mean = 0;
		int nbIter = 1000000;
		double factor = 1d / nbIter;
		double oriVal1 = linkFunction.getParameterValue(1);
		double oriVal2 = linkFunction.getParameterValue(2);
		for (int i = 0; i < nbIter; i++) {
			Matrix u = chol.multiply(StatisticalUtility.drawRandomVector(chol.m_iRows, Distribution.Type.GAUSSIAN));
			linkFunction.setParameterValue(1, oriVal1 + u.getValueAt(0, 0));
			linkFunction.setParameterValue(2, oriVal2 + u.getValueAt(1, 0));
			mean += linkFunction.getValue() * factor;
		}
		
		System.out.println("Simulated mean =  " + mean);


		linkFunction.setParameterValue(1, oriVal1);
		linkFunction.setParameterValue(2, oriVal2);

		List<Integer> indices = new ArrayList<Integer>();
		indices.add(1);
		indices.add(2);
		double sum = ghq5.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 5 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

		
		sum = ghq10.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 10 points =  " + sum);
		assertEquals(mean, sum, 1E-3);


		sum = ghq15.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 15 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

	}

	
	@Test
    public void TestWithGaussHermiteQuadratureAndStatisticalFunction() throws Exception {
		GaussHermiteQuadrature ghq5 = new GaussHermiteQuadrature(NumberOfPoints.N5);
		GaussHermiteQuadrature ghq10 = new GaussHermiteQuadrature(NumberOfPoints.N10);
		GaussHermiteQuadrature ghq15 = new GaussHermiteQuadrature(NumberOfPoints.N15);
		
		Random random = new Random();
		LinkFunction linkFunction = new LinkFunction(Type.Logit);
		double xBeta = -1.5;
		linkFunction.setParameterValue(0, 1d);
		linkFunction.setVariableValue(0, xBeta);
		linkFunction.setParameterValue(1, 0d);
		linkFunction.setVariableValue(1, 1d);		// intercept random effect
		
		double mean = 0;
		int nbIter = 1000000;
		double etaValue;
		double factor = 1d / nbIter;
		double stdDev = 1d;
		for (int i = 0; i < nbIter; i++) {
			etaValue = xBeta + random.nextGaussian() * stdDev;
			mean += Math.exp(etaValue) / (1 + Math.exp(etaValue)) * factor;
		}

		System.out.println("Simulated mean =  " + mean);

		List<Integer> indices = new ArrayList<Integer>();
		indices.add(1);
		Matrix chol = new Matrix(1,1);
		chol.setValueAt(0, 0, stdDev);
		
		double sum = ghq5.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 5 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

		sum = ghq10.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 10 points =  " + sum);
		assertEquals(mean, sum, 1E-3);
		
		sum = ghq15.getIntegralApproximation(linkFunction, indices, chol);
		
		System.out.println("Mean with 15 points =  " + sum);
		assertEquals(mean, sum, 1E-3);

	}


	
	@Test
    public void TestWithGaussLegendreQuadratureBetweenMinus1AndPlus1() throws Exception {
		GaussLegendreQuadrature ghq2 = new GaussLegendreQuadrature(NumberOfPoints.N2);
		GaussLegendreQuadrature ghq3 = new GaussLegendreQuadrature(NumberOfPoints.N3);
		GaussLegendreQuadrature ghq4 = new GaussLegendreQuadrature(NumberOfPoints.N4);
		GaussLegendreQuadrature ghq5 = new GaussLegendreQuadrature(NumberOfPoints.N5);

		double trueMean = (1d/3 + 1d/2) - (-1d/3 + 1d/2); 
		
		System.out.println("Function x^2 + x between [-1,1] =  " + trueMean);

		double sum = 0;
		double value, point;
		for (int i = 0; i < ghq2.getXValues().size(); i++) {
			point = ghq2.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq2.getWeights().get(i);
			sum += value * ghq2.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 2 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		
		sum = 0;
		for (int i = 0; i < ghq3.getXValues().size(); i++) {
			point = ghq3.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq3.getWeights().get(i);
			sum += value * ghq3.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 3 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		sum = 0;
		for (int i = 0; i < ghq4.getXValues().size(); i++) {
			point = ghq4.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq4.getWeights().get(i);
			sum += value * ghq4.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 4 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		sum = 0;
		for (int i = 0; i < ghq5.getXValues().size(); i++) {
			point = ghq5.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq5.getWeights().get(i);
			sum += value * ghq5.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 5 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);
	}


	
	@Test
    public void TestWithGaussLegendreQuadratureBetweenOtherBounds() throws Exception {
		double upperBound = 5;
		double lowerBound = -2;
		
		GaussLegendreQuadrature ghq2 = new GaussLegendreQuadrature(NumberOfPoints.N2);
		ghq2.setLowerBound(lowerBound);
		ghq2.setUpperBound(upperBound);
		GaussLegendreQuadrature ghq3 = new GaussLegendreQuadrature(NumberOfPoints.N3);
		ghq3.setLowerBound(lowerBound);
		ghq3.setUpperBound(upperBound);
		GaussLegendreQuadrature ghq4 = new GaussLegendreQuadrature(NumberOfPoints.N4);
		ghq4.setLowerBound(lowerBound);
		ghq4.setUpperBound(upperBound);
		GaussLegendreQuadrature ghq5 = new GaussLegendreQuadrature(NumberOfPoints.N5);
		ghq5.setLowerBound(lowerBound);
		ghq5.setUpperBound(upperBound);

		double trueMean = (Math.pow(upperBound, 3d)/3 + Math.pow(upperBound, 2d)/2) - (Math.pow(lowerBound, 3d)/3 + Math.pow(lowerBound, 2d)/2); 
		System.out.println("Function x^2 + x between [" + lowerBound + "," + upperBound + "] =  " + trueMean);

		double sum = 0;
		double value, point;
		for (int i = 0; i < ghq2.getXValues().size(); i++) {
			point = ghq2.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq2.getWeights().get(i);
			sum += value * ghq2.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 2 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		
		sum = 0;
		for (int i = 0; i < ghq3.getXValues().size(); i++) {
			point = ghq3.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq3.getWeights().get(i);
			sum += value * ghq3.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 3 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		sum = 0;
		for (int i = 0; i < ghq4.getXValues().size(); i++) {
			point = ghq4.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq4.getWeights().get(i);
			sum += value * ghq4.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 4 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);

		sum = 0;
		for (int i = 0; i < ghq5.getXValues().size(); i++) {
			point = ghq5.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			value *= ghq5.getWeights().get(i);
			sum += value * ghq5.getRescalingFactors().get(i);
		}
		
		System.out.println("Mean with 5 points =  " + sum);
		assertEquals(trueMean, sum, 1E-8);
	}

	@Test
    public void TestWithTrapezoidalRule() throws Exception {
		double lowerBound = -1;
		double upperBound = 5;
		double trueMean = (Math.pow(upperBound, 3d)/3 + Math.pow(upperBound, 2d)/2) - (Math.pow(lowerBound, 3d)/3 + Math.pow(lowerBound, 2d)/2); 
		System.out.println("Function x^2 + x between [" + lowerBound + "," + upperBound + "] =  " + trueMean);

		NumericalIntegrationMethod trapezoidalRule = new TrapezoidalRule(.05d);
		trapezoidalRule.setLowerBound(lowerBound);
		trapezoidalRule.setUpperBound(upperBound);

		List<Double> yValues = new ArrayList<Double>();
		
		double value;
		double point;
		for (int i = 0; i < trapezoidalRule.getXValues().size(); i++) {
			point = trapezoidalRule.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			yValues.add(value);
		}
		
		Matrix yMatrix = new Matrix(yValues);
		Matrix weights = new Matrix(trapezoidalRule.getWeights());
		Matrix rescalingFactors = new Matrix(trapezoidalRule.getRescalingFactors());
		
		double sum = rescalingFactors.elementWiseMultiply(weights).elementWiseMultiply(yMatrix).getSumOfElements();
		
		System.out.println("Mean with TrapezoidalRule instance  =  " + sum);
		assertEquals(trueMean, sum, 1E-2);
	}

	
	@Test
    public void TestWithCompositeSimpsonRule() throws Exception {
		double lowerBound = -1;
		double upperBound = 5;
		double trueMean = (Math.pow(upperBound, 3d)/3 + Math.pow(upperBound, 2d)/2) - (Math.pow(lowerBound, 3d)/3 + Math.pow(lowerBound, 2d)/2); 
		System.out.println("Function x^2 + x between [" + lowerBound + "," + upperBound + "] =  " + trueMean);

		NumericalIntegrationMethod trapezoidalRule = new CompositeSimpsonRule(16);
		trapezoidalRule.setLowerBound(lowerBound);
		trapezoidalRule.setUpperBound(upperBound);

		List<Double> yValues = new ArrayList<Double>();
		
		double value;
		double point;
		for (int i = 0; i < trapezoidalRule.getXValues().size(); i++) {
			point = trapezoidalRule.getXValues().get(i);
			value = Math.pow(point, 2d) + point;
			yValues.add(value);
		}
		
		Matrix yMatrix = new Matrix(yValues);
		Matrix weights = new Matrix(trapezoidalRule.getWeights());
		Matrix rescalingFactors = new Matrix(trapezoidalRule.getRescalingFactors());
		
		double sum = rescalingFactors.elementWiseMultiply(weights).elementWiseMultiply(yMatrix).getSumOfElements();
		
		System.out.println("Mean with CompositeSimpsonRule instance =  " + sum);
		assertEquals(trueMean, sum, 1E-2);
	}

}
