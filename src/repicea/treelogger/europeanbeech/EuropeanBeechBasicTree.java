package repicea.treelogger.europeanbeech;

import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTree;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public interface EuropeanBeechBasicTree extends DiameterBasedTree {

	public static enum Species implements TextableEnum {
		EuropeanBeech("European beech", "H\u00EAtre europ\u00E9en");
		
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
	
}
