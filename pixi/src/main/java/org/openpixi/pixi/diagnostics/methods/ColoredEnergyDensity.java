package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.*;
import java.util.ArrayList;

/**
 * This diagnostic computes energy density color components for each cell.
 *
 * The output format of this diagnostic is binary to get smaller file sizes. It is arranged as follows:
 *
 * Header:
 * NComp Number of color components (Int32),
 * D Number of dimensions (Int32),
 * N Lattice size (Int32 * number of dimensions)
 *
 * \epsilon_0 (N^D * Real64), \epsilon_1 (N^D * Real64), ..., \epsilon_NComp (N^D * Real64)
 *
 * On a 256^3 grid with SU(2) fields the filesize will be roughly 0.4 gb.
 *
 * Java writes binary data with Big Endian encoding.
 *
 */
public class ColoredEnergyDensity implements Diagnostics {
	private String path;
	private double timeInstant;
	private int step;
	private ComponentComputation componentComputation = new ComponentComputation();


	public ColoredEnergyDensity(String path, double timeInstant) {
		this.path = path;
		this.timeInstant = timeInstant;
	}

	public void initialize(Simulation s) {
		this.step = (int) Math.max(Math.round((timeInstant / s.getTimeStep())), 1);

		componentComputation.initialize(s.grid);

		FileFunctions.clearFile(path);
		File file = FileFunctions.getFile(path);
		writeBinaryHeader(file, s);
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps == step) {
			componentComputation.reset();
			grid.getCellIterator().execute(grid, componentComputation);

			// Write to file
			File file = FileFunctions.getFile(path);

			try {
				DataOutputStream stream = null;
				try {
					stream = getStream(file);
					for (int c = 0; c < grid.getElementFactory().numberOfComponents; c++) {
						writeBinaryDoubleArray(stream, componentComputation.energy[c]);
					}
				} finally {
					stream.flush();
					stream.close();
				}
			} catch (IOException ex) {
				System.out.println("ColoredEnergyDensity: Error writing to file.");
			}
		}
	}

	private void writeBinaryHeader(File file, Simulation s) {
		try {
			DataOutputStream stream = null;
			try {
				stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
				stream.writeInt(s.grid.getElementFactory().numberOfComponents);
				stream.writeInt(s.getNumberOfDimensions());
				for (int d = 0; d < s.getNumberOfDimensions(); d++) {
					stream.writeInt(s.grid.getNumCells(d));
				}
			} finally {
				stream.flush();
				stream.close();
			}
		} catch (IOException ex) {
			System.out.println("ColoredEnergyDensity: Error writing to file.");
		}
	}

	private DataOutputStream getStream(File file) throws IOException {
		DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
		return stream;
	}

	private void writeBinaryDoubleArray(DataOutputStream stream, double[] array) throws IOException {
		for (int i = 0; i < array.length; i++) {
			stream.writeDouble(array[i]);
		}
	}

	private class ComponentComputation implements CellAction {

		private int numberOfCells;
		private int numberOfComponents;
		private int numberOfDimensions;
		private double[] unitFactor;

		private double[][] energy;

		public void initialize(Grid grid) {
			numberOfCells = grid.getTotalNumberOfCells();
			numberOfComponents = grid.getElementFactory().numberOfComponents;
			numberOfDimensions = grid.getNumberOfDimensions();

			energy = new double[numberOfComponents][numberOfCells];

			unitFactor = new double[numberOfDimensions];
			for (int d = 0; d < numberOfDimensions; d++) {

				unitFactor[d] = Math.pow(grid.getLatticeUnitFactor(0), -2);
			}

			reset();
		}


		public void reset() {
			for (int c = 0; c < numberOfComponents; c++) {
				for (int i = 0; i < numberOfCells; i++) {
					energy[c][i] = 0.0;
				}
			}
		}

		public void execute(Grid grid, int index) {
			// Use index array to get longitudinal index.
			if(grid.isEvaluatable(index)) {
				for (int d = 0; d < numberOfDimensions; d++) {
					AlgebraElement E = grid.getE(index, d);
					AlgebraElement B = grid.getB(index, d, 0);
					B.addAssign(grid.getB(index, d, 1));

					for (int c = 0; c < numberOfComponents; c++) {
						double Esq = 0.5 * Math.pow(E.get(c), 2);
						double Bsq = 0.25 * Math.pow(B.get(c), 2);

						synchronized (this) {
							energy[c][index] += (Esq + Bsq) * unitFactor[d];
						}
					}
				}
			}
		}
	}
}
