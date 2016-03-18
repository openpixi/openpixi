package org.openpixi.pixi;

/**
 * Created by David on 17.03.2016.
 */

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.util.yaml.YamlParser;

import java.io.IOException;

public class PerfTest {

	@Test
	public void testFullSimulation() {
		String configurationString = "simulationType: temporal cgc ngp\n" +
				"gridStep: 1\n" +
				"couplingConstant: 1\n" +
				"numberOfDimensions: 3\n" +
				"numberOfColors: 2\n" +
				"numberOfThreads: 4\n" +
				"gridCells: [512, 16, 16]\n" +
				"timeStep: 0.5\n" +
				"duration: 64\n" +
				"evaluationRegion:\n" +
				"  enabled: true\n" +
				"  point1: [2, 0, 0]\n" +
				"  point2: [-3, -1, -1]\n" +
				"activeRegion:\n" +
				"  enabled: true\n" +
				"  point1: [1, 0, 0]\n" +
				"  point2: [-2, -1, -1]\n" +
				"\n" +
				"currents:\n" +
				"  dualMVModels:\n" +
				"    - direction: 0\n" +
				"      longitudinalLocation: 8\n" +
				"      longitudinalWidth: 2.0\n" +
				"      mu: 0.1\n" +
				"      lowPassCoefficient: 1\n" +
				"      randomSeed1: 1\n" +
				"      randomSeed2: 2\n" +
				"      createInitialConditionsOutput: false\n" +
				"      outputFile: \"planarInitial.dat\"\n" +
				"\n" +
				"output:\n" +
				"  projectedEnergyDensity:\n" +
				"    - direction: 0\n" +
				"      path: \"test.dat\"\n" +
				"      interval: 1.0";

		Settings settings = new Settings();
		YamlParser yamlParser = new YamlParser(settings);
		yamlParser.parseString(configurationString);

		Simulation s = new Simulation(settings);
		try {
			long t0 = System.nanoTime();
			while(s.continues()) {
				s.step();
				System.out.println("Simulation step: " + s.totalSimulationTime);
			}

			long t1 = System.nanoTime();
			int dt = (int) ((t1 - t0) / 1000 / 1000 / 1000);
			System.out.println("MainBatch: Simulation time: " + dt + " s.");
		} catch (IOException ex) {

		}
	}
/*
	@Test
	public void multVsMultAssign() {
		int tests = 100000000;
		SU2GroupElement A = createRandomSU2Matrix();
		SU2GroupElement B = createRandomSU2Matrix();
		long t0, t1, dt;

		t0 = System.nanoTime();
		for (int i = 0; i < tests; i++) {
			A.mult(B).mult(A).mult(B).mult(A).mult(B).mult(A).mult(B);
		}
		t1 = System.nanoTime();
		dt = (int) ((t1 - t0) / 1000 / 1000);
		System.out.println("mult: " + dt + " ms.");


		t0 = System.nanoTime();
		for (int i = 0; i < tests; i++) {
			SU2GroupElement C = new SU2GroupElement(1, 0, 0, 0);
			C.multAssign(A);
			C.multAssign(B);
			C.multAssign(A);
			C.multAssign(B);
			C.multAssign(A);
			C.multAssign(B);
			C.multAssign(A);
			C.multAssign(B);
		}

		t1 = System.nanoTime();
		dt = (int) ((t1 - t0) / 1000 / 1000);
		System.out.println("multAssign: " + dt + " ms.");

		try {
			Thread.sleep(10000);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
*/

	private SU2GroupElement createRandomSU2Matrix() {
		/*
			Create random SU2 matrix.
		 */
		double[] vec = new double[4];
		double modulus = 0.0;
		for (int i = 0; i < 4; i++) {
			vec[i] = (Math.random() - 0.5);
			modulus += vec[i] * vec[i];
		}
		modulus = Math.sqrt(modulus);

		for (int i = 0; i < 4; i++) {
			vec[i] /= modulus;
		}

		SU2GroupElement m = new SU2GroupElement(vec[0], vec[1], vec[2], vec[3]);

		return m;
	}
}
