package org.openpixi.pixi.physics.grid;

import junit.framework.Assert;

/**
 * Extends the grid functionality which is required in one or more tests.
 */
public class GridTestCommon {

	public static double getJxSum(Grid grid) {
		double sum = 0;
		for (int x = -1; x < grid.getNumCellsX() + 1; x++) {
			for (int y = -1; y < grid.getNumCellsY() + 1; y++) {
				sum += grid.getJx(x, y);
			}
		}
		return sum;
	}

	public static double getJySum(Grid grid) {
		double sum = 0;
		for (int x = -1; x < grid.getNumCellsX() + 1; x++) {
			for (int y = -1; y < grid.getNumCellsY() + 1; y++) {
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
