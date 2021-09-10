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

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.stats.data.DataSet;
import repicea.util.ObjectUtility;

public class MetaModelTests {

	static {		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelManager", "repicea.simulation.metamodel.MetaModelManager");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel", "repicea.simulation.metamodel.MetaModel");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.DataBlockWrapper", "repicea.simulation.metamodel.DataBlockWrapper");		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelGibbsSample", "repicea.simulation.metamodel.MetaModelGibbsSample");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$Bound", "repicea.simulation.metamodel.MetaModel$Bound");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$InnerModel", "repicea.simulation.metamodel.MetaModel$InnerModel");		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.ExtScriptResult", "repicea.simulation.metamodel.ScriptResult");				
	}
				
	@Test
	public void testingOutputTypes() throws Exception {
		MetaModelManager manager = new MetaModelManager();
		String fittedModelsFilename = ObjectUtility.getPackagePath(getClass()).replace("class", "test" + File.separator + "src") + "fittedMetaModel.zml";
		manager.load(fittedModelsFilename);				
		List<String> outputTypes = manager.getPossibleOutputTypes("RE2_NoChange");
		Assert.assertEquals("Testing list size", 2, outputTypes.size());
		Assert.assertEquals("Testing first value", "Broadleaved", outputTypes.get(0));
		Assert.assertEquals("Testing second value", "Coniferous", outputTypes.get(1));
	}

	@Test
	public void testingMetaModelPrediction() throws Exception {
		MetaModelManager manager = new MetaModelManager();
		String fittedModelsFilename = ObjectUtility.getPackagePath(getClass()).replace("class", "test" + File.separator + "src") + "fittedMetaModel.zml";
		manager.load(fittedModelsFilename);		
		double pred = manager.getPrediction("RE2_NoChange", 90, 0, "Coniferous");
		Assert.assertEquals("Testing prediction at 90 yrs of age", 105.8510350604584, pred, 1E-8);
	}

	@Test
	public void testingDeserializationFittedMetaModelManager() throws Exception {
		MetaModelManager manager = new MetaModelManager();
		String fittedModelsFilename = ObjectUtility.getPackagePath(getClass()).replace("class", "test" + File.separator + "src") + "fittedMetaModel.zml";
		manager.load(fittedModelsFilename);		
		DataSet finalDataSet = manager.getMetaModelResult("RE2_NoChange");
		Assert.assertEquals("Testing dataset size", 108, finalDataSet.getNumberOfObservations());
	}
}
