/*
 * This file is part of the repicea-util library.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import repicea.app.JSONConfiguration;
import repicea.app.JSONConfigurationGlobal;
import repicea.app.REpiceaJSONConfiguration;
import repicea.io.Loadable;
import repicea.io.Saveable;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.stats.data.DataSet;

/**
 * Handle different instances of ExtMetaModel. It is thread safe.
 * @author Mathieu Fortin - December 2020
 */
public class MetaModelManager extends ConcurrentHashMap<String, MetaModel> implements Loadable, Saveable {	
	
	static {
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelManager", "repicea.simulation.metamodel.MetaModelManager");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel", "repicea.simulation.metamodel.MetaModel");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$Bound", "repicea.simulation.metamodel.MetaModel$Bound");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$InnerModel", "repicea.simulation.metamodel.MetaModel$InnerModel");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.ExtScriptResult", "repicea.simulation.metamodel.ScriptResult");				
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.DataBlockWrapper", "repicea.simulation.metamodel.DataBlockWrapper");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelGibbsSample", "repicea.simulation.metamodel.MetaModelGibbsSample");
	}
	
//	static int NbRealizations = 10000;
	static int NbRealizations = 500000;
//	static int NbRealizations = 1000000;
	static int NbInternalIter = 100000;
	static int NbBurnIn = 5000;
	static int OneEach = 50;
//	static int NbInitialGrid = 100;
	static int NbInitialGrid = 10000;	
	
	static boolean Verbose = false;
	
	/**
	 * Constructor.
	 */
	public MetaModelManager() {}	

	/**
	 * Add a DataSet instance to a particular stratum group. 
	 * @param stratumGroup a String that stands for the stratum group
	 * @param initialAgeYr the age of the stratum at the beginning of the simulation
	 * @param result an ExtScriptResult instance that contains the simulation results
	 */
	public void addDataset(String stratumGroup, int initialAgeYr, ScriptResult result) {		
		if (!containsKey(stratumGroup)) {
			put(stratumGroup, new MetaModel(stratumGroup));
		}
		MetaModel metaModel = get(stratumGroup);
		metaModel.add(initialAgeYr, result);		
	}
	
	/**
	 * Fit all the meta-models.
	 * @throws an ExtMetaModelException if one of the models has not converged.
	 */
	public void fitMetaModels() throws MetaModelException {
		this.fitMetaModels(keySet());
		for (String stratumGroup : keySet()) {
			MetaModel metaModel = get(stratumGroup);
			if (!metaModel.fitModel()) {
				throw new MetaModelException("The meta-model for this stratum group has not converged: " + stratumGroup);
			}
		}
	}

	public void fitMetaModels(Collection<String> stratumGroups) throws MetaModelException {
		
		int NbWorkers = (int)((long) JSONConfigurationGlobal.getInstance().get(REpiceaJSONConfiguration.processingMaxThreads, 2L));
		
		// there is no point in creating more worker threads than there are stratumGroups, so limit their number if needed
		NbWorkers = NbWorkers > stratumGroups.size() ? stratumGroups.size() : NbWorkers; 
		
		try {
			LinkedBlockingQueue queue = new LinkedBlockingQueue();
			List<MetaModelManagerWorker> workers = new ArrayList<MetaModelManagerWorker>();
			for (int i = 0; i < NbWorkers; i++) {
				workers.add(new MetaModelManagerWorker(i, queue));
			}
			
			for (String stratumGroup : stratumGroups) {
				MetaModel metaModel = get(stratumGroup);
				metaModel.setStratumGroup(stratumGroup);
				queue.add(metaModel);
			}
			
			for (int i = 0; i < NbWorkers; i++) {
				queue.add(MetaModelManagerWorker.FinishToken);
			}
			for (MetaModelManagerWorker t : workers) {
				t.join();
			}
		} catch (InterruptedException e) {
			throw new MetaModelException(e.getMessage());
		}
	}
	
	/**
	 * Compute and return the prediction generated from a particular meta-model.
	 * @param stratumGroup a String that stands for the stratum group
	 * @param ageYr the age of the stratum (yr) 
	 * @param timeSinceInitialDateYr the time since the initial date (yr)
	 * @param outputType a String e.g. Broadleaved, Coniferous
	 * @return a double
	 * @throws MetaModelException if the model has not been fitted or if the stratum group is not found in the Map.
	 */
	public double getPrediction(String stratumGroup, int ageYr, int timeSinceInitialDateYr, String outputType) throws MetaModelException {
		MetaModel metaModel = getFittedMetaModel(stratumGroup);
		return metaModel.getPrediction(ageYr, timeSinceInitialDateYr, outputType);
	}
	
	private MetaModel getFittedMetaModel(String stratumGroup) throws MetaModelException {
		if (!containsKey(stratumGroup)) {
			throw new MetaModelException("The meta model for this stratum group does not exist: " + stratumGroup);
		} else {
			MetaModel model = get(stratumGroup);
			if (!model.converged) {
				throw new MetaModelException("The meta model for this stratum group has not been fitted or has not converged: " + stratumGroup);
			}
			return model;
		}
	}
	
	/**
	 * Provide the stratum groups 
	 * @param stratumGroup a String that stands for the stratum group.
	 * @return a List of String (sorted)
	 * 
	 */
	public List<String> getStratumGroups() throws MetaModelException {
		ArrayList<String> list = Collections.list(keys());
		list.sort(null);
		return list;
	}

	/**
	 * Provide the possible output types (e.g. "Coniferous", "Broadleaved") for a particular stratum group.
	 * @param stratumGroup a String that stands for the stratum group.
	 * @return a List of String
	 * @throws MetaModelException if the model has not been fitted or if the stratum group is not found in the Map.
	 */
	public List<String> getPossibleOutputTypes(String stratumGroup) throws MetaModelException {
		MetaModel metaModel = getFittedMetaModel(stratumGroup);
		List<String> outputTypes = new ArrayList<String>();
		for (Object obj : metaModel.model.dummyOriginalValues) {
			outputTypes.add(obj.toString());
		}
		return outputTypes;
	}

	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		MetaModelManager manager = (MetaModelManager) deserializer.readObject();
		clear(); // we clear only after loading the new manager
		putAll(manager);
	}
	
	/**
	 * Provide a final data set including the prediction of the meta model. 
	 * @param stratumGroup the stratum group
	 * @return a DataSet instance
	 * @throws MetaModelException if the stratum group does not exist or the model has not been fitted or has not converged yet.
	 */
	public DataSet getMetaModelResult(String stratumGroup) throws MetaModelException {
		if (containsKey(stratumGroup)) {
			MetaModel model = get(stratumGroup);
			if (model.converged) {
				return model.dataSet;
			} else {
				throw new MetaModelException("The model of this group : " + stratumGroup + " has not been fitted or has not converged yet!");
			}
		} else {
			throw new MetaModelException("This stratum group is not recognized: " + stratumGroup);
		}
	}
 	
}
