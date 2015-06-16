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

	private boolean printDebugOutput = false;

	@Test
	public void testTrivialCoulombConfiguration() {
		if (printDebugOutput) {
			System.out.println("Trivial Coulomb Configuration");
		}
		Double[] convergenceList = testForSU2Fields(new SU2Field(0, 0, 0), new SU2Field(0, 0, 0));

		// Trivial Coulomb configurations can be recognized
		// after 1 iteration:
		Assert.assertEquals(convergenceList.length, 1);
	}

	@Test
	public void testAbelianCoulombConfiguration() {
		if (printDebugOutput) {
			System.out.println("Abelian Coulomb Configuration");
		}
		Double[] convergenceList = testForSU2Fields(new SU2Field(.1, 0, 0), new SU2Field(0, 0, 0));

		// Abelian configurations should be Coulomb gauge transformed
		// in exact 2 iterations:
		Assert.assertEquals(convergenceList.length, 2);
	}

	@Test
	public void testNonAbelianCoulombConfiguration() {
		if (printDebugOutput) {
			System.out.println("NonAbelian Coulomb Configuration");
		}
		Double[] convergenceList = testForSU2Fields(new SU2Field(.1, 0, 0), new SU2Field(0, .1, 0));

		// NonAblian configurations require more than 2 steps:
		Assert.assertTrue(convergenceList.length > 2);
	}

	private Double[] testForSU2Fields(SU2Field field1, SU2Field field2) {
		// Initialize simulation

		Settings settings = new Settings();
		settings.setNumberOfColors(2);
		settings.setGridCells(new int[] {2, 2, 2});

		double[] k = new double[] {0, 0, 0};
		double[] amplitudeSpatialDirection = new double[] {1, 0, 0};
		double[] amplitudeColorDirection = new double[] {1, 0, 0};
		double amplitudeMagnitude = 0;
		SU2PlaneWave constantfield = new SU2PlaneWave(k, amplitudeSpatialDirection, amplitudeColorDirection, amplitudeMagnitude);

		settings.addFieldGenerator(constantfield);

		Simulation s = new Simulation(settings);
		Grid grid = s.grid;

		GaugeTransformation transformation = new GaugeTransformation(grid);

		// Apply some test gauge transformation
		LinkMatrix g1 = field1.getLinkExact();
		transformation.getG()[0] = transformation.getG()[0].mult(g1);

		LinkMatrix g2 = field2.getLinkExact();
		transformation.getG()[0] = transformation.getG()[0].mult(g2);

		transformation.applyGaugeTransformation(grid);

		// Apply Coulomb gauge transformation:
		CoulombGauge coulomb = new CoulombGauge(grid);
		coulomb.applyGaugeTransformation(grid);

		Double[] convergenceList = coulomb.getLastConvergence();
		Assert.assertTrue(convergenceList[convergenceList.length -1] < coulomb.getAccuracyGoal());

		if (printDebugOutput) {
			printConvergence(convergenceList);
			printU("Coulomb gauge: ", grid);
			printg("Coulomb g:", coulomb.getG());
			System.out.println();
		}
		return convergenceList;
	}

	/**
	 * Output for debugging
	 */
	private void printConvergence(Double[] convergenceList) {
		System.out.println("Number of iterations: " + convergenceList.length);
		System.out.print("Convergance: ");
		for (Double convergence : convergenceList) {
			System.out.print("" + convergence + ", ");
		}
		System.out.println();
	}
	/**
	 * Output for debugging
	 * @param string
	 * @param grid
	 */
	private void printU(String string, Grid grid) {
		System.out.print(string + ": ");
		for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
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
