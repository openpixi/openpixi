package org.openpixi.pixi.ui.panel.properties;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;

/**
 * Convenience method for storing strings that specify coordinates for plots.
 * <br>
 * Examples:
 * <ul><li>x, i, 0: Draw first index as x-axis, second index as loop index,
 * and set third index to 0.</li>
 * <li>x, y, 16: Draw first and second indices as x- and y-axes, and set
 * the third index to 16.</li>
 * <li>8, x, y: Draw the second and third indices as x- and y-axes, and
 * set the first index to 8.</li></ul>
 *
 */
public class CoordinateProperties extends StringProperties {

	public enum Mode {
		MODE_1D_LOOP,
		MODE_2D
	}

	Mode mode;
	int[] positions;
	int xAxisIndex;
	int yAxisIndex;
	int loopIndex;

	public CoordinateProperties(SimulationAnimation simulationAnimation,
			Mode mode) {
		super(simulationAnimation, "Show coordinate", "");

		this.mode = mode;

		// Construct default string:
		int wstart = 0;
		String coordinates = "";
		switch (mode) {
		case MODE_1D_LOOP:
			// x is the coordinate displayed as x-axis.
			// i is the loop coordinate
			coordinates = "x, i, ";
			wstart = 2;
			break;
		case MODE_2D:
			// x is the coordinate displayed as x-axis.
			// y is the coordinate displayed as y-axis
			coordinates = "x, y, ";
			wstart = 2;
			break;
		default:
			break;
		}
		// fill remaining coordinates with the center of each coordinate
		for(int w = wstart; w < simulationAnimation.getSimulation().getNumberOfDimensions(); w++) {
			coordinates += simulationAnimation.getSimulation().grid.getNumCells(w)/2 + ", ";
		}
		coordinates = coordinates.substring(0, coordinates.length() - 2);

		setValue(coordinates);
	}

	@Override
	public void update() {
		// Also parse the string:

		Simulation s = simulationAnimation.getSimulation();
		int dimensions = s.getNumberOfDimensions();

		xAxisIndex = -1;
		yAxisIndex = -1;
		loopIndex = -1;

		// Set default values in case parsing fails
		if (mode != null) {
			switch (mode) {
			case MODE_1D_LOOP:
				xAxisIndex = 0;
				break;
			case MODE_2D:
				xAxisIndex = 0;
				yAxisIndex = 1;
				break;
			default:
				break;
			}
		}

		positions = new int[dimensions];
		for(int i = 0; i < dimensions; i++) {
			positions[i] = s.grid.getNumCells(i)/2;
		}

		// No loop index set
		String[] indices = getValue().split(",");
		int imax = Math.min(indices.length, dimensions);
		for (int i = 0; i < imax; i++) {
			if (indices[i].trim().equals("x")) {
				xAxisIndex = i;
			} else if (indices[i].trim().equals("y")) {
					yAxisIndex = i;
			} else if (indices[i].trim().equals("i")) {
				loopIndex = i;
			} else {
				try{
					positions[i] = Integer.parseInt(indices[i].trim());
				} catch (NumberFormatException e) {
					// No error message - use default instead.
				}
			}
		}
	}

	public int getXAxisIndex() {
		return xAxisIndex;
	}

	public int getYAxisIndex() {
		return yAxisIndex;
	}

	public int getLoopIndex() {
		return loopIndex;
	}

	public int[] getPositions() {
		return positions;
	}
}
