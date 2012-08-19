package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class SimpleParticleBoundaries implements ParticleBoundaries {

	private BoundaryRegions boundaryRegions;
	private ParticleBoundary[] regionBoundaryMap =
			new ParticleBoundary[BoundaryRegions.NUM_OF_REGIONS];

	private ParticleBoundaryType boundaryType;
	private DoubleBox simulationArea;

	/**
	 * Box around the particle which is used to determine
	 * whether the particle lies outside of the simulation area or not.
	 */
	private DoubleBox particleBox = new DoubleBox(0,0,0,0);


	public ParticleBoundaryType getType() {
		return boundaryType;
	}


	public SimpleParticleBoundaries(DoubleBox simulationArea, ParticleBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
		this.simulationArea = simulationArea;
		boundaryRegions = new BoundaryRegions(simulationArea);
		createBoundaryMap();
	}


	/**
	 * For run-time boundary type switching in interactive version.
	 */
	public void changeType(ParticleBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
		createBoundaryMap();
	}


	/*
	 * In 3D case it might be easier to use for cycles for the mapping.
	 */
	private void createBoundaryMap() {
		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(-simulationArea.xsize(), -simulationArea.ysize());
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(0, -simulationArea.ysize());
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(simulationArea.xsize(), -simulationArea.ysize());
		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER] =
				boundaryType.createBoundary(-simulationArea.xsize(), 0);
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_CENTER] =
				new EmptyBoundary(0, 0);
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER] =
				boundaryType.createBoundary(simulationArea.xsize(), 0);
		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(-simulationArea.xsize(), simulationArea.ysize());
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(0, simulationArea.ysize());
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(simulationArea.xsize(), simulationArea.ysize());
	}


	public void applyOnParticleBoundingBox(
			Solver solver, Force force, Particle particle, double timeStep) {

		/*
		 * Since there can be a large number of particles,
		 * it is costly to create new bounding box for each particle in each time step;
		 * thus, we reuse single bounding box.
		 */
		boundaryType.getParticleBox(particle, particleBox);

		int region = boundaryRegions.getRegion(particleBox);
		regionBoundaryMap[region].apply(solver, force, particle, timeStep);
	}


	public void applyOnParticleCenter(
			Solver solver, Force force, Particle particle, double timeStep) {
		int region = boundaryRegions.getRegion(particle.getX(), particle.getY());
		regionBoundaryMap[region].apply(solver, force, particle, timeStep);
	}
}
