package repicea.treelogger.diameterbasedtreelogger;

import java.awt.Container;
import java.awt.Window;

import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public abstract class DiameterBasedTreeLoggerParameters extends TreeLoggerParameters<DiameterBasedTreeLogCategory> {

	public static enum MessageID implements TextableEnum {
		WoodProducts("Wood products", "Produits bois"),
		ProductType("Product type", "Type de produit"),
		;

		MessageID(String englishText, String frenchText) {
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
	
	private transient DiameterBasedTreeLoggerParametersDialog guiInterface;

	protected DiameterBasedTreeLoggerParameters(Class<? extends TreeLogger<?,?>> clazz) {
		super(EuropeanBeechBasicTreeLogger.class);
		initializeDefaultLogCategories();
	}

	@Override
	protected abstract void initializeDefaultLogCategories(); 

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public DiameterBasedTreeLoggerParametersDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new DiameterBasedTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}
	
	protected abstract String getSpeciesName();
	
//	public static void main(String[] args) {
//		EuropeanBeechBasicTreeLoggerParameters params = new EuropeanBeechBasicTreeLoggerParameters();
//		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
//		params.showInterface(null);
//		params.showInterface(null);
//	}

}
