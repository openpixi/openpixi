package org.openpixi.pixi.physics.fields.fieldgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.math.SU2AlgebraElement;

public class SU2RandomFields implements IFieldGenerator {

	private int numberOfDimensions;
	private int numberOfComponents;
	private Grid g;

	public SU2RandomFields() {
	}

	public void applyFieldConfiguration(Simulation s) {
		this.g = s.grid;
		this.numberOfDimensions = s.getNumberOfDimensions();
		this.numberOfComponents = 3;

		int numberOfCells = g.getTotalNumberOfCells();

		double magnitude = 10.0;

		// Cycle through each cell and apply the plane wave configuration to the links and electric fields.
		for (int c = 0; c < numberOfCells; c++) {
			Cell currentCell = g.getCell(c);

			for (int i = 0; i < numberOfDimensions; i++) {
				SU2AlgebraElement efield = new SU2AlgebraElement();
				SU2AlgebraElement ufield = new SU2AlgebraElement();
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
