package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This diagnostic averages the energy density over the transversal plane (specified by the longitudinal direction)
 * and writes 4 energy density components to a file:
 * Electric longitudinal (energyDensity_L_el), magnetic longitudinal (energyDensity_L_mag),
 * electric transversal  (energyDensity_T_el) and magnetic transversal (energyDensity_T_mag) energy density.
 *
 * Using these four components one can compute various quantities:
 * total energy density = (sum of all components)
 * longitudinal energy density = (sum of longitudinal components)
 * transversal energy density = (sum of transversal components)
 * longitudinal pressure = transversal energy density - longitudinal energy density
 * transvesal pressure = longitudinal energy density
 *
 * It also outputs the longitudinal Poynting vector component (Averaged & Only time-averaged).
 *
 * The output format:
 * 1) time
 * 2) electric transversal
 * 3) magnetic transversal
 * 4) electric longitudinal
 * 5) magnetic longitudinal
 * 6) longitudinal poynting vector (averaged)
 * 7) longitudinal poynting vector (only time-averaged)
 *
 */
public class ProjectedEnergyDensity implements Diagnostics {

	private int direction;

	private String path;
	private double timeInterval;
	private int stepInterval;

	private EnergyDensityComputation energyDensityComputation;
	private PoyntingComputation poyntingComputation;


	protected double areaFactor;


	public ProjectedEnergyDensity(String path, double timeInterval, int direction) {
		this.direction = direction;
		this.path = path;
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		this.stepInterval = (int) (timeInterval / s.getTimeStep());

		this.energyDensityComputation = new EnergyDensityComputation();
		energyDensityComputation.initialize(s.grid, direction);
		this.poyntingComputation = new PoyntingComputation();
		poyntingComputation.initialize(s.grid, direction);

		FileFunctions.clearFile("output/" + path);

		// Compute area factor
		areaFactor = 1.0;
		for (int i = 0; i < s.getNumberOfDimensions(); i++) {
			if(i != direction) {
				areaFactor *= s.grid.getNumCells(i);
			}
		}
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {

			energyDensityComputation.reset();
			grid.getCellIterator().execute(grid, energyDensityComputation);

			poyntingComputation.reset();
			grid.getCellIterator().execute(grid, poyntingComputation);


			// Write to file
			File file = FileFunctions.getFile("output/" + path);
			try {
				FileWriter pw = new FileWriter(file, true);
				Double time = steps * grid.getTemporalSpacing();
				pw.write(time.toString() + "\n");
				pw.write(FileFunctions.generateTSVString(energyDensityComputation.energyDensity_T_el) + "\n");
				pw.write(FileFunctions.generateTSVString(energyDensityComputation.energyDensity_T_mag) + "\n");
				pw.write(FileFunctions.generateTSVString(energyDensityComputation.energyDensity_L_el) + "\n");
				pw.write(FileFunctions.generateTSVString(energyDensityComputation.energyDensity_L_mag) + "\n");
				pw.write(FileFunctions.generateTSVString(poyntingComputation.poyntingAveraged) + "\n");
				pw.write(FileFunctions.generateTSVString(poyntingComputation.poyntingTimeAveraged) + "\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("ProjectedEnergyDensity: Error writing to file.");
			}
		}
	}

	// Multithreaded code which leaks memory. I don't know why.
	private class EnergyDensityComputation implements CellAction {

		private int direction;
		private int numberOfCells;
		private double[] energyDensity_T_el;
		private double[] energyDensity_T_mag;
		private double[] energyDensity_L_el;
		private double[] energyDensity_L_mag;

		public void initialize(Grid grid, int direction) {
			this.direction = direction;
			this.numberOfCells = grid.getNumCells(this.direction);
			this.energyDensity_T_el = new double[numberOfCells];
			this.energyDensity_T_mag = new double[numberOfCells];
			this.energyDensity_L_el = new double[numberOfCells];
			this.energyDensity_L_mag = new double[numberOfCells];

		}

		public void reset() {
			for (int i = 0; i < numberOfCells; i++) {
				this.energyDensity_T_el[i] = 0.0;
				this.energyDensity_T_mag[i] = 0.0;
				this.energyDensity_L_el[i] = 0.0;
				this.energyDensity_L_mag[i] = 0.0;
			}
		}

		public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				int projIndex = grid.getCellPos(index)[direction];
				// transversal & longitudinal electric energy density
				double e_T_el = 0.0;
				double e_L_el = 0.0;
				// transversal & longitudinal magnetic energy density
				double e_T_mag = 0.0;
				double e_L_mag = 0.0;

				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					double unitFactor = Math.pow(grid.getLatticeUnitFactor(j), -2);
					double electric = 0.5 * grid.getE(index, j).square() * unitFactor;
					double magnetic = 0.25 * (grid.getBsquaredFromLinks(index, j, 0) + grid.getBsquaredFromLinks(index, j, 1)) * unitFactor;
					if(j == direction) {
						e_L_el += electric;
						e_L_mag += magnetic;
					} else {
						e_T_el += electric;
						e_T_mag += magnetic;
					}
				}

				synchronized (this) {
					energyDensity_T_el[projIndex] += e_T_el;
					energyDensity_T_mag[projIndex] += e_T_mag;
					energyDensity_L_el[projIndex] += e_L_el;
					energyDensity_L_mag[projIndex] += e_L_mag;
				}
			}
		}
	}


	private class PoyntingComputation implements CellAction {

		private int direction;
		private int numberOfDimensions;
		private int numberOfCells;
		private double[] poyntingAveraged;
		private double[] poyntingTimeAveraged;
		private ElementFactory factory;

		public void initialize(Grid grid, int direction) {
			this.direction = direction;
			this.numberOfCells = grid.getNumCells(direction);
			this.poyntingAveraged = new double[numberOfCells];
			this.poyntingTimeAveraged = new double[numberOfCells];
			this.numberOfDimensions = grid.getNumberOfDimensions();
			this.factory = grid.getElementFactory();
		}

		public void reset() {
			for (int i = 0; i < numberOfCells; i++) {
				this.poyntingAveraged[i] = 0.0;
				this.poyntingTimeAveraged[i] = 0.0;
			}
		}

		public void execute(Grid grid, int index) {
			double localPoyntingAveraged = 0.0;
			double localPoyntingTimeAveraged = 0.0;

			int d = direction;
			for (int i = 0; i < numberOfDimensions; i++) {
				if (i != direction) {
					// AVERAGED COMPUTATION

					// Average spatial components of field strength tensor in space and time (B-Field).
					GroupElement FG = factory.groupZero();

					// Spatial average at t-at/2
					FG.addAssign(grid.getPlaquette(index, d, i, 1, 1, 0));

					FG.addAssign(grid.getPlaquette(index, i, d, -1, 1, 0));
					FG.addAssign(grid.getPlaquette(index, i, d, 1, -1, 0));
					FG.addAssign(grid.getPlaquette(index, d, i, -1, -1, 0));

					// Spatial average at t+at/2
					FG.addAssign(grid.getPlaquette(index, d, i, 1, 1, 1));
					FG.addAssign(grid.getPlaquette(index, i, d, -1, 1, 1));
					FG.addAssign(grid.getPlaquette(index, i, d, 1, -1, 1));
					FG.addAssign(grid.getPlaquette(index, d, i, -1, -1, 1));

					// Divide by factors and convert to AlgebraElement.
					double unitFactorF = grid.getCellArea(d, i ) * grid.getGaugeCoupling();
					AlgebraElement F = FG.proj().mult(1.0 / (8.0 * unitFactorF));

					// Average E field in space.
					double unitFactorE = grid.getLatticeUnitFactor(i);
					int shiftedIndex = grid.shift(index, i, -1);
					AlgebraElement E1 = grid.getE(index, i);
					AlgebraElement E2 = grid.getE(shiftedIndex, i).act(grid.getLink(index, i, -1, 0));
					AlgebraElement E = E1.add(E2).mult(0.5 * unitFactorE);

					// Multiply E and F;
					localPoyntingAveraged += E.mult(F);
				}
			}

			// TIME-AVERAGED COMPUTATION:

			// Indices for cross product:
			int dir1 = (direction + 1) % 3;
			int dir2 = (direction + 2) % 3;
			double unitFactor1 = grid.getLatticeUnitFactor(dir1);
			double unitFactor2 = grid.getLatticeUnitFactor(dir2);

			// fields at same time:
			AlgebraElement E1 = grid.getE(index, dir1).mult(unitFactor1);
			AlgebraElement E2 = grid.getE(index, dir2).mult(unitFactor2);
			// time averaged B-field:
			AlgebraElement B1 = grid.getB(index, dir1, 0).add(grid.getB(index, dir1, 1)).mult(unitFactor1);
			AlgebraElement B2 = grid.getB(index, dir2, 0).add(grid.getB(index, dir2, 1)).mult(unitFactor2);

			localPoyntingTimeAveraged = E1.mult(B2) - E2.mult(B1);

			// Add to array.
			int projIndex = grid.getCellPos(index)[direction];
			synchronized (this) {
				this.poyntingAveraged[projIndex] += localPoyntingAveraged;
				this.poyntingTimeAveraged[projIndex] += localPoyntingTimeAveraged;

			}
		}
	}

}
