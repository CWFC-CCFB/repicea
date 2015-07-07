package repicea.treelogger.europeanbeech;

import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class EuropeanBeechBasicTreeLoggerParameters extends DiameterBasedTreeLoggerParameters {

	public static enum Grade implements TextableEnum {
		IndustryWood("Particle", "Bois industrie"),
		SawlogLowQuality("Sawlog low quality", "Sciage basse qualit\u00E9"),
		SawlogHighQuality("Sawlog high quality", "Sciage haute qualit\u00E9"),
		;

		Grade(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	protected EuropeanBeechBasicTreeLoggerParameters() {
		super(EuropeanBeechBasicTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		String species = getSpeciesName();
		getLogCategories().clear();
		getLogCategories().put(species, categories);
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.SawlogHighQuality, species, 25));
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.SawlogLowQuality, species, 16));
		categories.add(new EuropeanBeechBasicTreeLogCategory(Grade.IndustryWood, species, 10));
	}
	
	
	@Override
	protected String getSpeciesName() {
		return EuropeanBeechBasicTree.Species.EuropeanBeech.toString();
	}

	public static void main(String[] args) {
		EuropeanBeechBasicTreeLoggerParameters params = new EuropeanBeechBasicTreeLoggerParameters();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showInterface(null);
		params.showInterface(null);
	}

}
