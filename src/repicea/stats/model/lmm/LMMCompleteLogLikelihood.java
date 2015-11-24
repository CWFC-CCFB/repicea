package repicea.stats.model.lmm;

import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.data.DataBlock;
import repicea.stats.model.CompositeLogLikelihood;
import repicea.stats.model.IndividualLikelihood;
import repicea.stats.model.IndividualLogLikelihood;

@SuppressWarnings("serial")
public class LMMCompleteLogLikelihood extends CompositeLogLikelihood {

//	final LinearMixedModel model;
//	final InternalLogLikelihood llk;

	public LMMCompleteLogLikelihood(InternalGroupLogLikelihood innerLogLikelihoodFunction, Matrix xValues,	Matrix yValues) {
		super(innerLogLikelihoodFunction, xValues, yValues);
		// TODO Auto-generated constructor stub
	}

//	ModelLogLikelihood(LinearMixedModel model) {
//		this.model = model;
//		llk = new InternalLogLikelihood();
//		llk.setParameterValue(InternalLogLikelihood.ParameterID.MatrixV, model.getMatrixVFunction());
//	}
	

	@Override
	public InternalGroupLogLikelihood getOriginalFunction() {return (InternalGroupLogLikelihood) super.getOriginalFunction();}

	@Override
	public Double getValue() {
		double llkValue = 0;
		Matrix residuals = model.getResiduals();
		Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
		for (String subject : dataBlocks.keySet()) {
			DataBlock db = dataBlocks.get(subject);
			getOriginalFunction().matrixVFunction.setDataBlock(db);
			Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setResiduals(r_i);
			Matrix matrixX_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setXMatrix(matrixX_i);

			llkValue += getOriginalFunction().getValue();
		}
		
		// profile the likelihood
		return -.5 * (llkValue + (model.getDataStructure().getNumberOfObservations() - model.le.getNumberOfParameters()) * Math.log(2* Math.PI));
	}


	@Override
	public Matrix getGradient() {
		Matrix gradient = null;
		Matrix residuals = model.getResiduals();
		Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
		for (String subject : dataBlocks.keySet()) {
			DataBlock db = dataBlocks.get(subject);
			getOriginalFunction().matrixVFunction.setDataBlock(db);
			Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setResiduals(r_i);
			Matrix X_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setXMatrix(X_i);

			if (gradient == null) {
				gradient = getOriginalFunction().getGradient();
			} else {
				gradient = gradient.add(getOriginalFunction().getGradient());
			}
		}
		return gradient.scalarMultiply(-.5);
	}

	@Override
	public Matrix getHessian() {
		Matrix hessian = null;
		Matrix residuals = model.getResiduals();
		Map<String, DataBlock> dataBlocks = model.getDataStructure().getHierarchicalStructure();
		for (String subject : dataBlocks.keySet()) {
			DataBlock db = dataBlocks.get(subject);
			getOriginalFunction().matrixVFunction.setDataBlock(db);
			Matrix r_i = residuals.getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setResiduals(r_i);
			Matrix X_i = model.getDataStructure().getMatrixX().getSubMatrix(db.getIndices(), null);
			getOriginalFunction().setXMatrix(X_i);

			if (hessian == null) {
				hessian = getOriginalFunction().getHessian();
			} else {
				hessian = hessian.add(getOriginalFunction().getHessian());
			}
		}
		return hessian;
	}

}
