package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.currentgenerators.NucleusLCCurrent;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalParticleColorCurrentNucleus {
	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

	/**
	 * Orientation of the current pulse (-1 or 1)
	 */
	public Integer orientation;

	/**
	 * Starting location of the pulse on the longitudinal line
	 */
	public Double longitudinalLocation;

	/**
	 *	Center of the charge distribution in the transversal plane
	 */
	public ArrayList<Double> transversalLocation;

	/**
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * Transversal radius of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double transversalRadius;

	/**
	 * Transversal surface thickness of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double surfaceThickness;

	/**
	 * Number of nucleons in the nucleus
	 */
	public Integer numberOfNucleons;

	/**
	 * Width of the Gaussian distribution of the valence quark charge
	 */
	public Double partonWidth;

	/**
	 * Width of the Gaussian distribution of the nucleons
	 */
	public Double nucleonWidth;

	/**
	 * Number of colors
	 */
	public Integer numberOfColors;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed = null;

	/**
	 * Density of the color charge
	 */
	public Double colorChargeDensity;

	/**
	 * Option whether to use the dipole removal method.
	 */
	public Boolean useDipoleRemoval = true;

	/**
	 * Option whether to use the monopole removal method.
	 */
	public Boolean useMonopoleRemoval = true;

	/**
	 * Option whether to use the constituent quark model for the nucleons.
	 */
	public Boolean useConstituentQuarks = true;


	public NucleusLCCurrent getCurrentGenerator(Settings set) {
		double[] locationTransverse = new double[transversalLocation.size()];
		for (int j = 0; j < transversalLocation.size(); j++) {
			locationTransverse[j] = transversalLocation.get(j);
		}
		int [] transversalNumCells = GridFunctions.reduceGridPos(set.getGridCells(), direction);
		int start = 0, range;
		if(transversalNumCells[start] > 1) {
			range = transversalNumCells[start];
		} else {
			range = transversalNumCells[start+1];
		}
		for (int j = 0; j < transversalNumCells.length; j++) {
			if( (transversalNumCells[j] > 1) && (transversalNumCells[j] < range) ) {
				range = transversalNumCells[j];
			}
		}

		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		ArrayList<double[]> listOfNucleonLocations = new ArrayList<double[]>();
		for(int i = 0; i < numberOfNucleons; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + getWoodsSaxonMonteCarlo(rand, range*set.getGridStep());
			}
			listOfNucleonLocations.add(chargeLocation);
		}

		NucleusLCCurrent generator = new NucleusLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth, locationTransverse, useMonopoleRemoval, useDipoleRemoval, useConstituentQuarks, rand, transversalRadius, surfaceThickness, colorChargeDensity);


		for(int i = 0; i < numberOfNucleons; i++) {
			generator.addNucleon(listOfNucleonLocations.get(i), nucleonWidth, partonWidth);
		}

		return generator;
	}

	private double getWoodsSaxonMonteCarlo(Random rand, double range) {
		double random1, random2, y;
		do {
			random1 = rand.nextDouble();
			random2 = rand.nextDouble();
			double norm = 2.0*Math.pow(Math.PI, transversalLocation.size() - 1)/surfaceThickness*Math.log(1.0 + Math.exp(transversalRadius/surfaceThickness));
			//double range = transversalRadius + surfaceThickness*Math.log(1.0/(10e-10*norm) - 1.0);
			random1 *= range;
			random2 /= norm;
			y = 1.0/(norm*(Math.exp((random1 - transversalRadius)/surfaceThickness) + 1));
		} while (random2 > y);

		double randSign = Math.signum(rand.nextDouble() - 0.5);//TODO: Make this method spherical!!

		return random1*randSign;
	}


}
