package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This diagnostic averages the energy density over the transversal plane (specified by the longitudinal direction)
 * and writes 4 energy density components to a file:
 * Electric longitudinal (energyDensity_L_el), magnetic longitudinal (energyDensity_L_mag),
 * electric transversal  (energyDensity_T_el) and magnetic transversal (energyDensity_T_mag) energy density.
 * Using these four components one can compute various quantities:
 * total energy density = (sum of all components)
 * longitudinal energy density = (sum of longitudinal components)
 * transversal energy density = (sum of transversal components)
 * longitudinal pressure = transversal energy density - longitudinal energy density
 * transvesal pressure = longitudinal energy density
 *
 * The output format:
 * 1) time
 * 2) electric transversal
 * 3) magnetic transversal
 * 4) electric longitudinal
 * 5) magnetic longitudinal
 *
 */
public class ProjectedEnergyDensity implements Diagnostics {

	private int direction;

	private String path;
	private double timeInterval;
	private int stepInterval;
	private int numberOfCells;

	private double[] energyDensity_T_el;
	private double[] energyDensity_T_mag;
	private double[] energyDensity_L_el;
	private double[] energyDensity_L_mag;

	public ProjectedEnergyDensity(String path, double timeInterval, int direction) {
		this.direction = direction;
		this.path = path;
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		numberOfCells = s.grid.getNumCells(direction);
		this.stepInterval = (int) (timeInterval / s.getTimeStep());

		FileFunctions.clearFile("output/" + path);
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {
			// Compute projected energy density (electric, magnetic, transversal and longitudinal)
			energyDensity_T_el = new double[numberOfCells];
			energyDensity_T_mag = new double[numberOfCells];
			energyDensity_L_el = new double[numberOfCells];
			energyDensity_L_mag = new double[numberOfCells];

			for (int i = 0; i < numberOfCells; i++) {
				energyDensity_T_el[i] = 0.0;
				energyDensity_T_mag[i] = 0.0;
				energyDensity_L_el[i] = 0.0;
				energyDensity_L_mag[i] = 0.0;
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
						double electric = 0.5 * grid.getE(i, j).square();
						double magnetic = 0.25 * (grid.getBsquaredFromLinks(i, j, 0) + grid.getBsquaredFromLinks(i, j, 1));
						if(j == direction) {
							e_L_el += electric;
							e_L_mag += magnetic;
						} else {
							e_T_el += electric;
							e_T_mag += magnetic;
						}
					}

					energyDensity_T_el[projIndex] += e_T_el;
					energyDensity_T_mag[projIndex] += e_T_mag;
					energyDensity_L_el[projIndex] += e_L_el;
					energyDensity_L_mag[projIndex] += e_L_mag;
				}
			}

			// Write to file
			File file = FileFunctions.getFile("output/" + path);
			try {
				FileWriter pw = new FileWriter(file, true);
				Double time = steps * grid.getTemporalSpacing();
				pw.write(time.toString() + "\n");
				pw.write(generateTSVString(energyDensity_T_el) + "\n");
				pw.write(generateTSVString(energyDensity_T_mag) + "\n");
				pw.write(generateTSVString(energyDensity_L_el) + "\n");
				pw.write(generateTSVString(energyDensity_L_mag) + "\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("ProjectedEnergyDensity: Error writing to file.");
			}
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
