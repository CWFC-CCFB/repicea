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

import repicea.serial.xml.XmlDeserializer;
import repicea.util.ObjectUtility;

public class StreamImportFieldManagerTest {

	
	
	@Test
	public void testingAutomaticHeader() throws Exception {
		String referenceFilename = ObjectUtility.getPackagePath(getClass()) + "headerTest.xml";
		REpiceaRecordReader recordReader = new TestRecordReader();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		recordReader.initInScriptMode(ifm);
		List<String> fieldNames = ifm.getFormatReader().getFieldNames();
//		UNCOMMENT THESE TWO LINES TO UPDATE THE TEST
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(fieldNames);

		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		List<String> refFieldNames = (List) deserializer.readObject();
		
		for (int i = 0; i < fieldNames.size(); i++) {
			String fieldName = fieldNames.get(i);
			String referenceFieldName = refFieldNames.get(i);
			Assert.assertEquals("Testing fieldnames", referenceFieldName, fieldName);
		}
	}
	
	@Test
	public void testingInterruption() throws Exception {
		TestRecordReader2 recordReader = new TestRecordReader2();
		StreamImportFieldManager ifm = new StreamImportFieldManager(recordReader);
		recordReader.initInScriptMode(ifm);
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

}
