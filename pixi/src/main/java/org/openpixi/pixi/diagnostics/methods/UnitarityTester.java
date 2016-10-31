package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.IOException;
import java.util.ArrayList;

public class UnitarityTester implements Diagnostics {

	private Simulation s;
	private double timeInterval;
	private int stepInterval;
	private double tolerance;
	private int numberOfColors;

	public UnitarityTester(double timeInterval) {
		this.timeInterval = timeInterval;
		tolerance = Math.pow(10, -8);
	}

	public void initialize(Simulation s) {
		this.s = s;
		this.stepInterval = (int) Math.max((timeInterval / s.getTimeStep()), 1);
		this.numberOfColors = s.getNumberOfColors();
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {
			/*
				Tests if the trace of the each gauge link is within the range of unitary matrices, i.e.
					-numberOfColors < Tr U < numberOfColors.
				Warning: if this test is positive the link could still be non-unitary.
			 */
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					if (!checkTraceWithinRange(grid.getU(i, j)) || !checkTraceWithinRange(grid.getUnext(i, j))) {
						System.out.println("UnitarityTester: non-unitary link found @ " + i);
					}
				}
			}
		}
	}

	private boolean checkTraceWithinRange(GroupElement U) {
		return Math.abs(U.getRealTrace()) < numberOfColors + tolerance;
	}
}
