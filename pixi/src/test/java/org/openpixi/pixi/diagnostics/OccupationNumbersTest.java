package org.openpixi.pixi.diagnostics;

import org.junit.Test;
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
		OccupationNumbersInTime occupationNumbers = new OccupationNumbersInTime(1.0);

		int size = (int) Math.pow(2, 10);

		Settings settings = new Settings();
		settings.setCouplingConstant(1.0);
		settings.setGridStep(1.0);
		settings.setNumberOfColors(2);
		settings.setNumberOfDimensions(3);
		settings.setGridCells(new int[]{size, 1, 1});
		settings.setRelativistic(true);
		settings.setNumOfThreads(6);
		settings.setSpeedOfLight(1.0);
		settings.setTimeStep(0.1);
		settings.setPoissonSolver(new EmptyPoissonSolver());
		settings.setGridSolver(new GeneralYangMillsSolver());
		settings.addDiagnostics(occupationNumbers);

		double n = 8.0;
		double[] k 	= new double[]{n * 2.0 * Math.PI / ((double) size), 0.0, 0.0};
		double[] ac	= new double[]{0.0, 1.0, 0.0};
		double[] as = new double[]{0.0, 1.0, 0.0};
		SU2PlaneWave waveGenerator = new SU2PlaneWave(k, as, ac, 10.0);
		//settings.addFieldGenerator(waveGenerator);

		double[] dir = new double[]{1.0, 0.0, 0.0};
		double[] pos = new double[]{size / 2.0, 0, 0};
		SU2PlanePulse pulseGenerator = new SU2PlanePulse(dir, pos, as, ac, 0.2, 4.0);
		settings.addFieldGenerator(pulseGenerator);

		Simulation s = new Simulation(settings);

		int steps = 100;
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
