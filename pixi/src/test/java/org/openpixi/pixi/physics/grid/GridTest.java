package org.openpixi.pixi.physics.grid;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2RandomFields;
import org.openpixi.pixi.physics.fields.TemporalYangMillsSolver;
import org.openpixi.pixi.physics.solver.relativistic.LeapFrogRelativistic;

public class GridTest {

	private final double accuracy = 1.E-13;

	@Test
	public void testLinkMethods() {
		// Initialize simulation
		Settings settings = getStandardSettings();
		settings.addFieldGenerator(new SU2RandomFields());
		Simulation s = new Simulation(settings);
		Grid g = s.grid;


		//Test for getLinearizedLink() method and shift() method
		int numberOfTests = 100;
		for (int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos = getRandomLatticePosition(s);
			int index = g.getCellIndex(pos);

			// Choose random direction
			int dir = (int) (Math.random() * s.getNumberOfDimensions());

			// Shift position
			int shiftedIndex = g.shift(index, dir, 1);

			GroupElement l1 = g.getLink(index, dir, 1, 0);
			GroupElement l2 = g.getLink(shiftedIndex, dir, -1, 0);
			l2.adjAssign();

			// This code is specific to SU2
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(((SU2GroupElement) l1).get(i), ((SU2GroupElement) l2).get(i), accuracy);
			}
		}

		// Test for getPlaquette() method
		numberOfTests = 50;
		for (int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos = getRandomLatticePosition(s);
			int index = g.getCellIndex(pos);

			// Choose random directions
			int d1 = (int) (Math.random() * s.getNumberOfDimensions());
			int d2 = (int) (Math.random() * s.getNumberOfDimensions());

			// These two plaquettes should be the inverse of each other.
			GroupElement plaq1 = g.getPlaquette(index, d1, d2, 1, 1, 0);
			GroupElement plaq2 = g.getPlaquette(index, d2, d1, 1, 1, 0);

			GroupElement result = plaq1.mult(plaq2);

			// This code is specific to SU2
			Assert.assertEquals(1.0, ((SU2GroupElement) result).get(0), accuracy);
			Assert.assertEquals(0.0, ((SU2GroupElement) result).get(1), accuracy);
			Assert.assertEquals(0.0, ((SU2GroupElement) result).get(2), accuracy);
			Assert.assertEquals(0.0, ((SU2GroupElement) result).get(3), accuracy);

			// Forward and backward plaquette around the same rectangle should have the same trace.
			int shiftedIndex = g.shift(index, d1, 1);

			GroupElement plaq3 = g.getPlaquette(shiftedIndex, d1, d2, -1, 1, 0);

			Assert.assertEquals(plaq1.getRealTrace(), plaq3.getRealTrace(), accuracy);
		}
	}

	@Test
	public void testCellIndexMethods()
	{
		int numberOfTests = 10;

		Settings settings = getStandardSettings();
		settings.addFieldGenerator(new SU2RandomFields());
		Simulation s = new Simulation(settings);
		Grid g = s.grid;

		// Test getCellIndex() and getCellPos()
		for(int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos = getRandomLatticePosition(s);

			// Compute cell index from position
			int index = g.getCellIndex(pos);

			// Compute lattice position from cell index
			int[] pos2 = g.getCellPos(index);

			// Test if equal
			Assert.assertArrayEquals(pos, pos2);
		}
	}

	@Test
	public void testShiftAndPeriodic()
	{
		int numberOfTests = 10;

		Settings settings = getStandardSettings();
		settings.addFieldGenerator(new SU2RandomFields());
		Simulation s = new Simulation(settings);
		Grid g = s.grid;

		// Test periodic()
		for(int i = 0; i < g.getNumberOfDimensions(); i++)
		{
			int[] pos = new int[g.getNumberOfDimensions()];
			pos[i] = -1;
			int[] pos2 = g.periodic(pos);

			Assert.assertEquals(g.getNumCells(i) + pos[i], pos2[i]);
		}
		for(int i = 0; i < g.getNumberOfDimensions(); i++)
		{
			int[] pos = new int[g.getNumberOfDimensions()];
			pos[i] = g.getNumCells(i) ;
			int[] pos2 = g.periodic(pos);

			Assert.assertEquals(0, pos2[i]);
		}


		// Test shift()
		for(int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos0 = getRandomLatticePosition(s);

			// Choose random direction
			int d = (int) (Math.random() * s.getNumberOfDimensions());

			int[] pos2 = g.shift(pos0, d, 1);
			int[] pos3 = g.shift(pos2, d, -1);

			// Test if equal
			Assert.assertArrayEquals(pos0, pos3);

			// Test the same using indices:
			int index0 = g.getCellIndex(pos0);
			int index2 = g.shift(index0, d, 1);
			int index3 = g.shift(index2, d, -1);
			int index2B = g.getCellIndex(pos2);
			int index3B = g.getCellIndex(pos3);

			// Tests
			Assert.assertEquals(index0, index3);
			Assert.assertEquals(index2, index2B);
			Assert.assertEquals(index3, index3B);
		}
	}

	@Test
	public void testShiftSpeed()
	{
		int numberOfTests = 10;

		Settings settings = getStandardSettings();
		settings.addFieldGenerator(new SU2RandomFields());
		Simulation s = new Simulation(settings);
		Grid g = s.grid;

		// Create random lattice position
		int[] pos = getRandomLatticePosition(s);
		int index = g.getCellIndex(pos);

		// Start random generators with exactly the same seed (for comparability)
		long seed = System.currentTimeMillis();
		Random generator1 = new Random(seed);
		Random generator2 = new Random(seed);
		Random generator3 = new Random(seed);

		// Test shift() using position vectors
		long time1 = -System.currentTimeMillis();
		for (int t = 0; t < numberOfTests; t++) {

			// Choose random direction and orientation
			int d = generator1.nextInt(s.getNumberOfDimensions());
			int o = generator1.nextInt(2) * 2 - 1;

			pos = g.shift(pos, d, o);
		}
		time1 += System.currentTimeMillis();

		// Test shift() using indices
		long time2 = -System.currentTimeMillis();
		for (int t = 0; t < numberOfTests; t++) {

			// Choose random direction
			int d = generator2.nextInt(s.getNumberOfDimensions());
			int o = generator2.nextInt(2) * 2 - 1;

			index = g.shift(index, d, o);
		}
		time2 += System.currentTimeMillis();

		// Test duration of random seed creation:
		long time3 = -System.currentTimeMillis();
		for (int t = 0; t < numberOfTests; t++) {

			// Choose random direction and orientation
			int d = generator3.nextInt(s.getNumberOfDimensions());
			int o = generator3.nextInt(2) * 2 - 1;
		}
		time3 += System.currentTimeMillis();

		// Test the same using indices:
		int index2 = g.getCellIndex(pos);

		// Tests
		Assert.assertEquals(index, index2);

		if (numberOfTests > 1000) {
			// Compare times
			System.out.println("Shift test: Time1: " + time1 + " vs. Time2: " + time2);
			System.out.println("Generation of random numbers: " + time3);
			System.out.println("Shift test without random number generation: Time1: " + (time1 - time3) + " vs. Time2: " + (time2 - time3));
		}
	}

	private int[] getRandomLatticePosition(Simulation s) {
		Grid g = s.grid;
		//Create random lattice position
		int[] pos = new int[s.getNumberOfDimensions()];
		for (int i = 0; i < s.getNumberOfDimensions(); i++) {
			pos[i] = (int) (Math.random() * g.getNumCells(i));
		}
		return pos;
	}

	private Settings getStandardSettings() {
		Settings s = new Settings();

		s.setRelativistic(true);
		s.setBoundary(GeneralBoundaryType.Periodic);
		s.setFieldSolver(new TemporalYangMillsSolver());
		s.useGrid(true);
		s.setInterpolator(new EmptyInterpolator());
		s.setSpeedOfLight(1.0);
		s.setNumberOfDimensions(3);

		s.setGridStep(1.0);
		s.setTimeStep(0.1);
		s.setGridCells(0, 16);
		s.setGridCells(1, 17);
		s.setGridCells(2, 18);

		s.setNumberOfColors(2);

		s.setCouplingConstant(1.0);
		s.setParticleSolver(new LeapFrogRelativistic(s.getNumberOfDimensions(), s.getSpeedOfLight()));
		s.setNumOfThreads(6);

		return s;
	}
}
