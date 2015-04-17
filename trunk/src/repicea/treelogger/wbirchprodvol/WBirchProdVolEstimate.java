/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import repicea.math.Matrix;
import repicea.stats.estimates.HybridEstimate;
import repicea.treelogger.wbirchprodvol.WBirchProdVolPredictor.Version;

@SuppressWarnings("serial")
public class WBirchProdVolEstimate extends HybridEstimate<Matrix> {
	
	private final Version version;
	
	protected WBirchProdVolEstimate(Version version) {
		super();
		this.version = version;
	}
	

	protected WBirchProdVolEstimate(int numberOfMonteCarloRealizations, boolean isStochastic, Version version) {
		super(numberOfMonteCarloRealizations, isStochastic);
		this.version = version;
	}

	/**
	 * This method returns the version of the model that was used during the prediction process.
	 * @return a Version enum
	 */
	protected Version getVersion() {return version;}
	
	
}
