package repicea.predictor.wbirchloggrades.simplelinearmodel;

import java.util.ArrayList;
import java.util.Random;

import repicea.stats.estimates.PopulationTotalEstimate;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

@SuppressWarnings("serial")
class PlotList extends ArrayList<SamplePlot> {

	static final Random RANDOM = new Random();
	
	void setRealization(int id) {
		for (SamplePlot plot : this) {
			plot.setMonteCarloRealizationId(id);
		}
	}
	
	PopulationTotalEstimate getHorvitzThompsonEstimate(int populationSize) {
		PopulationTotalEstimate estimate = new PopulationTotalEstimate();
		
		for (SamplePlot plot : this) {
			estimate.addObservation(new PopulationUnitWithUnequalInclusionProbability(plot.getY(), 1d/populationSize));
		}
		return estimate;
	}
	
	PlotList getBootstrapSample() throws CloneNotSupportedException {
		PlotList newList = new PlotList();
		for (int i = 0; i < size(); i++) {
			int plotIndex = (int) Math.floor(RANDOM.nextDouble() * size());
			newList.add(get(plotIndex).clone());
		}
		return newList;
	}
	
}
