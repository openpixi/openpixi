/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openpixi.pixi.ui.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is an example DataOutput object as needed to extract data
 * from the diagnostics package. Here we decided to write the data
 * to files.
 */
public class ParticleDataOutput extends EmptyParticleDataOutput{
	
	private BufferedWriter totalKineticEnergy;
	
	public ParticleDataOutput(String dir, String runid) throws IOException {
		totalKineticEnergy = writerFactory(dir + "Energy" + runid);
	}
	
	public void kineticEnergy(double energy) {
		try {
			totalKineticEnergy.write("" + energy);
			totalKineticEnergy.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Should be called at the beginning of each iteration of the simulation */
	public void startIteration(int iteration) {
		try {
			totalKineticEnergy.write("Iteration: " + iteration + "\n");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Should be called when no data needs to be written anymore */
	public void closeStreams() {
		try {
			
			totalKineticEnergy.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedWriter writerFactory(String path) throws IOException {
		File file;
		FileWriter fstream;
		BufferedWriter out;
	
		file = new File(path);
		fstream = new FileWriter(file);
		out = new BufferedWriter(fstream);
		return out;
	}
}
