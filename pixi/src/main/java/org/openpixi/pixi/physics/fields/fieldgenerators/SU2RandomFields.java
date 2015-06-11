package org.openpixi.pixi.physics.fields.fieldgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;

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

		// Calculate number of cells in the grid.

		int numberOfCells = 1;
		for (int i = 0; i < this.numberOfDimensions; i++) {
			numberOfCells *= g.getNumCells(i);
		}

		double magnitude = 10.0;

		// Cycle through each cell and apply the plane wave configuration to the links and electric fields.
		for (int c = 0; c < numberOfCells; c++) {
			int[] cellPosition = g.getCellPos(c);
			Cell currentCell = g.getCell(cellPosition);

			for (int i = 0; i < numberOfDimensions; i++) {
				SU2Field efield = new SU2Field();
				SU2Field ufield = new SU2Field();
				for (int j = 0; j < numberOfComponents; j++) {
					efield.set(i, (Math.random() - 0.5) * magnitude);
					ufield.set(i, (Math.random() - 0.5) * magnitude);
				}

				currentCell.setU(i, ufield.getLinkExact());
				currentCell.setE(i, efield);
			}
		}

	}
}
