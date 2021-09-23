package repicea.simulation.scriptapi;

import repicea.simulation.ApplicationScaleProvider.ApplicationScale;
import repicea.simulation.climate.REpiceaClimateGenerator.ClimateChangeOption;

public interface Art2014ScriptAPI extends ExtScriptAPI {	
	public void setInitialParameters(int initialDateYr, 
			boolean isStochastic, 
			int nbRealizations, 
			ApplicationScale scale,
			ClimateChangeOption climateChange);	
}
