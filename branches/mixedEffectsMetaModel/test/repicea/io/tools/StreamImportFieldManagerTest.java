/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Rouge-Epicea
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
package repicea.io.tools;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.tools.ImportFieldElement.ImportFieldElementIDCard;
import repicea.serial.xml.XmlDeserializer;
import repicea.util.ObjectUtility;

public class StreamImportFieldManagerTest {

	
	
	@Test
	public void testingAutomaticHeader() throws Exception {
		String referenceFilename = ObjectUtility.getPackagePath(getClass()) + "headerTest.xml";
		REpiceaRecordReader recordReader = new RecordReaderImpl();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		recordReader.initInScriptMode(ifm);
		List<ImportFieldElementIDCard> fieldDescriptions = ifm.getFieldDescriptions();
		System.out.println(fieldDescriptions);
//		UNCOMMENT THESE TWO LINES TO UPDATE THE TEST
//		XmlSerializer serializer = new XmlSerializer(referenceFilename.replace("bin", "test"));
//		serializer.writeObject(fieldNames);

		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		List<String> refFieldDescriptions = (List) deserializer.readObject();
		
		for (int i = 0; i < fieldDescriptions.size(); i++) {
			String fieldDescription = fieldDescriptions.get(i).toString();
			String referenceFieldDescription = refFieldDescriptions.get(i);
			Assert.assertEquals("Testing fieldnames", referenceFieldDescription, fieldDescription);
		}
	}
	
	@Test
	public void testingInputData() throws Exception {
		RecordReaderImpl2 recordReader = new RecordReaderImpl2();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		recordReader.initInScriptMode(ifm);
		List<ImportFieldElementIDCard> fieldDescriptions = ifm.getFieldDescriptions();
		System.out.println(fieldDescriptions);
		
		Object[] record = new Object[2];
		record[0] = "plot1";
		record[1] = "EPX";
		ifm.getFormatReader().addRecord(record);
		record = new Object[2];
		record[0] = "plot2";
		record[1] = "SAB";
		ifm.getFormatReader().addRecord(record);
		recordReader.readAllRecords();
		Assert.assertEquals("Testing plot 1", recordReader.resultMap.get("plot1"), "EPX");
		Assert.assertEquals("Testing plot 2", recordReader.resultMap.get("plot2"), "SAB");
	}

	@Test
	public void testingInverseOrderInputData() throws Exception {
		RecordReaderImpl2 recordReader = new RecordReaderImpl2();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		ifm.setFieldMatches(new int[] {1,0});
		recordReader.initInScriptMode(ifm);
		List<ImportFieldElementIDCard> fieldDescriptions = ifm.getFieldDescriptions();
		System.out.println(fieldDescriptions);
		
		Object[] record = new Object[2];
		record[1] = "plot1";
		record[0] = "EPX";
		ifm.getFormatReader().addRecord(record);
		record = new Object[2];
		record[1] = "plot2";
		record[0] = "SAB";
		ifm.getFormatReader().addRecord(record);
		recordReader.readAllRecords();
		Assert.assertEquals("Testing plot 1", recordReader.resultMap.get("plot1"), "EPX");
		Assert.assertEquals("Testing plot 2", recordReader.resultMap.get("plot2"), "SAB");
	}

	@Test
	public void testingLargerThanNeededInputData() throws Exception {
		RecordReaderImpl2 recordReader = new RecordReaderImpl2();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		ifm.setFieldMatches(new int[] {2,0});
		recordReader.initInScriptMode(ifm);
		List<ImportFieldElementIDCard> fieldDescriptions = ifm.getFieldDescriptions();
		System.out.println(fieldDescriptions);
		
		Object[] record = new Object[3];
		record[2] = "plot1";
		record[1] = "carotte";
		record[0] = "EPX";
		ifm.getFormatReader().addRecord(record);
		record = new Object[3];
		record[2] = "plot2";
		record[1] = "patate";
		record[0] = "SAB";
		ifm.getFormatReader().addRecord(record);
		recordReader.readAllRecords();
		Assert.assertEquals("Testing plot 1", recordReader.resultMap.get("plot1"), "EPX");
		Assert.assertEquals("Testing plot 2", recordReader.resultMap.get("plot2"), "SAB");
	}

	@Test
	public void testingResettingAndThenClear() throws Exception {
		RecordReaderImpl2 recordReader = new RecordReaderImpl2();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		ifm.setFieldMatches(new int[] {1,0});
		recordReader.initInScriptMode(ifm);
		List<ImportFieldElementIDCard> fieldDescriptions = ifm.getFieldDescriptions();
		System.out.println(fieldDescriptions);
		
		Object[] record = new Object[2];
		record[1] = "plot1";
		record[0] = "EPX";
		ifm.getFormatReader().addRecord(record);
		record = new Object[2];
		record[1] = "plot2";
		record[0] = "SAB";
		ifm.getFormatReader().addRecord(record);
		
		recordReader.readAllRecords();
		Assert.assertEquals("Testing plot 1", recordReader.resultMap.get("plot1"), "EPX");
		Assert.assertEquals("Testing plot 2", recordReader.resultMap.get("plot2"), "SAB");
		recordReader.resultMap.clear();
		ifm.getFormatReader().reset();
		
		recordReader.readAllRecords();
		Assert.assertEquals("Testing plot 1", recordReader.resultMap.get("plot1"), "EPX");
		Assert.assertEquals("Testing plot 2", recordReader.resultMap.get("plot2"), "SAB");
		recordReader.resultMap.clear();
		ifm.getFormatReader().clearRecords();
		
		recordReader.readAllRecords();
		Assert.assertEquals("Testing that the result map is empty", 0, recordReader.resultMap.size());
	}

}
