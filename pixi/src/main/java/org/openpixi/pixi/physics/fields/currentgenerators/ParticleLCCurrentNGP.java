package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * This current generator uses particles on fixed trajectories to correctly interpolate the charge and current density
 * on the grid according to CGC initial conditions.
 */
public class ParticleLCCurrentNGP extends ParticleLCCurrent {

	public ParticleLCCurrentNGP(int direction, int orientation, double location, double longitudinalWidth){
		super(direction, orientation, location, longitudinalWidth);
	}

	@Override
	protected AlgebraElement interpolateChargeFromGrid(Simulation s, double[] particlePosition) {
		int[] ngp = GridFunctions.nearestGridPoint(particlePosition, as);
		return poissonSolver.getGaussConstraint(s.grid.getCellIndex(ngp));
	}




}
