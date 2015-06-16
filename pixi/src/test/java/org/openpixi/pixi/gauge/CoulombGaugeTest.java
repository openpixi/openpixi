package org.openpixi.pixi.gauge;

import junit.framework.Assert;

import org.junit.Test;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2PlaneWave;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2RandomFields;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.gauge.GaugeTransformation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;

public class CoulombGaugeTest {

	@Test
	public void testAbelianCoulombGauge() {
		// Initialize simulation

		Settings settings = new Settings();
		settings.setNumberOfColors(2);
		settings.setGridCells(new int[] {2, 2, 2});

		double[] k = new double[] {0, 0, 0};
		double[] amplitudeSpatialDirection = new double[] {1, 0, 0};
		double[] amplitudeColorDirection = new double[] {1, 0, 0};
		double amplitudeMagnitude = 1;
		SU2PlaneWave constantfield = new SU2PlaneWave(k, amplitudeSpatialDirection, amplitudeColorDirection, amplitudeMagnitude);

		settings.addFieldGenerator(constantfield);

		Simulation s = new Simulation(settings);
		Grid grid = s.grid;

		GaugeTransformation transformation = new GaugeTransformation(grid);
		transformation.copyGrid(grid);

//		printU("Start: ", transformation.gaugedGrid);

		CoulombGauge coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		Double[] convergenceList = coulomb.getLastConvergence();
//		printConvergence(convergenceList);

		Assert.assertEquals(convergenceList.length, 1);
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

//		printU("Coulomb gauge: ", coulomb.getGaugedGrid());
//		printg("Coulomb g:", coulomb.getGaugeTransformation());

		// Apply gauge transformation
		LinkMatrix g1 = (new SU2Field(.1, 0, 0)).getLinkExact();
		transformation.g[0] = transformation.g[0].mult(g1);

		transformation.applyGaugeTransformation();

		// Apply Coulomb gauge transformation:
		coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		convergenceList = coulomb.getLastConvergence();
		Assert.assertEquals(convergenceList.length, 2);
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

//		printConvergence(convergenceList);
/*
		printU("Test transformation: ", transformation.gaugedGrid);
		printg("Test g:", transformation.g);

		for (int i = 0; i < 3; i++) {
			System.out.println("Iteration: " + i);
			coulomb = new CoulombGauge(transformation.gaugedGrid);
			coulomb.fixGauge(transformation.gaugedGrid);

			printU("Coulomb gauge: ", coulomb.getGaugedGrid());
			printg("Coulomb g:", coulomb.getGaugeTransformation());

			transformation.copyGrid(coulomb.getGaugedGrid());
		}
*/
	}

	@Test
	public void testNonAbelianCoulombGauge() {
		// Initialize simulation

		Settings settings = new Settings();
		settings.setNumberOfColors(2);
		settings.setGridCells(new int[] {2, 2, 2});

		double[] k = new double[] {0, 0, 0};
		double[] amplitudeSpatialDirection = new double[] {1, 0, 0};
		double[] amplitudeColorDirection = new double[] {1, 0, 0};
		double amplitudeMagnitude = 1;
		SU2PlaneWave constantfield = new SU2PlaneWave(k, amplitudeSpatialDirection, amplitudeColorDirection, amplitudeMagnitude);

		settings.addFieldGenerator(constantfield);

		Simulation s = new Simulation(settings);
		Grid grid = s.grid;

		GaugeTransformation transformation = new GaugeTransformation(grid);
		transformation.copyGrid(grid);

//		printU("Start: ", transformation.gaugedGrid);

		CoulombGauge coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		Double[] convergenceList = coulomb.getLastConvergence();
//		printConvergence(convergenceList);

		Assert.assertEquals(convergenceList.length, 1);
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

//		printU("Coulomb gauge: ", coulomb.getGaugedGrid());
//		printg("Coulomb g:", coulomb.getGaugeTransformation());

		// Apply gauge transformation
		LinkMatrix g1 = (new SU2Field(.1, 0, 0)).getLinkExact();
		transformation.g[0] = transformation.g[0].mult(g1);

		transformation.applyGaugeTransformation();

		// Apply Coulomb gauge transformation:
		coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		Assert.assertEquals(convergenceList.length, 1);
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

		LinkMatrix g2 = (new SU2Field(0, 0, .1)).getLinkExact();
		transformation.g[1] = transformation.g[1].mult(g2);

		transformation.applyGaugeTransformation();

		// Apply Coulomb gauge transformation:
		coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		convergenceList = coulomb.getLastConvergence();
		Assert.assertTrue(convergenceList.length > 3); // Need a few iterations
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

//		printConvergence(convergenceList);
/*
		printU("Test transformation: ", transformation.gaugedGrid);
		printg("Test g:", transformation.g);

		for (int i = 0; i < 3; i++) {
			System.out.println("Iteration: " + i);
			coulomb = new CoulombGauge(transformation.gaugedGrid);
			coulomb.fixGauge(transformation.gaugedGrid);

			printU("Coulomb gauge: ", coulomb.getGaugedGrid());
			printg("Coulomb g:", coulomb.getGaugeTransformation());

			transformation.copyGrid(coulomb.getGaugedGrid());
		}
*/
	}

	/**
	 * Output for debugging
	 */
	private void printConvergence(Double[] convergenceList) {
		for (Double convergence : convergenceList) {
			System.out.println("convergence: " + convergence);
		}
	}
	/**
	 * Output for debugging
	 * @param string
	 * @param grid
	 */
	private void printU(String string, Grid grid) {
		System.out.print(string + ": ");
		for (int i = 0; i < grid.getNumberOfCells(); i++) {
			int[] coor = grid.getCellPos(i);
			System.out.print("" + grid.getCell(coor).getU(0).get(0) + "|" + grid.getCell(coor).getU(0).get(1) + ", ");
		}
		System.out.println();
	}


	/**
	 * Output for debugging
	 * @param string
	 * @param grid
	 */
	private void printg(String string, LinkMatrix[] g) {
		System.out.print(string + ": ");
		for (int i = 0; i < g.length; i++) {
			System.out.print("" + g[i].get(0) + "|" + g[i].get(1) + ", ");
		}
		System.out.println();
	}
}
