package repicea.predictor.thinners.melothinner;

import repicea.simulation.HierarchicalLevel;

class MeloThinnerPlotImpl implements MeloThinnerPlot {

	private final String subjectId;
	private final double plotBasalAreaM2Ha;
	private final double stemDensityHa;
	private final String ecologicalType;
	private final SlopeMRNFClass slopeClass;
	private final double[] aac;
	private final double pred;
	private final double meanPA;
	
	MeloThinnerPlotImpl(String subjectId, 
			double plotBasalAreaM2Ha, 
			double stemDensityHa, 
			String ecologicalType, 
			SlopeMRNFClass slopeClass,
			double[] aac,
			double pred,
			double meanPA) {
		this.subjectId = subjectId;
		this.plotBasalAreaM2Ha = plotBasalAreaM2Ha;
		this.stemDensityHa = stemDensityHa;
		this.ecologicalType = ecologicalType;
		this.slopeClass = slopeClass; 
		this.aac = aac;
		this.pred = pred;
		this.meanPA = meanPA;
	}
		
	@Override
	public String getSubjectId() {return subjectId;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getBasalAreaM2Ha() {return plotBasalAreaM2Ha;}

	@Override
	public double getNumberOfStemsHa() {return stemDensityHa;}

	@Override
	public SlopeMRNFClass getSlopeClass() {return slopeClass;}

	@Override
	public String getEcologicalType() {return ecologicalType;}

	@Override
	public String getCruiseLineID() {return this.getSubjectId().substring(0, 8);}

	double[] getAAC() {return aac;}
	double getPredSurvival() {return pred;}
	double getMeanPA() {return meanPA;}
}
