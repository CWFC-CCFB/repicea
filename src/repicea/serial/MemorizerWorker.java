/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.serial;

import java.util.concurrent.LinkedBlockingDeque;

import repicea.serial.xml.XmlList;
import repicea.serial.xml.XmlMarshaller;

class MemorizerWorker extends Thread {
	
	protected static final MemorizerPackage ShutDownMemorizerPackage = new MemorizerPackage();

	private final LinkedBlockingDeque<MemorizerPackage> queue;

	private final REpiceaMemorizerHandler handler;
	
	protected MemorizerWorker(String name, REpiceaMemorizerHandler handler) {
		setName(name);
		this.handler = handler;
		queue = new LinkedBlockingDeque<MemorizerPackage>();
	}

	@Override
	public void run() {
		int numberOfFailures = 0;
		boolean stop = false;
		do {
			try {
				MemorizerPackage originalMp = queue.takeLast();
				queue.clear();
				if (System.identityHashCode(originalMp) == System.identityHashCode(ShutDownMemorizerPackage)) {
					stop = true;
				} else {
					XmlMarshaller marshaller = new XmlMarshaller();
					XmlList list = marshaller.marshall(originalMp);
					handler.registerMemorizerPackage(list);
				}
			} catch (InterruptedException e) {
				numberOfFailures++;
				e.printStackTrace();
			}
		} while (numberOfFailures < 10 && !stop);
	}

	protected void addToQueue(MemorizerPackage mp) {
		queue.addLast(mp);
	}
	
}


