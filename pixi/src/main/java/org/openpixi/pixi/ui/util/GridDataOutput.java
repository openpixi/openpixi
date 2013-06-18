package org.openpixi.pixi.ui.util;

import org.openpixi.pixi.physics.grid.Grid;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GridDataOutput extends EmptyGridDataOutput{

	private BufferedWriter potential;


	public GridDataOutput(String dir, String runid, Grid g) throws IOException {
		
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

	public void startIteration(int iteration) {
		try {
			potential.write("Iteration: " + iteration + "\n");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
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
