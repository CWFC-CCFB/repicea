/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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

import java.util.Vector;

import repicea.io.tools.ExportTool;

@SuppressWarnings("deprecation")
public class ExportDBFToolImpl extends ExportTool {

	public enum Allo {allo, byebye, bonjour};
	
	protected ExportDBFToolImpl() throws Exception {
		super();
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	protected Vector<Enum> defineAvailableExportOptions() {
		Vector<Enum> exportOptions = new Vector<Enum>();
		for (Allo var : Allo.values()) {
			exportOptions.add(var);
		}
		return exportOptions;
	}


	public static void main(String[] args) {
		ExportDBFToolImpl test;
		try {
			test = new ExportDBFToolImpl();
			test.showUI(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption) {
		return null;
	}


	
	
}
