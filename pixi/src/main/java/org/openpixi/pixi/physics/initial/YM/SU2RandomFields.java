package org.openpixi.pixi.physics.initial.YM;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.initial.IInitialCondition;

public class SU2RandomFields implements IInitialCondition {

	private int numberOfDimensions;
	private int numberOfComponents;
	private Grid g;

	public SU2RandomFields() {
	}

	public void applyInitialCondition(Simulation s) {
		this.g = s.grid;
		this.numberOfDimensions = s.getNumberOfDimensions();

		ElementFactory factory = g.getElementFactory();
		int colors = g.getNumberOfColors();

		this.numberOfComponents = colors * colors - 1;

		int numberOfCells = g.getTotalNumberOfCells();

		double magnitude = 10.0;

		// Cycle through each cell and apply the plane wave configuration to the links and electric fields.
		for (int c = 0; c < numberOfCells; c++) {
			Cell currentCell = g.getCell(c);

			for (int i = 0; i < numberOfDimensions; i++) {
				AlgebraElement efield = factory.algebraZero(colors);
				AlgebraElement ufield = factory.algebraZero(colors);
				for (int j = 0; j < numberOfComponents; j++) {
					efield.set(i, (Math.random() - 0.5) * magnitude);
					ufield.set(i, (Math.random() - 0.5) * magnitude);
				}

				currentCell.setU(i, ufield.getLink());
				currentCell.setE(i, efield);
			}
		}

	}
}
