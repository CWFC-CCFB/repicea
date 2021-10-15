package repicea.simulation.metamodel;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.distributions.GaussianDistribution;

public class RichardsChapmanModelWithTimeAndRandomEffectsImplementation extends RichardsChapmanModelWithRandomEffectImplementation {

	public RichardsChapmanModelWithTimeAndRandomEffectsImplementation(HierarchicalStatisticalDataStructure structure, Matrix varCov) {
		super(structure, varCov);
	}

	
	@Override
	double getPrediction(double ageYr, double timeSinceBeginning, double r1) {
		double b1 = getParameters().getValueAt(0, 0);
		double b2 = getParameters().getValueAt(1, 0);
		double b3 = getParameters().getValueAt(2, 0);
		double b4 = getParameters().getValueAt(3, 0);
		double pred = (b1 + b4 * timeSinceBeginning + r1) * Math.pow(1 - Math.exp(-b2 * ageYr), b3);
		return pred;
	}
	
	@Override
	GaussianDistribution getStartingParmEst(double coefVar) {
		Matrix parmEst = new Matrix(6,1);
		parmEst.setValueAt(0, 0, 100d);
		parmEst.setValueAt(1, 0, 0.02);
		parmEst.setValueAt(2, 0, 2d);
		parmEst.setValueAt(3, 0, 0.01d);
		parmEst.setValueAt(4, 0, 200d);
		parmEst.setValueAt(5, 0, .92);

		this.indexRandomEffectVariance = 4;
		this.indexCorrelationParameter = 5;
		
		Matrix varianceDiag = new Matrix(parmEst.m_iRows,1);
		for (int i = 0; i < varianceDiag.m_iRows; i++) {
			varianceDiag.setValueAt(i, 0, Math.pow(parmEst.getValueAt(i, 0) * coefVar, 2d));
		}
		
		GaussianDistribution gd = new GaussianDistribution(parmEst, varianceDiag.matrixDiagonal());
		
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound(0,400));
		bounds.add(new Bound(0.0001, 0.1));
		bounds.add(new Bound(1,6));
		bounds.add(new Bound(0,1));
		bounds.add(new Bound(0,350));
		bounds.add(new Bound(.9,.99));

		return gd;
	}

	
}
