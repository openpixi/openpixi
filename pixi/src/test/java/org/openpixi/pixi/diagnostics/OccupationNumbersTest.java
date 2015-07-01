package org.openpixi.pixi.diagnostics;

import org.junit.Test;
import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;
import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.EmptyPoissonSolver;
import org.openpixi.pixi.physics.fields.GeneralYangMillsSolver;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2FocusedGaussianPulse;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2PlanePulse;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2PlaneWave;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2RandomFields;

import java.io.IOException;

/**
 * Created by David on 27.06.2015.
 */
public class OccupationNumbersTest
{
	@Test
	public void test()
	{
		OccupationNumbersInTime occupationNumbers = new OccupationNumbersInTime(2.0, "csv", "occ3.csv");

		int size = (int) Math.pow(2, 7);

		Settings settings = new Settings();
		settings.setCouplingConstant(1.0);
		settings.setGridStep(1.0);
		settings.setNumberOfColors(2);
		settings.setNumberOfDimensions(3);
		settings.setGridCells(new int[]{size, size, 1});
		settings.setRelativistic(true);
		settings.setNumOfThreads(12);
		settings.setSpeedOfLight(1.0);
		settings.setTimeStep(0.05);
		settings.setPoissonSolver(new EmptyPoissonSolver());
		settings.setGridSolver(new GeneralYangMillsSolver());
		settings.addDiagnostics(occupationNumbers);

		/*
			Plane wave tests.
		 */
		double n = 8.0;
		double[] k 	= new double[]{n * 2.0 * Math.PI / ((double) size), 0.0, 0.0};
		double[] ac	= new double[]{0.0, 1.0, 0.0};
		double[] as = new double[]{0.0, 1.0, 0.0};
		SU2PlaneWave waveGenerator = new SU2PlaneWave(k, as, ac, 10.0);
		//settings.addFieldGenerator(waveGenerator);

		/*
			Single pulse tests.
		 */
		double[] dir = new double[]{1.0, 0.0, 0.0};
		double[] pos = new double[]{size / 2.0, 0, 0};
		SU2PlanePulse pulseGenerator = new SU2PlanePulse(dir, pos, as, ac, 1.0, 1.0);
		//settings.addFieldGenerator(pulseGenerator);

		/*
			Colliding plane pulse tests.
		 */
		double[] dir1 	= new double[]{1.0, 0.0, 0.0};
		double[] pos1 	= new double[]{size / 4.0, 0, 0};
		double[] dir2 	= new double[]{-1.0, 0.0, 0.0};
		double[] pos2 	= new double[]{3.0 * size / 4.0, 0, 0};
		double[] ac1	= new double[]{1.0, 1.0, 0.0};
		double[] as1 	= new double[]{0.0, 0.0, 1.0};
		double[] ac2	= new double[]{0.0, 1.0, 0.0};
		double[] as2 	= new double[]{0.0, 0.0, 1.0};
		SU2PlanePulse pulseGenerator1 = new SU2PlanePulse(dir1, pos1, as1, ac1, 1.0, 4.0);
		SU2PlanePulse pulseGenerator2 = new SU2PlanePulse(dir2, pos2, as2, ac2, 1.0, 4.0);


		//settings.addFieldGenerator(pulseGenerator1);
		//settings.addFieldGenerator(pulseGenerator2);

		/*
			Colliding focused pulse tests.
		 */

		double[] focalPoint1 = new double[]{size/2.0, size/2.0 - 4.0, 0};
		double[] focalPoint2 = new double[]{size/2.0, size/2.0 + 4.0, 0};
		double amplitude = 1.0;
		double sigma = 8.0;
		double angle = 1.0;
		double dist = size / 4;
		SU2FocusedGaussianPulse pg1 = new SU2FocusedGaussianPulse(dir1, focalPoint1, as1, ac1, amplitude, sigma, angle, dist);
		SU2FocusedGaussianPulse pg2 = new SU2FocusedGaussianPulse(dir2, focalPoint2, as2, ac2, amplitude, sigma, angle, dist);
		settings.addFieldGenerator(pg1);
		settings.addFieldGenerator(pg2);

		/*
			Bulk quantities.
		 */
		BulkQuantitiesInTime bulk = new BulkQuantitiesInTime("test2.dat", 1.0);
		//settings.addDiagnostics(bulk);

		Simulation s = new Simulation(settings);

		int steps = (int) ( 128 * occupationNumbers.timeInterval /  s.getTimeStep());
		for(int step = 0; step < steps; step++)
		{
			try {
				System.out.println(step + "/" + steps);
				s.step();
			} catch(IOException ex)
			{

			}
		}


	}
}
