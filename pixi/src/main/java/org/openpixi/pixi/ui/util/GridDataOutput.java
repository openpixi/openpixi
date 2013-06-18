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
public class GridDataOutput extends EmptyGridDataOutput{

	private BufferedWriter potential;

	public GridDataOutput(String dir, String runid, Grid g) throws IOException {
		
		// Writes grid parameters to file s.t. the other methods do not have to do it
		// in every time step.
		BufferedWriter gridProperties = writerFactory(dir + "gridproperties-" + runid);
		gridProperties.write("Number of cells in x direction\t" + g.getNumCellsX() + "\n" +
				"Number of cells in y direction\t" +  g.getNumCellsY() + "\n\n" +
				"Cell width (x direction)\t" + g.getCellWidth() + "\n" +
				"Cell height (y direction)\t" + g.getCellHeight() + "\n\n" +
				"The output in grid related files is structured in the following way:\n" +
				"For scalar quantities the first " + g.getNumCellsY() + " entries represent" +
						" the first column of the grid. The next " + g.getNumCellsY() + 
						" entries represent the second column, etc.\n");
		gridProperties.close();		
		
		potential = writerFactory(dir + "potential-" + runid);

	}
	
	public void potential(double phi) {
		try {
			potential.write(phi + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Should be called at the beginning of each iteration of the simulation */
	public void startIteration(int iteration) {
		try {
			potential.write("Iteration: " + iteration + "\n");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Should be called when no data needs to be written anymore */
	public void closeStreams() {
		try {
			potential.close();
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
