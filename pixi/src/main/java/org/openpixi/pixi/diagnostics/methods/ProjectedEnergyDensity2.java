package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.*;
import java.util.ArrayList;

/**
 * This diagnostic averages the energy density over the transversal plane with x being the longitudinal direction
 * and writes various energy densities and other components to a file:
 *
 * 1) 0.5 ET^2 (transverse electric field squared)
 * 2) 0.5 BT^2 (transverse magnetic field squared)
 * 3) 0.5 EL^2 (longitudinal electric field squared)
 * 4) 0.5 BL^2 (longitudinal electric field squared)
 * 5) SL       (longitudinal Poynting vector component)
 * 6) jL * EL  (power input)
 *
 * Combining these components one can compute all kinds of relevant energy-momentum tensor components:
 * pT, pL, eT, eL, ratios of fields, etc..
 *
 * It is also possible to compute longitudinally boosted quantities such as the local rest-frame energy density or
 * the energy density in the tau eta comoving frame.
 *
 * This is an updated version of the ProjectedEnergyDensity diagnostic.
 * Whats new:
 * Improved Poynting vector calculation
 * Correctly shifted/averaged fields
 * Improved way to traverse the lattice
 *
 * Note: this diagnostic relies on some assumptions:
 * the simulation is in 3D
 * x is the longitudinal coordinate
 * transverse lattice spacings are the same in both directions
 *
 * The output of this diagnostic is a binary file to get smaller file sizes. It is arranged as follows:
 *
 * Header:
 * Longitudinal lattice size (Int32), Number of time steps (Int32)
 *
 * For every time step:
 * Time (Real64), ET (NT * Real64), BT (NT * Real64), EL (NT * Real64), BL (NT * Real64), SL (NT * Real64), JE (NT * Real64)
 *
 * Java writes binary data with Big Endian encoding.
 *
 */
public class ProjectedEnergyDensity2 implements Diagnostics {
	private String path;
	private double timeInterval;
	private int stepInterval;
	private int maxWrites;
	private int writes;
	private ComponentComputation componentComputation = new ComponentComputation();


	public ProjectedEnergyDensity2(String path, double timeInterval) {
		this.path = path;
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		this.stepInterval = (int) Math.max(Math.round((timeInterval / s.getTimeStep())), 1);
		maxWrites = s.getIterations() / stepInterval;
		writes = 0;


		componentComputation.initialize(s.grid);

		FileFunctions.clearFile(path);
		File file = FileFunctions.getFile(path);
		writeBinaryHeader(file, s);
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0 && writes < maxWrites) {
			componentComputation.reset();
			grid.getCellIterator().execute(grid, componentComputation);
			componentComputation.finalizeArrays(grid);

			// Write to file
			File file = FileFunctions.getFile(path);



			try {
				DataOutputStream stream = null;
				try {
					stream = getStream(file);
					double time = steps * grid.getTemporalSpacing();
					stream.writeDouble(time);
					writeBinaryDoubleArray(stream, componentComputation.ET);
					writeBinaryDoubleArray(stream, componentComputation.BT);
					writeBinaryDoubleArray(stream, componentComputation.EL);
					writeBinaryDoubleArray(stream, componentComputation.BL);
					writeBinaryDoubleArray(stream, componentComputation.SL);
					writeBinaryDoubleArray(stream, componentComputation.JE);
				} finally {
					stream.flush();
					stream.close();
					writes++;
				}
			} catch (IOException ex) {
				System.out.println("ProjectedEnergyDensity2: Error writing to file.");
			}
		}
	}

	private void writeBinaryHeader(File file, Simulation s) {
		int longitudinalCells = s.grid.getNumCells(0);
		int timesteps = s.getIterations() / stepInterval;
		try {
			DataOutputStream stream = null;
			try {
				stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
				stream.writeInt(longitudinalCells);
				stream.writeInt(timesteps);
			} finally {
				stream.flush();
				stream.close();
			}
		} catch (IOException ex) {
			System.out.println("ProjectedEnergyDensity2: Error writing to file.");
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

	private void writeBinaryDoubleArray(File file, double[] array) {
		try {
			DataOutputStream stream = null;
			try {
				stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				for (int i = 0; i < array.length; i++) {
					stream.writeDouble(array[i]);
				}
			} finally {
				stream.close();
			}
		} catch (IOException ex) {
			System.out.println("ProjectedEnergyDensity2: Error writing to file.");
		}
	}

	private class ComponentComputation implements CellAction {
		private int numberOfCells;
		private int longitudinalCells;
		private int[] numCells;

		private int[] indexArray;
		private int[] longitudinalIndexArray;

		private double[] ET;
		private double[] BT;
		private double[] EL;
		private double[] BL;
		private double[] SL;
		private double[] JE;

		public void initialize(Grid grid) {
			numCells = grid.getNumCells();
			numberOfCells = grid.getTotalNumberOfCells();
			longitudinalCells = numCells[0];

			initializeIndexArray(grid);

			ET = new double[longitudinalCells];
			BT = new double[longitudinalCells];
			EL = new double[longitudinalCells];
			BL = new double[longitudinalCells];
			SL = new double[longitudinalCells];
			JE = new double[longitudinalCells];

			reset();
		}


		public void reset() {
			for (int i = 0; i < longitudinalCells; i++) {
				ET[i] = 0.0;
				BT[i] = 0.0;
				EL[i] = 0.0;
				BL[i] = 0.0;
				SL[i] = 0.0;
				JE[i] = 0.0;
			}
		}

		public void finalizeArrays(Grid grid) {
			multiplyArraysWithFactors(grid);
			shiftFields();
		}

		public void execute(Grid grid, int oldindex) {
			int index = indexArray[oldindex];
			int lindex = longitudinalIndexArray[index];
			if(grid.isEvaluatable(index)) {
				// Field components
				double ExSq = grid.getE(index, 0).square();
				double EySq = grid.getE(index, 1).square();
				double EzSq = grid.getE(index, 2).square();

				double BxSq0 = grid.getB(index, 0, 0).square();
				double BySq0 = grid.getB(index, 1, 0).square();
				double BzSq0 = grid.getB(index, 2, 0).square();

				double BxSq1 = grid.getB(index, 0, 1).square();
				double BySq1 = grid.getB(index, 1, 1).square();
				double BzSq1 = grid.getB(index, 2, 1).square();

				// Poynting vector
				int iShiftX = grid.shift(index, 0, 1);

				AlgebraElement Ey0 = grid.getE(index, 1);
				AlgebraElement Ey1 = grid.getE(iShiftX, 1);
				AlgebraElement Ez0 = grid.getE(index, 2);
				AlgebraElement Ez1 = grid.getE(iShiftX, 2);

				AlgebraElement Ey = Ey0.add(Ey1);
				AlgebraElement Ez = Ez0.add(Ez1);

				AlgebraElement By0 = grid.getB(index, 1, 0);
				AlgebraElement By1 = grid.getB(index, 1, 1);
				AlgebraElement Bz0 = grid.getB(index, 2, 0);
				AlgebraElement Bz1 = grid.getB(index, 2, 1);

				AlgebraElement By = By0.add(By1);
				AlgebraElement Bz = Bz0.add(Bz1);

				double SL1 = - Ez.mult(By);
				double SL2 = Ey.mult(Bz);

				// Power input (not correctly time-averaged!)
				AlgebraElement Ex = grid.getE(index, 0);
				AlgebraElement jx = grid.getJ(index, 0);

				double jInE = jx.mult(Ex);

				// Synchronized write to arrays
				synchronized (this) {
					ET[lindex] += EySq + EzSq;
					BT[lindex] += BySq0 + BzSq0 + BySq1 + BzSq1;
					EL[lindex] += ExSq;
					BL[lindex] += BxSq0 + BxSq1;

					SL[lindex] += SL1 + SL2;
					JE[lindex] += jInE;
				}
			}
		}

		private void initializeIndexArray(Grid grid) {
			indexArray = new int[numberOfCells];
			longitudinalIndexArray = new int[numberOfCells];

			int index = 0;
			for (int x = 0; x < numCells[0]; x++) {
				for (int y = 0; y < numCells[1]; y++) {
					for (int z = 0; z < numCells[2]; z++) {
						int[] gridPos = new int[]{x, y, z};
						int cellIndex = grid.getCellIndex(gridPos);
						indexArray[index] = cellIndex;
						longitudinalIndexArray[index] = x;
						index++;
					}
				}
			}
		}

		private void multiplyArraysWithFactors(Grid grid) {
			double cellFactor = grid.getTotalNumberOfCells() / ((double) grid.getNumCells(0));
			double unitFactorLSquared = Math.pow(grid.getLatticeUnitFactor(0), -2) / cellFactor;
			double unitFactorTSquared = Math.pow(grid.getLatticeUnitFactor(1), -2) / cellFactor;
			double unitFactorL = 1.0 / (cellFactor * grid.getLatticeUnitFactor(0));

			for (int i = 0; i < longitudinalCells; i++) {
				ET[i] *= 0.5 * unitFactorTSquared;
				BT[i] *= 0.25 * unitFactorTSquared;
				EL[i] *= 0.5 * unitFactorLSquared;
				BL[i] *= 0.25 * unitFactorLSquared;
				SL[i] *= 0.25 * unitFactorTSquared;
				JE[i] *= unitFactorL;
			}
		}

		private void shiftFields() {
			/*
			Before shifting:
			JE, SL, EL, BT are half-shifted
			ET, BL are unshifted

			After shifting:
			All quantities are defined at to unshifted lattice points
			*/


			shiftArray(EL);
			shiftArray(BT);
			shiftArray(SL);
			shiftArray(JE);
		}

		private void shiftArray(double[] array) {
			double[] source = array.clone();
			int n = array.length;

			for (int i = 1; i < n; i++) {
				array[i] = (source[i] + source[i - 1]) * 0.5;
			}

			array[0] = (source[n-1] + source[0]) * 0.5;
		}
	}
}
