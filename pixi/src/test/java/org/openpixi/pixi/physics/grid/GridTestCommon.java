package org.openpixi.pixi.physics.grid;

import junit.framework.Assert;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.solver.Boris;

/**
 * Extends the grid functionality which is required in one or more tests.
 */
public class GridTestCommon {

	public static Settings getCommonSettings() {
		Settings stt = new Settings();

		stt.setSpeedOfLight(0.7);
		stt.setSimulationWidth(10);
		stt.setSimulationHeight(10);
		stt.setParticleSolver(new Boris());
		stt.setBoundary(GeneralBoundaryType.Periodic);

		stt.setGridCellsX(10);
		stt.setGridCellsY(10);

		return stt;
	}

	public static double getJxSum(Grid grid) {
		double sum = 0;
		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				sum += grid.getJx(x, y);
			}
		}
		return sum;
	}

	public static double getJySum(Grid grid) {
		double sum = 0;
		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				sum += grid.getJy(x, y);
			}
		}
		return sum;
	}

	public static void checkSignJy(Grid grid) {
		double sign = 0;
		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				if (grid.getJy(x, y) != 0) {
					sign = Math.signum(grid.getJy(x, y));
				}
			}
		}

		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				if (grid.getJy(x, y) != 0) {
					if (sign != Math.signum(grid.getJy(x, y))) {
						Assert.assertTrue("wrong sign", false);
					}
				}
			}
		}
	}

	public static void checkSignJx(Grid grid) {
		double sign = 0;
		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				if (grid.getJx(x, y) != 0) {
					sign = Math.signum(grid.getJx(x, y));
				}
			}
		}

		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
				if (grid.getJx(x, y) != 0) {
					if (sign != Math.signum(grid.getJx(x, y))) {
						Assert.assertTrue("wrong sign", false);
					}
				}
			}
		}
	}
}
