package org.openpixi.pixi.gauge;

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
	public void testCoulombGauge() {
		// Initialize simulation

		Settings settings = new Settings();
		settings.setNumberOfColors(2);
		settings.setGridCells(new int[] {3, 3, 3});

		double[] k = new double[] {0, 0, 0};
		double[] amplitudeSpatialDirection = new double[] {1, 0, 0};
		double[] amplitudeColorDirection = new double[] {1, 0, 0};
		double amplitudeMagnitude = 1.0;
		SU2PlaneWave constantfield = new SU2PlaneWave(k, amplitudeSpatialDirection, amplitudeColorDirection, amplitudeMagnitude);

		settings.addFieldGenerator(constantfield);

		Simulation s = new Simulation(settings);
		Grid grid = s.grid;

		GaugeTransformation transformation = new GaugeTransformation(grid);
		transformation.copyGrid(grid);

		printU("Start: ", transformation.gaugedGrid);

		CoulombGauge coulomb = new CoulombGauge(transformation.gaugedGrid);
		coulomb.fixGauge(transformation.gaugedGrid);

		printU("Coulomb gauge: ", coulomb.getGaugedGrid());
		printg("Coulomb g:", coulomb.getGaugeTransformation());

		LinkMatrix g1 = (new SU2Field(.001, 0, 0)).getLinkExact();
		transformation.g[0] = transformation.g[0].mult(g1);
		transformation.applyGaugeTransformation();

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
	}

	private void printU(String string, Grid grid) {
		System.out.print(string + ": ");
		for (int i = 0; i < grid.getNumberOfCells(); i++) {
			int[] coor = grid.getCellPos(i);
			System.out.print("" + grid.getCell(coor).getU(0).get(0) + ", ");
		}
		System.out.println();
	}

	private void printg(String string, LinkMatrix[] g) {
		System.out.print(string + ": ");
		for (int i = 0; i < g.length; i++) {
			System.out.print("" + g[i].get(0) + "|" + g[i].get(1) + ", ");
		}
		System.out.println();
	}
}
