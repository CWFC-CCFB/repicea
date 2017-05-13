package repicea.predictor.thinners.melothinner;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.covariateproviders.standlevel.LandOwnershipProvider;

class MeloThinnerPlotImpl implements MeloThinnerPlot, LandOwnershipProvider {

	private final String subjectId;
	private final double plotBasalAreaM2Ha;
	private final double stemDensityHa;
	private final String ecologicalType;
	private final SlopeMRNFClass slopeClass;
	private final int year0;
	private final int year1;
	private final double[] aac;
	private final double pred;
	private final double meanPA;
	private final QuebecForestRegion region;
	private final LandOwnership ownership;
	
		MeloThinnerPlotImpl(String subjectId, 
			double plotBasalAreaM2Ha, 
			double stemDensityHa, 
			String ecologicalType, 
			SlopeMRNFClass slopeClass,
			int year0,
			int year1,
			int regionCode,
			String ownershipCode,
			double[] aac,
			double pred,
			double meanPA) {
		this.subjectId = subjectId;
		this.plotBasalAreaM2Ha = plotBasalAreaM2Ha;
		this.stemDensityHa = stemDensityHa;
		this.ecologicalType = ecologicalType;
		this.slopeClass = slopeClass; 
		this.year0 = year0;
		this.year1 = year1;
		this.aac = aac;
		this.pred = pred;
		this.meanPA = meanPA;
		this.region = QuebecForestRegion.getRegion(regionCode);
		this.ownership = LandOwnership.getLandOwnership(ownershipCode);
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
	int getYear0() {return year0;}
	int getYear1() {return year1;}

	@Override
	public QuebecForestRegion getQuebecForestRegion() {return region;}

	@Override
	public LandOwnership getLandOwnership() {return ownership;}
}
