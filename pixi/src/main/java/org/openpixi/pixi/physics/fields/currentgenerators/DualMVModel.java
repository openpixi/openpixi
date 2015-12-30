package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.SU2AlgebraElement;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DualMVModel implements ICurrentGenerator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	private int direction;

	/**
	 * Longitudinal location of the initial charge density in the simulation box.
	 */
	private double location;

	/**
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * \mu parameter of the MV model.
	 */
	private double mu;

	/**
	 * Seed for the random variables.
	 */
	private boolean useSeed = false;
	private int seed1;
	private int seed2;


	/**
	 * Low pass filter for the Poisson solver.
	 */
	private double lowPassCoefficient;

	/**
	 * Initial condition output
	 */
	private String outputFile;
	private boolean createInitialConditionsOutput;

	private MVModel mv1;
	private MVModel mv2;


	public DualMVModel(int direction, double location, double longitudinalWidth, double mu, double lowPassCoefficient,
					   boolean useSeed, int seed1, int seed2, boolean createInitialConditionsOutput, String outputFile){
		this.direction = direction;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.mu = mu;
		this.lowPassCoefficient = lowPassCoefficient;

		this.useSeed = useSeed;
		this.seed1 = seed1;
		this.seed2 = seed2;

		this.createInitialConditionsOutput = createInitialConditionsOutput;
		this.outputFile = outputFile;
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		if(useSeed){
			mv1 = new MVModel(direction, 1, location, longitudinalWidth, mu, seed1, lowPassCoefficient);
			mv2 = new MVModel(direction, -1, -(location+1), longitudinalWidth, mu, seed2, lowPassCoefficient);
		} else {
			mv1 = new MVModel(direction, 1, location, longitudinalWidth, mu, lowPassCoefficient);
			mv2 = new MVModel(direction, -1, -(location+1), longitudinalWidth, mu, lowPassCoefficient);
		}

		mv1.initializeCurrent(s, totalInstances);
		mv2.initializeCurrent(s, totalInstances);

		double g = s.getCouplingConstant();

		// Compute boost-invariant initial conditions. Note: this only works for SU2.
		if(createInitialConditionsOutput && s.getNumberOfColors() == 2) {

			double normalizationFactor = 1.0 / s.getCouplingConstant() * s.grid.getLatticeSpacing();
			int[] transNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
			int transverseNumberOfCells = GridFunctions.getTotalNumberOfCells(transNumCells);
			int effDimensions = GridFunctions.getEffectiveNumberOfDimensions(s.grid.getNumCells());

			GroupElement zero = (SU2GroupElement) s.grid.getElementFactory().groupZero(2);
			GroupElement identity = (SU2GroupElement) s.grid.getElementFactory().groupIdentity(2);

			// Transverse links
			SU2GroupElement[][] transverseLinks = new SU2GroupElement[effDimensions - 1][transverseNumberOfCells];
			SU2AlgebraElement[][] transverseFields = new SU2AlgebraElement[effDimensions - 1][transverseNumberOfCells];

			// Longitudinal E-Fields
			SU2AlgebraElement[] longitudinalFields = new SU2AlgebraElement[transverseNumberOfCells];

			NewLCPoissonSolver ps1 = mv1.particleLCCurrent.poissonSolver;
			NewLCPoissonSolver ps2 = mv2.particleLCCurrent.poissonSolver;

			// Compute transverse link elements
			for (int i = 0; i < transverseNumberOfCells; i++) {
				int ts = 0;
				for (int j = 0; j < effDimensions; j++) {
					if(j != direction) {
						// Compute initial condition for transverse fields
						int is = GridFunctions.shift(i, ts, 1, transNumCells);
						SU2GroupElement U1 = (SU2GroupElement) ps1.getV(i).mult(ps1.getV(is).adj());
						SU2GroupElement U2 = (SU2GroupElement) ps2.getV(i).mult(ps2.getV(is).adj());

						SU2GroupElement sum = (SU2GroupElement) U1.add(U2);
						SU2GroupElement sumInv = ((SU2GroupElement) sum.adj()).inv();

						transverseLinks[ts][i] = (SU2GroupElement) sum.mult(sumInv);
						transverseFields[ts][i] = (SU2AlgebraElement) transverseLinks[ts][i].getAlgebraElement().mult(normalizationFactor);
						ts++;
					}
				}
			}

			// Compute longitudinal fields
			for (int i = 0; i < transverseNumberOfCells; i++) {
				SU2GroupElement temp = (SU2GroupElement) zero.copy();
				//SU2AlgebraElement temp2 = (SU2AlgebraElement) s.grid.getElementFactory().algebraZero(2);

				int ts = 0;
				for (int j = 0; j < effDimensions; j++) {
					if(j != direction) {
						/*
						// Lattice calculation 1
						int is_a = GridFunctions.shift(i, ts, 1, transNumCells);
						SU2GroupElement Um1 = (SU2GroupElement) transverseLinks[ts][i].sub(identity);
						SU2GroupElement U1 = (SU2GroupElement) ps1.getV(i).mult(ps1.getV(is_a).adj());
						SU2GroupElement U2 = (SU2GroupElement) ps2.getV(i).mult(ps2.getV(is_a).adj());
						SU2GroupElement diff1 = (SU2GroupElement) U2.adj().sub(U1.adj());

						int is_b = GridFunctions.shift(i, ts, -1, transNumCells);
						SU2GroupElement Um2 = (SU2GroupElement) transverseLinks[ts][is_b].adj().sub(identity);
						SU2GroupElement U3 = (SU2GroupElement) ps1.getV(is_b).mult(ps1.getV(i).adj());
						SU2GroupElement U4 = (SU2GroupElement) ps2.getV(is_b).mult(ps2.getV(i).adj());
						SU2GroupElement diff2 = (SU2GroupElement) U4.sub(U3);

						temp.addAssign(Um1.mult(diff1).add(Um2.mult(diff2)));
						*/


						// Lattice calculation 2
						int is = GridFunctions.shift(i, ts, -1, transNumCells);
						SU2GroupElement Um1 = (SU2GroupElement) transverseLinks[ts][i].adj().sub(identity);
						SU2GroupElement diff1 = (SU2GroupElement) ps1.getU(i, ts).sub(ps2.getU(i, ts));
						SU2GroupElement Um2 = (SU2GroupElement) transverseLinks[ts][is].adj().sub(identity);
						SU2GroupElement diff2 = (SU2GroupElement) ps1.getU(is, ts).sub(ps2.getU(is, ts));
						temp.addAssign(diff1.mult(Um1).sub(Um2.mult(diff2)));


						// Continuum calculation
						/*
						SU2AlgebraElement A1 = (SU2AlgebraElement) ps1.getU(i, ts).getAlgebraElement().mult(normalizationFactor);
						SU2AlgebraElement A2 = (SU2AlgebraElement) ps2.getU(i, ts).getAlgebraElement().mult(normalizationFactor);
						temp2.addAssign(comm(A1,A2));
						*/
						ts++;
					}
				}

				longitudinalFields[i] = (SU2AlgebraElement) temp.proj().mult(normalizationFactor / (2.0 * g));
				//longitudinalFields[i] = temp2;
			}

			// File output ((d-1)x3 transversal gauge field components, 1x3 longitudinal electric field component)
			clear();
			File file = this.getOutputFile(outputFile);
			try {
				FileWriter pw = new FileWriter(file, true);

				// Transverse fields
				for (int i = 0; i < effDimensions-1; i++) {
					double[][] output = convertToDoubleArray(transverseFields[i]);
					pw.write(generateTSVString(output[0]) + "\n");
					pw.write(generateTSVString(output[1]) + "\n");
					pw.write(generateTSVString(output[2]) + "\n");
				}

				// Longitudinal fields
				double[][] output = convertToDoubleArray(longitudinalFields);
				pw.write(generateTSVString(output[0]) + "\n");
				pw.write(generateTSVString(output[1]) + "\n");
				pw.write(generateTSVString(output[2]) + "\n");

				pw.close();
			} catch (IOException ex) {
				System.out.println("DualMVModel: Error writing to file.");
			}

		}
	}

	public void applyCurrent(Simulation s) {
		mv1.applyCurrent(s);
		mv2.applyCurrent(s);
	}

	private SU2AlgebraElement comm(SU2AlgebraElement A, SU2AlgebraElement B) {
		SU2GroupElement a = new SU2GroupElement(0.0, A.get(0), A.get(1), A.get(2));
		SU2GroupElement b = new SU2GroupElement(0.0, B.get(0), B.get(1), B.get(2));
		SU2GroupElement c = (SU2GroupElement) a.mult(b).sub(b.mult(a));
		return (SU2AlgebraElement) c.proj().mult(1.0);
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
	private void clear() {
		File file = getOutputFile(outputFile);
		boolean fileExists1 = file.exists();
		if(fileExists1 == true) {
			file.delete();
		}
	}

	private String generateTSVString(double[] array) {
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

	private double[][] convertToDoubleArray(AlgebraElement[] array) {
		double[][] output = new double[3][array.length];
		for (int i = 0; i < array.length; i++) {
			output[0][i] = array[i].get(0);
			output[1][i] = array[i].get(1);
			output[2][i] = array[i].get(2);
		}
		return output;
	}

}
