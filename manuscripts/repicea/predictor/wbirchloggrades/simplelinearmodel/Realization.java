package repicea.predictor.wbirchloggrades.simplelinearmodel;

class Realization {
	final double trueTau;
	final double estTau;
	final double estVarianceUncorr;
	final double estVarianceCorr;
	final double samplingPart;
	final double modelPart;
	
	Realization(double trueTau, double estTau, double estVarianceUncorr, double estVarianceCorr, double samplingPart, double modelPart) {
		this.trueTau = trueTau;
		this.estTau = estTau;
		this.estVarianceUncorr = estVarianceUncorr;
		this.estVarianceCorr = estVarianceCorr;
		this.samplingPart = samplingPart;
		this.modelPart = modelPart;
	}
	
	Object[] getRecord() {
		Object[] record = new Object[6];
		record[0] = trueTau;
		record[1] = estTau;
		record[2] = estVarianceUncorr;
		record[3] = estVarianceCorr;
		record[4] = samplingPart;
		record[5] = modelPart;
		return record;
	}
	
	
}
