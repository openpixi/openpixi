package org.openpixi.pixi.physics.grid;

import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2RandomFields;
import org.openpixi.pixi.physics.fields.GeneralYangMillsSolver;
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


		//Test for getLink() method and shift() method
		int numberOfTests = 100;
		for (int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos = getRandomLatticePosition(s);

			// Choose random direction
			int dir = (int) (Math.random() * s.getNumberOfDimensions());

			// Shift position
			int[] shiftedPos = g.shift(pos, dir, 1);

			LinkMatrix l1 = g.getLink(pos, dir, 1);
			LinkMatrix l2 = g.getLink(shiftedPos, dir, -1);
			l2.selfadj();

			// This code is specific to SU2
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(l1.get(i), l2.get(i), accuracy);
			}
		}

		// Test for getPlaquette() method
		numberOfTests = 50;
		for (int t = 0; t < numberOfTests; t++) {
			// Create random lattice position
			int[] pos = getRandomLatticePosition(s);

			// Choose random directions
			int d1 = (int) (Math.random() * s.getNumberOfDimensions());
			int d2 = (int) (Math.random() * s.getNumberOfDimensions());

			// These two plaquettes should be the inverse of each other.
			LinkMatrix plaq1 = g.getPlaquette(pos, d1, d2, 1, 1);
			LinkMatrix plaq2 = g.getPlaquette(pos, d2, d1, 1, 1);

			LinkMatrix result = plaq1.mult(plaq2);

			// This code is specific to SU2
			Assert.assertEquals(1.0, result.get(0), accuracy);
			Assert.assertEquals(0.0, result.get(1), accuracy);
			Assert.assertEquals(0.0, result.get(2), accuracy);
			Assert.assertEquals(0.0, result.get(3), accuracy);

			// Forward and backward plaquette around the same rectangle should have the same trace.
			int[] shiftedPos = g.shift(pos, d1, 1);

			LinkMatrix plaq3 = g.getPlaquette(shiftedPos, d1, d2, -1, 1);

			Assert.assertEquals(plaq1.getTrace(), plaq3.getTrace(), accuracy);
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
		s.setGridSolver(new GeneralYangMillsSolver());
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
