package org.openpixi.pixi.physics.gauge;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;

/**
 * Apply a random gauge transformation to a grid.
 */
public class RandomGauge extends GaugeTransformation {
	private RandomGaugeTransformationAction randomGaugeTransformationAction = new RandomGaugeTransformationAction();

	/**
	 * Random vector in color space.
	 */
	private double[] randomVector;

	public void setRandomVector(double[] randomVector) {
		this.randomVector = randomVector;
	}

	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public RandomGauge(Grid grid) {
		super(grid);
		setRandomVector(new double[] {0, 0, 0});
	}

	public void applyGaugeTransformation(Grid grid) {
		// prepare the g field in a random way:
		grid.getCellIterator().execute(grid, randomGaugeTransformationAction);

		// apply the gauge transformation:
		super.applyGaugeTransformation(grid);
	}

	private class RandomGaugeTransformationAction implements CellAction {
		public void execute(Grid grid, int index) {
			double a = 2 * (Math.random() - 0.5) * randomVector[0];
			double b = 2 * (Math.random() - 0.5) * randomVector[1];
			double c = 2 * (Math.random() - 0.5) * randomVector[2];
			SU2Field field = new SU2Field(a, b, c);
			getG()[index] = field.getLink();
		}
	}
}
