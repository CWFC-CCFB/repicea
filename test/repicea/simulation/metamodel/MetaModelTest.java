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
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.metamodel.MetaModel.ModelImplEnum;
import repicea.util.ObjectUtility;

public class MetaModelTest {

	static {		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelManager", "repicea.simulation.metamodel.MetaModelManager");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel", "repicea.simulation.metamodel.MetaModel");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.DataBlockWrapper", "repicea.simulation.metamodel.DataBlockWrapper");		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModelGibbsSample", "repicea.simulation.metamodel.MetaModelGibbsSample");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$Bound", "repicea.simulation.metamodel.MetaModel$Bound");
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.metamodel.ExtMetaModel$InnerModel", "repicea.simulation.metamodel.MetaModel$InnerModel");		
		XmlSerializerChangeMonitor.registerClassNameChange("capsis.util.extendeddefaulttype.ExtScriptResult", "repicea.simulation.metamodel.ScriptResult");				
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation", "repicea.simulation.metamodel.ChapmanRichardsModelWithRandomEffectImplementation");				
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.simulation.metamodel.RichardsChapmanModelWithRandomEffectImplementation$DataBlockWrapper", 
				"repicea.simulation.metamodel.ChapmanRichardsModelWithRandomEffectImplementation$DataBlockWrapper");				
		XmlSerializerChangeMonitor.registerEnumNameChange("repicea.simulation.metamodel.MetaModel$ModelImplEnum", "RichardsChapmanWithRandomEffect", "ChapmanRichardsWithRandomEffect");
	}
	
	static MetaModel MetaModelInstance;
	
		
	@BeforeClass
	public static void deserializingMetaModel() throws IOException {
		String metaModelFilename = ObjectUtility.getPackagePath(MetaModelTest.class) + "QC_FMU02664_RE2_NoChange_AliveVolume_ConiferousSpecies.zml";
		MetaModelInstance = MetaModel.Load(metaModelFilename);
	}
	
	@AfterClass
	public static void removeSingleton() {
		MetaModelInstance = null;
	}

	@Test
	public void testingMetaModelDeserialization() throws IOException {
		Assert.assertTrue("Model is deserialized", MetaModelInstance != null);
		Assert.assertTrue("Has converged", MetaModelInstance.hasConverged());
		String filename = ObjectUtility.getPackagePath(getClass()) + "finalDataSet.csv";
		MetaModelInstance.exportFinalDataSet(filename);
		Assert.assertEquals("Testing final dataset size", 60, MetaModelInstance.getFinalDataSet().getNumberOfObservations());
	}

	@Test
	public void testingOutputTypes() throws Exception {
		List<String> outputTypes = MetaModelInstance.getPossibleOutputTypes();
		Assert.assertEquals("Testing list size", 2, outputTypes.size());
		Assert.assertEquals("Testing first value", "AliveVolume_BroadleavedSpecies", outputTypes.get(0));
		Assert.assertEquals("Testing second value", "AliveVolume_ConiferousSpecies", outputTypes.get(1));
	}

	@Test
	public void testingMetaModelPrediction() throws Exception {
		double pred = MetaModelInstance.getPrediction(90, 0);
		Assert.assertEquals("Testing prediction at 90 yrs of age", 102.049670163, pred, 1E-8);
	}

	public static void main(String[] args) throws IOException {
		String path = ObjectUtility.getPackagePath(MetaModelTest.class);
		String outputPath = "C:\\Users\\matforti\\Documents\\7_Developpement\\ModellingProjects\\Quebec\\ProcessedData\\UAF02664\\metaModels";
		String metaModelFilename = path + "QC_FMU02664_RS2_NoChange_AliveVolume_ConiferousSpecies.zml";
		MetaModel m = MetaModel.Load(metaModelFilename);
		System.out.println("Parameter estimates = " + m.getFinalParameterEstimates());
		m.fitModel("AliveVolume_ConiferousSpecies", ModelImplEnum.ChapmanRichards);
		m.exportFinalDataSet(outputPath + File.separator + "RS2_RichardsChapman.csv");
		m.fitModel("AliveVolume_ConiferousSpecies", ModelImplEnum.ChapmanRichardsWithRandomEffect);
		m.exportFinalDataSet(outputPath + File.separator + "RS2_RichardsChapmanWithRandomEffect.csv");
		m.fitModel("AliveVolume_ConiferousSpecies", ModelImplEnum.ChapmanRichardsDerivative);
		m.exportFinalDataSet(outputPath + File.separator + "RS2_RichardsChapmanDerivative.csv");
		m.fitModel("AliveVolume_ConiferousSpecies", ModelImplEnum.ChapmanRichardsDerivativeWithRandomEffect);
		m.exportFinalDataSet(outputPath + File.separator + "RS2_RichardsChapmanDerivativeWithRandomEffect.csv");
		
//		m.exportMetropolisHastingsSample(path + "mhSample.csv");
//		m.exportFinalDataSet(path + "QC_FMU02664_Art2009_Plus4Degrees_" + m.getStratumGroup() + "_Coniferous.csv");
//		m.save(path + "QC_FMU02664_Art2009_Plus4Degrees_" + m.getStratumGroup() + "_Coniferous.zml");
//		int u = 0;
	}
}
