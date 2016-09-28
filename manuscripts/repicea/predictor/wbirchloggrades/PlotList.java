package repicea.predictor.wbirchloggrades;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.estimates.HorvitzThompsonTauEstimate;

@SuppressWarnings("serial")
class PlotList extends ArrayList<WBirchLogGradesStandImpl> {

	void setRealization(int id) {
		for (WBirchLogGradesStandImpl plot : this) {
			plot.setMonteCarloRealizationId(id);
		}
	}
	
	HorvitzThompsonTauEstimate getHorvitzThompsonEstimate(int populationSize) {
		HorvitzThompsonTauEstimate estimate = new HorvitzThompsonTauEstimate(populationSize);
		
		for (WBirchLogGradesStandImpl plot : this) {
			Matrix plotTotal = new Matrix(7,1);
			for (WBirchLogGradesTreeImpl tree : plot.getTrees().values()) {
				plotTotal = plotTotal.add(tree.getRealizedValues());
			}
			estimate.addObservation(plotTotal, 1d/populationSize);
		}
		return estimate;
	}
	
}
