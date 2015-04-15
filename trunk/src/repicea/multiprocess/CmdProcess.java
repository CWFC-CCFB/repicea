/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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
package repicea.multiprocess;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class CmdProcess extends AbstractIndependentProcess {

	private static final long serialVersionUID = 20111104L;

	private List<String> commands;
	private File workingDirectory;
	
	/**
	 * General constructor.
	 * @param commands the list of commands
	 * @param workingDirectory the working directory for the process
	 */
	public CmdProcess(List<String> commands, File workingDirectory) {
		this.commands = commands;
		this.workingDirectory = workingDirectory;
	}
	
	/**
	 * Sub constructor with the commands only.
	 * @param commands the list of commands
	 */
	public CmdProcess(List<String> commands) {
		this(commands, null);
	}

	
	@Override
	protected Process createIndependentProcess() throws IOException {
		Runtime rt = Runtime.getRuntime();
		String[] cmdarray = new String[commands.size()];
		int i = 0;
		for (String command : commands) {
			cmdarray[i++] = command;
		}
		if (workingDirectory != null) {
			return rt.exec(cmdarray, null, workingDirectory);
		} else {
			return rt.exec(cmdarray);
		}
	}

	public String getName() {
		if (super.getName() == null) {
			String filename = commands.get(0);
			int lastIndex = filename.lastIndexOf(File.separator);
			return filename.substring(++lastIndex, filename.length());
		} else {
			return super.getName();
		}
	}
	
	
//	public static void main(String[] args) throws InterruptedException, ExecutionException, UnknownHostException {
////		String targetPath = "C:" + File.separator +
////				"Users" + File.separator +
////				"mfortin" + File.separator +
////				"JavaProjects" + File.separator +
////				"capsis4" + File.separator +
////				"ext" + File.separator +
////				"updater";
//		String workingDir = "C:" + File.separator +
//				"Users" + File.separator +
//				"mfortin" + File.separator +
//				"JavaProjects" + File.separator +
//				"capsis4" + File.separator;
//
//		args = new String[1];
//		args[0] = workingDir + "capsis.bat";
//		List<String> commands = new ArrayList<String>();
//		for (String str : args) {
//			commands.add(str);
//		}
//		CmdProcess process = new CmdProcess(commands, new File(workingDir));
//		process.execute();
//		System.exit(0);
//	}

}
