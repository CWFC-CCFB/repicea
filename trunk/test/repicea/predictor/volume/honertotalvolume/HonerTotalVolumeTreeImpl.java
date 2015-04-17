package repicea.predictor.volume.honertotalvolume;


public class HonerTotalVolumeTreeImpl implements HonerTotalVolumeTree {//, VolumableTree {

	private final HonerTotalVolumeTreeSpecies species;
	private final double dbh;
	private final double height;
	
	protected HonerTotalVolumeTreeImpl(HonerTotalVolumeTreeSpecies species, double dbh, double height) {
		this.species = species;
		this.dbh = dbh;
		this.height = height;
	}
	
	
	@Override
	public double getSquaredDbhCm() {return dbh * dbh;}

	@Override
	public double getHeightM() {return height;}

	@Override
	public HonerTotalVolumeTreeSpecies getHonerSpecies() {return species;}

//	@Override
//	public double getDbhCm() {
//		return dbh;
//	}
//	
//	@Override
//	public VolSpecies getVolumableTreeSpecies() {
//		return VolSpecies.valueOf(species.name());
//	}

}
