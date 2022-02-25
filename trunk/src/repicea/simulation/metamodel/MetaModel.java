/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */

package repicea.simulation.metamodel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;

import com.cedarsoftware.util.io.JsonWriter;

import repicea.io.Saveable;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.data.DataSet;
import repicea.stats.data.Observation;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.mcmc.MetropolisHastingsParameters;
import repicea.util.REpiceaLogManager;


/**
 * A package class that handles the data set and fits the meta model. It
 * implements Runnable for an eventual multi-threaded implementation.
 * 
 * @author Mathieu Fortin - December 2020
 */
public class MetaModel implements Saveable {
	
		public class MetaDataHelper {
						
			public MetaDataHelper() {
			}
			
			public MetaModelMetaData generate() {
				
				MetaModelMetaData data = new MetaModelMetaData();
				
				data.growth.geoDomain = MetaModel.this.geoDomain;
				data.growth.dataSource = MetaModel.this.dataSource;
				
				if (!MetaModel.this.scriptResults.isEmpty()) {						
				
					List<Integer> srKeys = new ArrayList<Integer>(MetaModel.this.scriptResults.keySet());
					
					Collections.sort(srKeys);
					
					boolean firstElement = true;
					for (Integer key : srKeys) {
						ScriptResult result = MetaModel.this.scriptResults.get(key);
						if (firstElement) {
							// fill in data that is constant 	
							data.growth.nbRealizations = result.getNbRealizations();
							data.growth.climateChangeOption = ((Enum)result.climateChangeScenario).name();
							data.growth.growthModel = result.growthModel;							
						}
						
						// DateYrFieldName && upscaling
						List<Integer> temp = new ArrayList<Integer>();					 
						for (int i = 0; i < result.getDataSet().getNumberOfObservations(); i++) {
							if (i == 0)
								data.growth.upscaling.put(key, (String)result.getDataSet().getValueAt(i, ScriptResult.VarianceEstimatorType));
								
							Integer value = (Integer)result.getDataSet().getValueAt(i, ScriptResult.DateYrFieldName); 
							if (!temp.contains(value))
								temp.add(value);							
						}
						data.growth.dataSourceYears.put(key, temp);
						
						// nbPlots
						data.growth.nbPlots.put(key, result.nbPlots);													
					}
				}
				
				data.fit.timeStamp = MetaModel.this.lastFitTimeStamp;
				data.fit.outputType = MetaModel.this.model.getSelectedOutputType();
				data.fit.fitModel = MetaModel.this.model.getModelImplementation().toString();
				data.fit.stratumGroup = MetaModel.this.stratumGroup;			
				
				return data;
			}
		}	
		
	
	static {
		repicea.serial.xml.XmlSerializerChangeMonitor.registerClassNameChange(
				"repicea.simulation.metamodel.MetaModel$InnerModel",
				"repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation");
		repicea.serial.xml.XmlSerializerChangeMonitor.registerClassNameChange(
				"repicea.simulation.metamodel.DataBlockWrapper",
				"repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation$DataBlockWrapper");
	}

//	protected static VerboseLevel Verbose = VerboseLevel.Minimum; 

//	public static enum VerboseLevel {None,
//		Minimum,
//		Medium,
//		High;
//		
//		public boolean shouldVerboseAtThisLevel(VerboseLevel level) {
//			if (ordinal() >= level.ordinal()) {
//				return true;
//			} else {
//				return false;
//			}
//		}
//	}

	public static enum ModelImplEnum {
		SimpleSlope(true), SimplifiedChapmanRichards(true), ChapmanRichards(true),
		ChapmanRichardsWithRandomEffect(false), ChapmanRichardsDerivative(true),
		ChapmanRichardsDerivativeWithRandomEffect(false);

		private static List<ModelImplEnum> ModelsWithoutRandomEffects;
		private static Map<ModelImplEnum, ModelImplEnum> MatchingModelsWithRandomEffects;

		final boolean modelWithoutRandomEffect;

		ModelImplEnum(boolean modelWithoutRandomEffect) {
			this.modelWithoutRandomEffect = modelWithoutRandomEffect;
		}

		public static List<ModelImplEnum> getModelsWithoutRandomEffects() {
			if (ModelsWithoutRandomEffects == null) {
				ModelsWithoutRandomEffects = new ArrayList<ModelImplEnum>();
				for (ModelImplEnum e : ModelImplEnum.values()) {
					if (e.modelWithoutRandomEffect) {
						ModelsWithoutRandomEffects.add(e);
					}
				}
			}
			return ModelsWithoutRandomEffects;
		}

		private static Map<ModelImplEnum, ModelImplEnum> getMatchingModelsWithRandomEffects() {
			if (MatchingModelsWithRandomEffects == null) {
				MatchingModelsWithRandomEffects = new HashMap<ModelImplEnum, ModelImplEnum>();
				MatchingModelsWithRandomEffects.put(ChapmanRichards, ChapmanRichardsWithRandomEffect);
				MatchingModelsWithRandomEffects.put(ChapmanRichardsDerivative,
						ChapmanRichardsDerivativeWithRandomEffect);
			}
			return MatchingModelsWithRandomEffects;
		}

		public static ModelImplEnum getMatchingModelWithRandomEffects(ModelImplEnum modelImplEnum) {
			return getMatchingModelsWithRandomEffects().get(modelImplEnum);
		}

	}

//	static class SimulationParameters implements Cloneable {
//		protected int nbBurnIn = 5000;
//		protected int nbRealizations = 500000 + nbBurnIn;
//		protected int nbInternalIter = 10000;
//		protected int oneEach = 50;
//		protected int nbInitialGrid = 10000;	
//		
//		SimulationParameters() {}
//		
//		@Override
//		public SimulationParameters clone() {
//			try {
//				return (SimulationParameters) super.clone();
//			} catch (CloneNotSupportedException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//	}

	protected MetropolisHastingsParameters mhSimParms;
	protected final Map<Integer, ScriptResult> scriptResults;
	protected AbstractModelImplementation model;
	private final String stratumGroup;
	private DataSet modelComparison;
	protected final String geoDomain;
	protected final String dataSource;
	public Date lastFitTimeStamp;

	/**
	 * Constructor.
	 * 
	 * @param stratumGroup a String representing the stratum group
	 */
	public MetaModel(String stratumGroup, String geoDomain, String dataSource) {
		this.stratumGroup = stratumGroup;
		this.geoDomain = geoDomain;
		this.dataSource = dataSource;
		
		scriptResults = new ConcurrentHashMap<Integer, ScriptResult>();
		setDefaultSettings();
	}

	private void setDefaultSettings() {
		mhSimParms = new MetropolisHastingsParameters();
	}

	/**
	 * Provide the stratum group for this mate-model.
	 * 
	 * @return a String
	 */
	public String getStratumGroup() {
		return stratumGroup;
	}

	/**
	 * Return the state of the model. The model can be used if it has converged.
	 * 
	 * @return
	 */
	public boolean hasConverged() {
		return model != null ? model.hasConverged() : false;
	}

	void add(int initialAge, ScriptResult result) {
		boolean canBeAdded;
		if (scriptResults.isEmpty()) {
			canBeAdded = true;
		} else {
			ScriptResult previousResult = scriptResults.values().iterator().next();
			if (previousResult.isCompatible(result)) {
				canBeAdded = true;
			} else {
				canBeAdded = false;
			}
		}
		if (canBeAdded) {
			scriptResults.put(initialAge, result);
			model = null; // so that converge is set to false by default	
		} else {
			throw new InvalidParameterException(
					"The result parameter is not compatible with previous results in the map!");
		}
	}

	private AbstractModelImplementation getInnerModel(String outputType, ModelImplEnum modelImplEnum)
			throws StatisticalDataException {
		AbstractModelImplementation model;
		switch (modelImplEnum) {
		case SimpleSlope:
			model = new SimpleSlopeModelImplementation(outputType, this);
			break;
		case SimplifiedChapmanRichards:
			model = new SimplifiedChapmanRichardsModelImplementation(outputType, this);
			break;
		case ChapmanRichards:
			model = new ChapmanRichardsModelImplementation(outputType, this);
			break;
		case ChapmanRichardsWithRandomEffect:
			model = new ChapmanRichardsModelWithRandomEffectImplementation(outputType, this);
			break;
		case ChapmanRichardsDerivative:
			model = new ChapmanRichardsDerivativeModelImplementation(outputType, this);
			break;
		case ChapmanRichardsDerivativeWithRandomEffect:
			model = new ChapmanRichardsDerivativeModelWithRandomEffectImplementation(outputType, this);
			break;
		default:
			throw new InvalidParameterException(
					"This ModelImplEnum " + modelImplEnum.name() + " has not been implemented yet!");
		}
		return model;
	}

	/**
	 * Return the possible output types given what they are in the ScriptResult
	 * instances.
	 * 
	 * @return a List of String
	 * @throws MetaModelException
	 */
	public List<String> getPossibleOutputTypes() {
		return getPossibleOutputTypes(scriptResults);
	}

	protected static List<String> getPossibleOutputTypes(Map<Integer, ScriptResult> scriptResults) {
		List<String> possibleOutputTypes = new ArrayList<String>();
		if (!scriptResults.isEmpty()) {
			ScriptResult scriptRes = scriptResults.values().iterator().next();
			possibleOutputTypes.addAll(scriptRes.getOutputTypes());
		}
		return possibleOutputTypes;
	}

	static class InnerWorker extends Thread implements Comparable<InnerWorker> {

		final AbstractModelImplementation ami;
		double prob;

		InnerWorker(AbstractModelImplementation ami) {
			super(ami);
			this.ami = ami;
			setName(ami.getModelImplementation().name());
		}

		@Override
		public int compareTo(InnerWorker o) {
			if (prob > o.prob) {
				return -1;
			} else if (prob == o.prob) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private InnerWorker performModelSelection(List<InnerWorker> innerWorkers) {
		double sumProb = 0;
		List<InnerWorker> newList = new ArrayList<InnerWorker>();
		for (InnerWorker w : innerWorkers) {
			if (w.ami.hasConverged()) {
				newList.add(w);
				sumProb += Math.exp(w.ami.mh.getLogPseudomarginalLikelihood());
				REpiceaLogManager.logMessage(MetaModelManager.LoggerName, Level.INFO, "Meta-model " + stratumGroup,
						"Result for the implementation " + w.ami.getModelImplementation().name());
			}
		}
		DataSet d = new DataSet(Arrays.asList(new String[] { "ModelImplementation", "LPML", "Prob" }));
		for (InnerWorker w : newList) {
			w.prob = Math.exp(w.ami.mh.getLogPseudomarginalLikelihood()) / sumProb;
			d.addObservation(new Object[] { w.ami.getModelImplementation().name(),
					w.ami.mh.getLogPseudomarginalLikelihood(), w.prob });
//			System.out.println("Implementation " + w.ami.getModelImplementation().name() + ": " + w.prob);
		}
		modelComparison = d;
		Collections.sort(newList);
		return newList.get(0);
	}

	/**
	 * Fit the meta-model.
	 * 
	 * @param outputType the output type the model will be fitted to (e.g.
	 *                   volumeAlive_Coniferous)
	 * @param e          a ModelImplEnum enum
	 * @return a boolean true if the model has converged or false otherwise
	 */
	public boolean fitModel(String outputType, boolean enableMixedModelImplementations) {
		model = null; // reset the convergence to false
		REpiceaLogManager.logMessage(MetaModelManager.LoggerName, Level.INFO, "Meta-model " + stratumGroup,
				"----------- Modeling output type: " + outputType + " ----------------");
		try {
			List<InnerWorker> modelList = new ArrayList<InnerWorker>();

			List<ModelImplEnum> myImplementations = new ArrayList<ModelImplEnum>();
			myImplementations.add(ModelImplEnum.ChapmanRichards);
			if (enableMixedModelImplementations) {
				myImplementations.add(ModelImplEnum.ChapmanRichardsWithRandomEffect);
			}
			myImplementations.add(ModelImplEnum.ChapmanRichardsDerivative);
			if (enableMixedModelImplementations) {
				myImplementations.add(ModelImplEnum.ChapmanRichardsDerivativeWithRandomEffect);
			}
			for (ModelImplEnum e : myImplementations) { // use the basic models first, i.e. those without random effects
				InnerWorker w = new InnerWorker(getInnerModel(outputType, e));
				w.start();
				modelList.add(w);
			}
			for (InnerWorker w : modelList) {
				w.join();
			}
			InnerWorker selectedWorker = performModelSelection(modelList);
			REpiceaLogManager.logMessage(MetaModelManager.LoggerName, Level.INFO, "Meta-model " + stratumGroup,
					"Selected model is " + selectedWorker.ami.getModelImplementation().name());
			model = selectedWorker.ami;
			
			lastFitTimeStamp = new Date(System.currentTimeMillis());
//			System.out.println(model.getSummary());
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
	}

	public double getPrediction(int ageYr, int timeSinceInitialDateYr) throws MetaModelException {
		if (hasConverged()) {
			double pred = model.getPrediction(ageYr, timeSinceInitialDateYr, 0d);
			return pred;
		} else {
			throw new MetaModelException("The meta-model has not converged or has not been fitted yet!");
		}
	}

	protected Matrix getFinalParameterEstimates() {
		return model.getParameters();
	}

//	/**
//	 * Export the initial data set (before fitting the meta-model).
//	 * @param filename
//	 * @throws Exception
//	 */
//	public void exportInitialDataSet(String filename) throws Exception {
//		getDataStructureReady().getDataSet().save(filename);
//	}

	/**
	 * Export a final dataset, that is the initial data set plus the meta-model
	 * predictions. This works only if the model has converged.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void exportFinalDataSet(String filename) throws IOException {
		if (hasConverged()) {
			getFinalDataSet().save(filename);
		}
	}

	/**
	 * Provide the selected output type that was set in the call to method fitModel.
	 * 
	 * @return a String
	 */
	public String getSelectedOutputType() {
		if (model != null) {
			return model.getSelectedOutputType();
		} else {
			return "";
		}
	}

	/**
	 * Save a CSV file containing the final sequence produced by the
	 * Metropolis-Hastings algorithm, that is without the burn-in period and with
	 * only every nth observation. The number of burn-in samples to be dropped is
	 * set by the nbBurnIn member while every nth observation is set by the oneEach
	 * member.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	void exportMetropolisHastingsSample(String filename) throws IOException {
		model.mh.exportMetropolisHastingsSample(filename);
	}

	protected DataSet getFinalDataSet() {
		return model.getFinalDataSet();
	}

	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
		
		MetaDataHelper helper = new MetaDataHelper(); 
		MetaModelMetaData data = helper.generate();
				
		FileOutputStream os = new FileOutputStream(FilenameUtils.removeExtension(filename).concat(".json"));
		
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(JsonWriter.PRETTY_PRINT, true);
		options.put(JsonWriter.DATE_FORMAT, JsonWriter.ISO_DATE_TIME_FORMAT+"Z");
		JsonWriter jw = new JsonWriter(os, options);
		jw.write(data);		
	}

	/**
	 * Load a meta-model instance from file
	 * 
	 * @param filename
	 * @return a MetaModel instance
	 * @throws IOException
	 */
	public static MetaModel Load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		Object obj = deserializer.readObject();
		MetaModel metaModel = (MetaModel) obj;
		if (metaModel.mhSimParms == null) { // saved under a former implementation where this variable was static
			metaModel.setDefaultSettings();
		}
		return metaModel;
	}

	public void printSummary() {
		DataSet d = getSummary();
		if (d != null) {
			System.out.println(model.getSummary());
		}
	}

	public DataSet getSummary() {
		if (hasConverged()) {
			return model.getSummary();
		} else {
			System.out.println("The model has not been fitted yet!");
			return null;
		}
	}

	public DataSet getModelComparison() {
		if (hasConverged()) {
			return modelComparison;
		} else {
			System.out.println("The model has not been fitted yet!");
			return null;
		}
	}
}
