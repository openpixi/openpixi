package org.openpixi.pixi.physics.grid;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.GeneralYangMillsSolver;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2PlaneWave;

import java.io.IOException;
import java.util.ArrayList;

public class GaussConstraintTest {

	@Test
	/**
	 * This method tests the Gauss constraint using a plane wave.
	 */
	public void testPlaneWaves() {
		// Test Gauss law with varying grid step.
		double[] gridSteps = new double[]{1.0, 0.5, 0.25, 0.125};
		int initialGridSize = 256;
		int[] gridSizes = new int[gridSteps.length];
		for(int i = 0; i < gridSteps.length; i++) {
			gridSizes[i] = (int) (initialGridSize / gridSteps[i]);
		}

		ArrayList<Double> gaussViolations = new ArrayList<Double>();

		for(int t = 0; t < gridSteps.length; t++) {
			// Setup the simulation settings
			Settings settings = new Settings();
			settings.setRelativistic(true);
			settings.setBoundary(GeneralBoundaryType.Periodic);
			settings.setGridSolver(new GeneralYangMillsSolver());
			settings.useGrid(true);
			settings.setInterpolator(new EmptyInterpolator());
			settings.setSpeedOfLight(1.0);
			settings.setNumberOfDimensions(3);
			settings.setNumberOfColors(2);
			settings.setTimeStep(0.05);
			settings.setCouplingConstant(1.0);
			settings.setTMax(10.0);
			settings.setNumOfThreads(12);
			settings.setGridCells(0, gridSizes[t]);
			settings.setGridCells(1, 1);
			settings.setGridCells(2, 1);
			settings.setGridStep(gridSteps[t]);

			BulkQuantitiesInTime bulk = new BulkQuantitiesInTime("", 1.0, true);
			settings.addDiagnostics(bulk);

			double simulationBoxLength = gridSizes[t] * gridSteps[t];
			double[] k = new double[]{40.0 * Math.PI / simulationBoxLength, 0.0, 0.0};
			double[] as = new double[]{0.0, 1.0, 0.0};
			double[] ac = new double[]{1.0, 0.0, 0.0};
			SU2PlaneWave planeWaveGenerator = new SU2PlaneWave(k, as, ac, 1.0);
			settings.addFieldGenerator(planeWaveGenerator);

			// Initialize the simulation and simulate a few steps.
			Simulation simulation = new Simulation(settings);
			while(simulation.continues()) {
				try {
					simulation.step();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}

			gaussViolations.add(bulk.gaussViolation);
		}

		// Check the results. Gauss violation should become smaller as the grid step is reduced.
		for(int i = 1; i < gaussViolations.size(); i++) {
			Assert.assertTrue(gaussViolations.get(i-1) > gaussViolations.get(i));
		}


		// Test Gauss law with varying time step.
		gaussViolations.clear();

		double[] timeSteps = new double[]{0.1, 0.05, 0.025, 0.0125};
		for(int t = 0; t < timeSteps.length; t++) {
			// Setup the simulation settings
			Settings settings = new Settings();
			settings.setRelativistic(true);
			settings.setBoundary(GeneralBoundaryType.Periodic);
			settings.setGridSolver(new GeneralYangMillsSolver());
			settings.useGrid(true);
			settings.setInterpolator(new EmptyInterpolator());
			settings.setSpeedOfLight(1.0);
			settings.setNumberOfDimensions(3);
			settings.setNumberOfColors(2);
			settings.setTimeStep(timeSteps[t]);
			settings.setCouplingConstant(1.0);
			settings.setTMax(10.0);
			settings.setNumOfThreads(12);
			settings.setGridCells(0, initialGridSize);
			settings.setGridCells(1, 1);
			settings.setGridCells(2, 1);
			settings.setGridStep(1.0);

			BulkQuantitiesInTime bulk = new BulkQuantitiesInTime("", 1.0, true);
			settings.addDiagnostics(bulk);

			double simulationBoxLength = gridSizes[t] * gridSteps[t];
			double[] k = new double[]{40.0 * Math.PI / simulationBoxLength, 0.0, 0.0};
			double[] as = new double[]{0.0, 1.0, 0.0};
			double[] ac = new double[]{1.0, 0.0, 0.0};
			SU2PlaneWave planeWaveGenerator = new SU2PlaneWave(k, as, ac, 1.0);
			settings.addFieldGenerator(planeWaveGenerator);

			// Initialize the simulation and simulate a few steps.
			Simulation simulation = new Simulation(settings);
			while(simulation.continues()) {
				try {
					simulation.step();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}

			gaussViolations.add(bulk.gaussViolation);
		}

		// Check the results. Gauss violation should become smaller as the time step is reduced.
		for(int i = 1; i < gaussViolations.size(); i++) {
			Assert.assertTrue(gaussViolations.get(i-1) > gaussViolations.get(i));
		}

	}

	@Test
	@Ignore
	/**
	 * This method tests an explicit violation of the Gauss constraint.
	 */
	public void testGaussConstraintViolation() {
		int gridSize = 256;
		double timeStep = 0.05;
		double gridStep = 1.0;

		Settings settings = new Settings();
		settings.setRelativistic(true);
		settings.setBoundary(GeneralBoundaryType.Periodic);
		settings.setGridSolver(new GeneralYangMillsSolver());
		settings.useGrid(true);
		settings.setInterpolator(new EmptyInterpolator());
		settings.setSpeedOfLight(1.0);
		settings.setNumberOfDimensions(3);
		settings.setNumberOfColors(2);
		settings.setTimeStep(timeStep);
		settings.setCouplingConstant(1.0);
		settings.setTMax(1.0);
		settings.setNumOfThreads(12);
		settings.setGridCells(0, gridSize);
		settings.setGridCells(1, gridSize);
		settings.setGridCells(2, 1);
		settings.setGridStep(gridStep);

		BulkQuantitiesInTime bulk = new BulkQuantitiesInTime("", timeStep, true);
		settings.addDiagnostics(bulk);
		// Initialize the simulation.
		Simulation simulation = new Simulation(settings);

		/* 	Setup the violating electric field.
		 		E_{x,i}^a = e_i^a n_j x_j,
		 	where e_i^a is the amplitude and n_j is some normalized vector. The amplitude is split into a spatial and a
		 	color amplitude: e_i^a = A_i B^a.
		 	The squared divergence of this field is
				(\partial_i E_{x,i}^a)^2 = (A_i n_i)^2 (B^a)^2.
			Averaged over the whole simulation box
		*/
		int numCells = simulation.grid.getTotalNumberOfCells();
		int numberOfComponents = simulation.getNumberOfColors() * simulation.getNumberOfColors() - 1;

		double[] spatialAmplitude = new double[]{1.0, 0.0, 0.0};
		double[] colorAmplitude = new double[]{1.0, 0.0, 0.0};
		double[] n = new double[]{1.0, 0.0, 0.0};
		SU2Field[] amplitude = new SU2Field[simulation.getNumberOfDimensions()];
		for(int j = 0; j < simulation.getNumberOfDimensions(); j++) {
			amplitude[j] =  new SU2Field();
			for(int k = 0; k < numberOfComponents; k++) {
				amplitude[j].set(k, spatialAmplitude[j] * colorAmplitude[k] * simulation.getCouplingConstant() * gridStep);
			}
		}

		for(int i = 0; i < numCells; i++) {
			Cell cell = simulation.grid.getCell(i);
			int[] gridPos = simulation.grid.getCellPos(i);
			double nx = 0.0;
			for(int j = 0; j < simulation.getNumberOfDimensions(); j++) {
				nx += gridPos[j] * gridStep * n[j];
			}
			for(int j = 0; j < simulation.getNumberOfDimensions(); j++) {
				cell.setE(j, amplitude[j].mult(nx));
			}
		}

		try {
			while(simulation.continues())
			{
				simulation.step();
			}
			double expectedResult = Math.pow(gridSize * gridStep, 2.0) / 3.0;
			Assert.assertEquals(expectedResult, bulk.gaussViolation, Math.pow(10.0, -10));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
