package org.openpixi.pixi.diagnostics.methods;

import org.apache.commons.io.FilenameUtils;
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
 * This diagnostic computes the energy density for each cell.
 *
 * The output format of this diagnostic is binary to get smaller file sizes. It is arranged as follows:
 *
 * Header:
 * D Number of dimensions (Int32),
 * N Lattice size (Int32 * number of dimensions)
 *
 * Body:
 * \epsilon (N^D * Real64)
 *
 * On a 256^3 grid the filesize will be roughly 0.13 gb.
 *
 * Java writes binary data with Big Endian encoding.
 *
 */
public class EnergyDensity implements Diagnostics {
	private Simulation simulation;
	private String filename;
	private double startTime;
	private double timeInterval;
	private int firstStep;
	private int step;
	private int counter = 0;
	private ComponentComputation componentComputation = new ComponentComputation();


	public EnergyDensity(String filename, double startTime, double timeInterval) {
		this.filename = filename;
		this.startTime = startTime;
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		this.firstStep = (int) Math.max(Math.round((startTime / s.getTimeStep())), 1);
		this.step = (int) Math.round(timeInterval / s.getTimeStep());

		componentComputation.initialize(s.grid);
		this.simulation = s;
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % step == 0) {
			if(steps >= firstStep) {
				// Generate new file name based on counter
				counter++;
				String filename_path = FilenameUtils.getPath(filename);
				String filename_base = FilenameUtils.getBaseName(filename);
				String filename_extension = FilenameUtils.getExtension(filename);
				String new_name = filename_base + "_" + counter + "." + filename_extension;
				String new_filepath = FilenameUtils.concat(filename_path, new_name);
				FileFunctions.clearFile(new_filepath);
				File file = FileFunctions.getFile(new_filepath);

				// Write header
				try {
					DataOutputStream stream = null;
					try {
						stream = getStream(file);
						stream.writeInt(simulation.getNumberOfDimensions());
						for (int d = 0; d < simulation.getNumberOfDimensions(); d++) {
							stream.writeInt(simulation.grid.getNumCells(d));
						}
					} finally {
						stream.flush();
						stream.close();
					}
				} catch (IOException ex) {
					System.out.println("EnergyDensity: Error writing to file.");
				}

				// Calculate
				componentComputation.reset();
				grid.getCellIterator().execute(grid, componentComputation);

				// Write body
				try {
					DataOutputStream stream = null;
					try {
						stream = getStream(file);
						writeBinaryDoubleArray(stream, componentComputation.energy);
					} finally {
						stream.flush();
						stream.close();
					}
				} catch (IOException ex) {
					System.out.println("EnergyDensity: Error writing to file.");
				}
			}
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
		private int numberOfDimensions;
		private double[] unitFactor;

		private double[] energy;

		public void initialize(Grid grid) {
			numberOfCells = grid.getTotalNumberOfCells();
			numberOfDimensions = grid.getNumberOfDimensions();

			energy = new double[numberOfCells];

			unitFactor = new double[numberOfDimensions];
			for (int d = 0; d < numberOfDimensions; d++) {

				unitFactor[d] = Math.pow(grid.getLatticeUnitFactor(0), -2);
			}

			reset();
		}


		public void reset() {
			for (int i = 0; i < numberOfCells; i++) {
				energy[i] = 0.0;
			}
		}

		public void execute(Grid grid, int index) {
			// Use index array to get longitudinal index.
			if(grid.isEvaluatable(index)) {
				for (int d = 0; d < numberOfDimensions; d++) {
					AlgebraElement E = grid.getE(index, d);
					AlgebraElement B = grid.getB(index, d, 0);
					B.addAssign(grid.getB(index, d, 1));

					double Esq = 0.5 * E.square();
					double Bsq = 0.25 * B.square();

					synchronized (this) {
						energy[index] += (Esq + Bsq) * unitFactor[d];
					}
				}
			}
		}
	}
}
