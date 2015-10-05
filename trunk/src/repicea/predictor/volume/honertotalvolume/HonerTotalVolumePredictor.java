/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.volume.honertotalvolume;

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.predictor.volume.honertotalvolume.HonerTotalVolumeTree.HonerTotalVolumeTreeSpecies;
import repicea.simulation.ModelBasedSimulator;

/**
 * The HonerTotalVolumePredictor class implements the volume model developed by Honer 1983.
 * It provides the total tree volume based on tree dbh and height.
 * @author Mathieu Fortin - March 2013
 */
@SuppressWarnings("serial")
public class HonerTotalVolumePredictor extends ModelBasedSimulator {

	private final Map<HonerTotalVolumeTreeSpecies, Matrix> betaMap;
		
	/**
	 * General constructor for this class.
	 */
	public HonerTotalVolumePredictor() {
		super(false, false,false);
		betaMap = new HashMap<HonerTotalVolumeTreeSpecies, Matrix>();
		init();
	}
	
	@Override
	protected final void init() {
		Matrix mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.SAB, mat);
		mat.m_afData[0][0] = 2.139;
		mat.m_afData[1][0] = 91.938;
		mat.m_afData[2][0] = 0.004331;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PRU, mat);
		mat.m_afData[0][0] = 1.112;
		mat.m_afData[1][0] = 106.708;
		mat.m_afData[2][0] = 0.004330;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.THO, mat);
		mat.m_afData[0][0] = 4.167;
		mat.m_afData[1][0] = 74.647;
		mat.m_afData[2][0] = 0.004330;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIG, mat);
		mat.m_afData[0][0] = 0.897;
		mat.m_afData[1][0] = 106.232;
		mat.m_afData[2][0] = 0.004331;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIB, mat);
		mat.m_afData[0][0] = 0.691;
		mat.m_afData[1][0] = 110.848;
		mat.m_afData[2][0] = 0.004319;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIR, mat);
		mat.m_afData[0][0] = 0.710;
		mat.m_afData[1][0] = 108.394;
		mat.m_afData[2][0] = 0.004331;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPN, mat);
		mat.m_afData[0][0] = 1.588;
		mat.m_afData[1][0] = 101.609;
		mat.m_afData[2][0] = 0.004327;
	
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPB, mat);
		mat.m_afData[0][0] = 1.440;
		mat.m_afData[1][0] = 104.295;
		mat.m_afData[2][0] = 0.004322;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPR, mat);
		mat.m_afData[0][0] = 1.226;
		mat.m_afData[1][0] = 96.266;
		mat.m_afData[2][0] = 0.004325;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.BOJ, mat);
		mat.m_afData[0][0] = 1.449;
		mat.m_afData[1][0] = 105.081;
		mat.m_afData[2][0] = 0.004320;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.BOP, mat);
		mat.m_afData[0][0] = 2.222;
		mat.m_afData[1][0] = 91.554;
		mat.m_afData[2][0] = 0.004322;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.CET, mat);
		mat.m_afData[0][0] = 0.033;
		mat.m_afData[1][0] = 119.889;
		mat.m_afData[2][0] = 0.004334;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERR, mat);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERS, mat);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERN, mat);
		mat.m_afData[0][0] = 1.046;
		mat.m_afData[1][0] = 117.035;
		mat.m_afData[2][0] = 0.004334;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PET, mat);
		mat.m_afData[0][0] = -0.312;
		mat.m_afData[1][0] = 133.101;
		mat.m_afData[2][0] = 0.004341;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PEB, mat);
		mat.m_afData[0][0] = 0.420;
		mat.m_afData[1][0] = 120.287;
		mat.m_afData[2][0] = 0.004341;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.HEG, mat);
		mat.m_afData[0][0] = 0.959;
		mat.m_afData[1][0] = 102.056;
		mat.m_afData[2][0] = 0.004334;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.CHR, mat);
		mat.m_afData[0][0] = 1.512;
		mat.m_afData[1][0] = 102.568;
		mat.m_afData[2][0] = 0.004334;
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.OSV, mat);
		mat.m_afData[0][0] = 1.877;
		mat.m_afData[1][0] = 101.372;
		mat.m_afData[2][0] = 0.004334;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.TIL, mat);
		mat.m_afData[0][0] = 0.948;
		mat.m_afData[1][0] = 122.364;
		mat.m_afData[2][0] = 0.004334;

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.ORA, mat);
		mat.m_afData[0][0] = 0.634;
		mat.m_afData[1][0] = 134.263;
		mat.m_afData[2][0] = 0.004334;
		
	}
	
	/**
	 * This method returns the total volume of a particular tree.
	 * @param tree a HonerTotalVolumeTree instance
	 * @return the total volume (m3)
	 */
	public double predictTreeTotalVolume(HonerTotalVolumeTree tree) {
		HonerTotalVolumeTreeSpecies species = tree.getHonerSpecies();
		Matrix beta = betaMap.get(species);
		
		double a0 = beta.m_afData[0][0];
		double a1 = beta.m_afData[1][0];
		double a2 = beta.m_afData[2][0];
		double h = tree.getHeightM();
		
		
		return a2 * tree.getSquaredDbhCm() / (a0 + a1 / h);
	}
	
}
