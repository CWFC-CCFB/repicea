package repicea.predictor.wbirchloggrades.simplelinearmodel;

import java.util.ArrayList;

import repicea.stats.estimates.HorvitzThompsonTauEstimate;

@SuppressWarnings("serial")
class PlotList extends ArrayList<SamplePlot> {

	void setRealization(int id) {
		for (SamplePlot plot : this) {
			plot.setMonteCarloRealizationId(id);
		}
	}
	
	HorvitzThompsonTauEstimate getHorvitzThompsonEstimate(int populationSize) {
		HorvitzThompsonTauEstimate estimate = new HorvitzThompsonTauEstimate(populationSize);
		
		for (SamplePlot plot : this) {
			estimate.addObservation(plot.getY(), 1d/populationSize);
		}
		return estimate;
	}
	
}
