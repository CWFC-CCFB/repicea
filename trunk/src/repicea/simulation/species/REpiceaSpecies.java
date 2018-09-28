package repicea.simulation.species;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class REpiceaSpecies {

	public static enum Species implements TextableEnum {
		Fagus_sylvatica("European beech", "H\u00EAtre commun"),
		Pinus_pinaster("Maritime pine", "Pin maritime"),
		Betula_papyrifera("White birch", "Bouleau blanc");
		
		Species(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
//	private final Species speciesEnum;
//	
//	
//	private REpiceaSpecies(Species speciesEnum) {
//		this.speciesEnum = speciesEnum;
//	}

}
