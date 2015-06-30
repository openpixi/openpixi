package org.openpixi.pixi.diagnostics;

import org.junit.Test;
import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;
import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.EmptyPoissonSolver;
import org.openpixi.pixi.physics.fields.GeneralYangMillsSolver;
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
		OccupationNumbersInTime occupationNumbers = new OccupationNumbersInTime(1.0, "csv", "occ2.csv");

		int size = (int) Math.pow(2, 10);

		Settings settings = new Settings();
		settings.setCouplingConstant(1.0);
		settings.setGridStep(1.0);
		settings.setNumberOfColors(2);
		settings.setNumberOfDimensions(3);
		settings.setGridCells(new int[]{size, 2, 1});
		settings.setRelativistic(true);
		settings.setNumOfThreads(6);
		settings.setSpeedOfLight(1.0);
		settings.setTimeStep(0.4);
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
			Colliding pulse tests.
		 */
		double[] dir1 	= new double[]{1.0, 0.0, 0.0};
		double[] pos1 	= new double[]{size / 4.0, 0, 0};
		double[] dir2 	= new double[]{-1.0, 0.0, 0.0};
		double[] pos2 	= new double[]{3.0 * size / 4.0, 0, 0};
		double[] ac1	= new double[]{1.0, 1.0, 0.0};
		double[] as1 	= new double[]{0.0, 0.0, 1.0};
		double[] ac2	= new double[]{0.0, 1.0, 0.0};
		double[] as2 	= new double[]{0.0, 0.0, 1.0};
		SU2PlanePulse pulseGenerator1 = new SU2PlanePulse(dir1, pos1, as1, ac1, 1.41421, 8.0);
		SU2PlanePulse pulseGenerator2 = new SU2PlanePulse(dir2, pos2, as2, ac2, 1.41421, 8.0);
		settings.addFieldGenerator(pulseGenerator1);
		settings.addFieldGenerator(pulseGenerator2);

		/*
			Bulk quantities.
		 */
		BulkQuantitiesInTime bulk = new BulkQuantitiesInTime("test2.dat", 1.0);
		settings.addDiagnostics(bulk);

		Simulation s = new Simulation(settings);

		for(int i = 0; i < s.grid.getTotalNumberOfCells(); i++)
		{
			int [] c = s.grid.getCellPos(i);
			System.out.println(i + " @ [" + c[0] + " " + c[1] + " " + c[2] + "]");
		}


		int steps = (int) (512.0 / s.getTimeStep());
		for(int step = 0; step < steps; step++)
		{
			try {
				s.step();
			} catch(IOException ex)
			{

			}
		}


	}
}
