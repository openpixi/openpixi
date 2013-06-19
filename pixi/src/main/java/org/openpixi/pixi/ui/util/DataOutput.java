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

import org.openpixi.pixi.physics.grid.Grid;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is an example DataOutput object as needed to extract data
 * from the diagnostics package. Here we decided to write the data
 * to files.
 */
public class DataOutput extends EmptyDataOutput{

	private BufferedWriter potential;
	private BufferedWriter totalKineticEnergy;
	private int iteration;

	public DataOutput(String directory, String runid, Grid grid) throws IOException {
		
		// Writes grid parameters to file s.t. the other methods do not have to do it
		// in every time step.
		BufferedWriter gridProperties = writerFactory(directory + "gridproperties-" + runid);
		gridProperties.write("Number of cells in x direction\t" + grid.getNumCellsX() + "\n" +
				"Number of cells in y direction\t" +  grid.getNumCellsY() + "\n\n" +
				"Cell width (x direction)\t" + grid.getCellWidth() + "\n" +
				"Cell height (y direction)\t" + grid.getCellHeight() + "\n\n" +
				"The output in grid related files is structured in the following way:\n" +
				"For scalar quantities the first " + grid.getNumCellsY() + " entries represent" +
						" the first column of the grid. The next " + grid.getNumCellsY() + 
						" entries represent the second column, etc.\n");
		gridProperties.close();		
		
		potential = writerFactory(directory + "potential-" + runid);
		totalKineticEnergy = writerFactory(directory + "kinetic-energy-" + runid);
	}
	
	public void potential(double[][] phi) {
		try {
			potential.write("Iteration: " + iteration + "\n");
			for(double[] column : phi) {
				for(double element : column) {
					potential.write(element + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void kineticEnergy(double energy) {
		try {
			totalKineticEnergy.write("Iteration: " + iteration + "\n");
			totalKineticEnergy.write("" + energy);
			totalKineticEnergy.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Should be called at the beginning of each iteration of the simulation */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}
	
	/** Should be called when no data needs to be written anymore */
	public void closeStreams() {
		try {
			potential.close();
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
