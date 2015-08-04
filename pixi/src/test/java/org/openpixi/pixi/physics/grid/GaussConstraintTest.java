package org.openpixi.pixi.physics.grid;

import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;
import org.openpixi.pixi.math.SU2AlgebraElement;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.GeneralYangMillsSolver;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2PlaneWave;
import org.openpixi.pixi.physics.gauge.RandomGauge;

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
		int initialGridSize = 32;
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
	/**
	 * This method tests an explicit violation of the Gauss constraint.
	 */
	public void testGaussConstraintViolation() {
		int gridSize = 16;
		double timeStep = 0.0025;
		double gridStep = 0.05;

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
		settings.setTMax(0.05);
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
		*/
		int numCells = simulation.grid.getTotalNumberOfCells();
		int numberOfComponents = simulation.getNumberOfColors() * simulation.getNumberOfColors() - 1;

		double[] spatialAmplitude = new double[]{0.345345, 0.0, 0.0};
		double[] colorAmplitude = new double[]{0.2345245, 0.02343, -1.234};
		double[] n = new double[]{1.567, 0.534, 0.0};

		SU2AlgebraElement[] amplitude = new SU2AlgebraElement[simulation.getNumberOfDimensions()];
		for(int j = 0; j < simulation.getNumberOfDimensions(); j++) {
			amplitude[j] =  new SU2AlgebraElement();
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

		// Do a manual updateLinks() call because simulation is initialized before the electric field is set.
		simulation.grid.updateLinks(simulation.getTimeStep());

		// Apply random gauge transformation.
		RandomGauge randomGauge = new RandomGauge(simulation.grid);
		randomGauge.setRandomVector(new double[]{1,1,1});
		randomGauge.applyGaugeTransformation(simulation.grid);


		try {
			while(simulation.continues())
			{
				// Choose random position near the center of the grid.
				int randomGridIndex = getRandomCellIndex(simulation);
				randomGauge.applyGaugeTransformation(simulation.grid);
				checkGaussViolation(simulation, spatialAmplitude, colorAmplitude, n, randomGridIndex);
				simulation.step();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Checks the local Gauss violation for the field E_{x,i}^a = e_i^a n_j x_j with Assert.
	 *
	 * @param s						Simulation instance
	 * @param spatialAmplitude		Spatial amplitude of the configuration
	 * @param colorAmplitude		Color amplitude	of the configuration
	 * @param n						n vector
	 * @param cellIndex				Cell index of the position where the divergence is calculated.
	 */
	private void checkGaussViolation(Simulation s, double[] spatialAmplitude, double[] colorAmplitude, double[] n, int cellIndex) {
		/* 	The initial field is given by
				E_{x,i}^a = e_i^a n_j x_j,
			where e_i^a is the amplitude and n_j is some normalized vector. The gauge fields are set to zero.
			The amplitude is split into a spatial and a color amplitude: e_i^a = A_i B^a.
			The squared divergence of this field is
				(\partial_i E_{x,i}^a)^2 = (A_i n_i)^2 (B^a)^2.
			This result is gauge invariant.
		*/
		double expectedResult = getVectorNormSquared(colorAmplitude) * Math.pow(getScalarProduct(spatialAmplitude, n), 2.0);
		double actualResult = s.grid.getGaussConstraintSquared(cellIndex);
		double delta = Math.pow(10.0, -4.0);
		Assert.assertEquals(expectedResult, actualResult, delta);
	}

	/**
	 * Returns a random cell index near the center of the grid.
	 * @param s	Simulation instance
	 * @return	Index of a random cell near the center of the box.
	 */
	private int getRandomCellIndex(Simulation s) {
		int[] randomGridPos = new int[s.getNumberOfDimensions()];
		for(int i = 0; i < s.getNumberOfDimensions(); i++) {
			randomGridPos[i] = s.grid.getNumCells(i) / 4 +
					(int ) (s.grid.getNumCells(i) / 2 * Math.random());
		}

		int cellIndex = s.grid.getCellIndex(randomGridPos);
		return cellIndex;
	}

	/**
	 * Computes the square of a double vector.
	 * @param v	double vector
	 * @return	square of the vector
	 */
	private double getVectorNormSquared(double[] v) {
		return getScalarProduct(v, v);
	}

	/**
	 * Computes the scalar product of two double vectors
	 * @param v1	first vector
	 * @param v2	second vector
	 * @return		scalar product of first and second vector
	 */
	private double getScalarProduct(double[] v1, double[] v2) {
		double scalarProduct = 0.0;
		for (int i = 0; i < v1.length; i++) {
			scalarProduct += v1[i] * v2[i];
		}
		return scalarProduct;
	}
}
