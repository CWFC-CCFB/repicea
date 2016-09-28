package repicea.predictor.wbirchloggrades;

import repicea.math.Matrix;

class Realization {
	final Matrix trueTau;
	final Matrix estTau;
	final Matrix estVarianceUncorr;
	final Matrix estVarianceCorr;
	final Matrix samplingPart;
	final Matrix modelPart;
	
	Realization(Matrix trueTau, Matrix estTau, Matrix estVarianceUncorr, Matrix estVarianceCorr, Matrix samplingPart, Matrix modelPart) {
		this.trueTau = trueTau;
		this.estTau = estTau;
		this.estVarianceUncorr = estVarianceUncorr;
		this.estVarianceCorr = estVarianceCorr;
		this.samplingPart = samplingPart;
		this.modelPart = modelPart;
	}
	
	Object[] getRecord() {
		Object[] record = new Object[6*7];
		record[0] = trueTau.m_afData[0][0];
		record[1] = estTau.m_afData[0][0];
		record[2] = estVarianceUncorr.m_afData[0][0];
		record[3] = estVarianceCorr.m_afData[0][0];
		record[4] = samplingPart.m_afData[0][0];
		record[5] = modelPart.m_afData[0][0];

		record[6] = trueTau.m_afData[1][0];
		record[7] = estTau.m_afData[1][0];
		record[8] = estVarianceUncorr.m_afData[1][1];
		record[9] = estVarianceCorr.m_afData[1][1];
		record[10] = samplingPart.m_afData[1][1];
		record[11] = modelPart.m_afData[1][1];

		record[12] = trueTau.m_afData[2][0];
		record[13] = estTau.m_afData[2][0];
		record[14] = estVarianceUncorr.m_afData[2][2];
		record[15] = estVarianceCorr.m_afData[2][2];
		record[16] = samplingPart.m_afData[2][2];
		record[17] = modelPart.m_afData[2][2];
		
		record[18] = trueTau.m_afData[3][0];
		record[19] = estTau.m_afData[3][0];
		record[20] = estVarianceUncorr.m_afData[3][3];
		record[21] = estVarianceCorr.m_afData[3][3];
		record[22] = samplingPart.m_afData[3][3];
		record[23] = modelPart.m_afData[3][3];

		record[24] = trueTau.m_afData[4][0];
		record[25] = estTau.m_afData[4][0];
		record[26] = estVarianceUncorr.m_afData[4][4];
		record[27] = estVarianceCorr.m_afData[4][4];
		record[28] = samplingPart.m_afData[4][4];
		record[29] = modelPart.m_afData[4][4];
		
		record[30] = trueTau.m_afData[5][0];
		record[31] = estTau.m_afData[5][0];
		record[32] = estVarianceUncorr.m_afData[5][5];
		record[33] = estVarianceCorr.m_afData[5][5];
		record[34] = samplingPart.m_afData[5][5];
		record[35] = modelPart.m_afData[5][5];

		record[36] = trueTau.m_afData[6][0];
		record[37] = estTau.m_afData[6][0];
		record[38] = estVarianceUncorr.m_afData[6][6];
		record[39] = estVarianceCorr.m_afData[6][6];
		record[40] = samplingPart.m_afData[6][6];
		record[41] = modelPart.m_afData[6][6];

		return record;
	}
	
	
}
