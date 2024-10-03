/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package repicea.io;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class REpiceaFileFilterTest {

	@Test
	public void testExcelFilename() {
		String extension = REpiceaFileFilter.XLSX.getExtension();
		Assert.assertEquals("Testing extension", ".xlsx", extension);
		String rightFilename = "Allo.xlsx";
		Assert.assertTrue("File is accepted", REpiceaFileFilter.XLSX.accept(new File(rightFilename)));
		String wrongFilename = "Allo.xls";
		Assert.assertTrue("File is not accepted", !REpiceaFileFilter.XLSX.accept(new File(wrongFilename)));
	}
	
	
}
