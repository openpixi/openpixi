package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class ProjectedEnergyDensity implements Diagnostics {

	private int direction;

	private String path;
	private double timeInterval;
	private int stepInterval;
	private int numberOfCells;
	private double[] energyDensity;

	public ProjectedEnergyDensity(String path, double timeInterval, int direction) {
		this.direction = direction;
		this.path = path;
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		numberOfCells = s.grid.getNumCells(direction);
		this.stepInterval = (int) (timeInterval / s.getTimeStep());

		clear();
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {
			// Compute projected energy density
			energyDensity = new double[numberOfCells];
			for (int i = 0; i < numberOfCells; i++) {
				energyDensity[i] = 0.0;
			}

			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				if(grid.isEvaluatable(i)) {
					int projIndex = grid.getCellPos(i)[direction];
					// transversal & longitudinal electric energy density
					double e_T_el = 0.0;
					double e_L_el = 0.0;
					// transversal & longitudinal magnetic energy density
					double e_T_mag = 0.0;
					double e_L_mag = 0.0;

					for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
						if(j == direction) {
							e_L_el += 0.5 * grid.getE(i, j).square();
							e_L_mag += 0.25 * (grid.getBsquaredFromLinks(i, j, 0) + grid.getBsquaredFromLinks(i, j, 1));
						} else {
							e_T_el += 0.5 * grid.getE(i, j).square();
							e_T_mag += 0.25 * (grid.getBsquaredFromLinks(i, j, 0) + grid.getBsquaredFromLinks(i, j, 1));
						}
					}

					// electric energy density
					double e_el = e_L_el + e_T_el;

					// magnetic energy density
					double e_mag = e_L_mag + e_T_mag;

					// total energy density
					double totalEnergyDensity = e_el + e_mag;

					/*
					// electric pressure
					double p_el_T = e_L_el;
					double p_el_L = e_T_el - e_L_el;

					// magnetic pressure
					double p_mag_T = e_L_mag;
					double p_mag_L = e_T_mag - e_L_mag;

					// total longitudinal & transversal pressure ratios
					double p_L_r = (p_el_L + p_mag_L) / totalEnergyDensity;
					double p_T_r = (p_el_T + p_mag_T) / totalEnergyDensity;
					*/

					energyDensity[projIndex] += totalEnergyDensity;
				}
			}

			// Write to file
			File file = this.getOutputFile(path);
			try {
				FileWriter pw = new FileWriter(file, true);
				Double time = steps * grid.getTemporalSpacing();
				pw.write(time.toString() + "\n");
				pw.write(generateTSVString(energyDensity) + "\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("ProjectedEnergyDensity: Error writing to file.");
			}
		}
	}

	/**
	 * Creates a file with a given name in the output folder.
	 * @param filename	Filename of the output file.
	 * @return			File object of the opened file.
	 */
	private File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}

	/**
	 * 	Checks if the files are already existent and deletes them
	 */
	public void clear() {
		File particlesfile = getOutputFile(path);
		boolean fileExists1 = particlesfile.exists();
		if(fileExists1 == true) {
			particlesfile.delete();
		}
	}

	public String generateTSVString(double[] array) {
		StringBuilder outputStringBuilder = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("0.################E0");
		for (int i = 0; i < array.length; i++) {
			outputStringBuilder.append(formatter.format(array[i]));
			if(i < array.length - 1) {
				outputStringBuilder.append("\t");
			}
		}
		return outputStringBuilder.toString();
	}
}
