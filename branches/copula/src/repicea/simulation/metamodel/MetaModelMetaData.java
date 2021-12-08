/*
 * This file is part of the repicea library.
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

import java.io.FileInputStream;
import java.io.IOException;

import repicea.io.Loadable;
import repicea.io.Saveable;
import repicea.serial.xml.XmlSerializer;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


/**
 * Stores and manipulates metadata associated with the metamodel.   
 * @author Jean-Francois Lavoie and Mathieu Fortin - September 2021
 */
public class MetaModelMetaData implements Loadable, Saveable {
	
	public class Common {
		// common
		String geoDomain;
		
		public Common(String geoDomain) {
			this.geoDomain = geoDomain;
		}
	}	
	
	public class Growth {
		String dataSource;	// which inventory was used ?
		String dataSourceYears; // year or year span used
		int nbRealizations;
		String climateChangeOption;
		String growthModel;
		String upscaling;	// enum
		
		public Growth(String dataSource, String dataSourceYears, int nbRealizations, String climateChangeOption, String growthModel, String upscaling) {
			this.dataSource = dataSource;
			this.dataSourceYears = dataSourceYears;
			this.nbRealizations = nbRealizations;
			this.climateChangeOption = climateChangeOption;
			this.growthModel = growthModel;
			this.upscaling = upscaling;
		}
	}
	
	public class Fit {
		String outputType;	
		String fitModel; 	
		String stratumGroup;
		double logLikelihood;
		
		public Fit(String outputType, String fitModel, String stratumGroup, double logLikelihood) {
			this.outputType = outputType;
			this.fitModel = fitModel;
			this.stratumGroup = stratumGroup;
			this.logLikelihood = logLikelihood;
		}
	}
	
	Common common;	
	Growth growth;
	Fit fit;
	
	public MetaModelMetaData (Common common, Growth growth, Fit fit) {
		this.common = common;
		this.growth = growth;
		this.fit = fit;
	}
	
	public String toJSON() {
		return JsonWriter.objectToJson(this);
	}

	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
	}

	@Override
	public void load(String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
