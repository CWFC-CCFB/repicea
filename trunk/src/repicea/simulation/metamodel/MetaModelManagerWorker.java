/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Authors: M. Fortin and J.-F. Lavoie - Canadian Forest Service
 * Copyright (C) 2020-21 Her Majesty the Queen in right of Canada
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package repicea.simulation.metamodel;

import java.util.concurrent.LinkedBlockingQueue;

class MetaModelManagerWorker extends Thread implements Runnable {

	static final Object FinishToken = new Object();
	
	final LinkedBlockingQueue queue;
	
	MetaModelManagerWorker(int i, LinkedBlockingQueue queue) {
		super("ExtMetaModelManagerWorker " + i);
		this.queue = queue;
		setDaemon(true);
		start();
	}
	
	
	@Override
	public void run() {
		Object o;
		try {
			while(!(o = queue.take()).equals(FinishToken)) {
				MetaModel metaModel = (MetaModel) o;
				metaModel.fitModel();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
