package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DipoleInitialBinned implements Diagnostics {

	private String path;
	private int direction, orientation;
	private boolean supressOutput;
	private Simulation s;
	private FieldMeasurements fieldMeasurements;

	public DipoleInitialBinned(String path, double timeInterval)
	{
		this(path, timeInterval, false);
	}

	public DipoleInitialBinned(String path, double timeInterval, boolean supressOutput)
	{
		this.path = path;
		this.supressOutput = supressOutput;
	}

	/**
	 * Initializes the DipoleInitialBinned object.
	 * It creates/deletes the output file.
	 *
	 * @param s    Instance of the simulation object
	 */
	public void initialize(Simulation s)
	{
		this.s = s;
		this.fieldMeasurements = new FieldMeasurements();

		if(!supressOutput) {
			// Create/delete file.
			FileFunctions.clearFile(path);

			// Write first line.
			File file = FileFunctions.getFile(path);
			try {
				FileWriter pw = new FileWriter(file, true);
				pw.write("#|x - y| \t tr[V_x V_y^dagger]");
				pw.write("\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("DipoleInitialBinned Error: Could not write to file '" + path + "'.");
			}
		}
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}


	/**
	 * Computes the trace of the dipole amplitude along the longitudinal direction, bins it over the transversal distance and writes the result to the output file.
	 *
	 * @param grid		Reference to the Grid instance.
	 * @param particles	Reference to the list of particles.
	 * @param steps		Total simulation steps so far.
	 * @throws IOException
	 */
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {

		int longitudinalNumCells = grid.getNumCells(direction);
		int[] transverseNumCells = GridFunctions.reduceGridPos(grid.getNumCells(), direction);
		int totalTransverseCells = GridFunctions.getTotalNumberOfCells(transverseNumCells);
		GroupElement tadLink1 = grid.getElementFactory().groupIdentity();
		GroupElement tadLink2 = grid.getElementFactory().groupIdentity();
		GroupElement tadLink3 = grid.getElementFactory().groupIdentity();
		int numBins = (int) Math.sqrt(totalTransverseCells);
		double binWidth = Math.sqrt(2*totalTransverseCells)/numBins;
		double[] dipoleTraces = new double[numBins];
		int[] counter = new int[numBins];
		double as = grid.getLatticeSpacing();
		int[] gridPos1, gridPos2;
		int end = 0;

		for (int i = 0; i < totalTransverseCells; i++) {
			// Current position
			int[] transGridPos1 = GridFunctions.getCellPos(i, transverseNumCells);

			for (int j = 0; (j < totalTransverseCells) && (j >= i); j++) {
				int[] transGridPos2 = GridFunctions.getCellPos(j, transverseNumCells);

				for (int k = 0; k < longitudinalNumCells; k++) {
					int z = (orientation < 0) ? k : (longitudinalNumCells - k - 1);
					gridPos1 = GridFunctions.insertGridPos(transGridPos1, direction, z);
					gridPos2 = GridFunctions.insertGridPos(transGridPos2, direction, z);
					int index1 = grid.getCellIndex(gridPos1);
					int index2 = grid.getCellIndex(gridPos2);
					tadLink1.multAssign(grid.getUnext(index1,0));
					tadLink2.multAssign(grid.getUnext(index2,0));
					end = z;
				}

				tadLink2.adjAssign();

				int dist1 = transGridPos2[0] - transGridPos1[0];			///Attention, this and the following only works in 3 dimensions!!
				int dist2 = transGridPos2[1] - transGridPos1[1];
				double distance = Math.sqrt(dist1*dist1+dist2*dist2);

				for (int k = transGridPos1[0]+1; k <= dist1; k++) {
					int[] transGridPos3 = new int[transGridPos1.length];
					System.arraycopy(transGridPos1, 0, transGridPos3, 0, transGridPos1.length);
					transGridPos3[0] = k;

					int[] gridPos3 = GridFunctions.insertGridPos(transGridPos3, direction, end);
					int index3 = grid.getCellIndex(gridPos3);
					tadLink3.multAssign(grid.getUnext(index3,0));
				}

				for (int k = transGridPos1[1]+1; k <= dist2; k++) {
					int[] transGridPos4 = new int[transGridPos1.length];
					System.arraycopy(transGridPos1, 0, transGridPos4, 0, transGridPos1.length);
					transGridPos4[1] = k;

					int[] gridPos4 = GridFunctions.insertGridPos(transGridPos4, direction, end);
					int index4 = grid.getCellIndex(gridPos4);
					tadLink3.multAssign(grid.getUnext(index4,0));
				}

				int binIndex = (int) Math.floor(distance/binWidth);
				dipoleTraces[binIndex] += tadLink1.mult(tadLink3).mult(tadLink2).getRealTrace()/2;
				counter[binIndex]++;

				tadLink1.set(grid.getElementFactory().groupIdentity());
				tadLink2.set(grid.getElementFactory().groupIdentity());
				tadLink3.set(grid.getElementFactory().groupIdentity());
			}
		}

		for (int i = 0; i < numBins; i++) {
			if(counter[i] != 0) {
				dipoleTraces[i] /= counter[i];
			} else {
				dipoleTraces[i] = 0;
			}
		}

		if(!supressOutput) {
			File file = FileFunctions.getFile(path);
			FileWriter pw = new FileWriter(file, true);
			DecimalFormat formatter = new DecimalFormat("0.################E0");

			for (int i = 0; i < numBins; i++) {
				if(counter[i] != 0) {
					pw.write(formatter.format(i*binWidth*as)+ "\t");
					pw.write(formatter.format(dipoleTraces[i]));
					pw.write("\n");
				}
			}

			pw.close();
		}

	}
}