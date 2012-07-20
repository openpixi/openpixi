package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class ParticleBoundaries {

	private BoundaryRegions boundaryRegions;
	private ParticleBoundary[] regionBoundaryMap =
			new ParticleBoundary[BoundaryRegions.NUM_OF_REGIONS];
	private ParticleBoundaryType boundaryType;

	/**
	 * Box around the particle which is used to determine
	 * whether the particle lies outside of the simulation area or not.
	 */
	private DoubleBox particleBox = new DoubleBox(0,0,0,0);

	/*
	 * In 3D case it might be easier to use for cycles for the initialization.
	 */	
	public ParticleBoundaries(DoubleBox sa, ParticleBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
		boundaryRegions = new BoundaryRegions(sa);

		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(-sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(0, -sa.ysize());
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_MIN] =
				boundaryType.createBoundary(sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER] =
				boundaryType.createBoundary(-sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_CENTER] =
				new EmptyBoundary(0, 0);
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER] =
				boundaryType.createBoundary(sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(-sa.xsize(), sa.ysize());
		regionBoundaryMap[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(0, sa.ysize());
		regionBoundaryMap[BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX] =
				boundaryType.createBoundary(sa.xsize(), sa.ysize());
	}


	public void apply(Particle p) {

		/*
		 * Since there can be a large number of particles,
		 * it is costly to create new bounding box for each particle in each time step;
		 * thus, we reuse single bounding box.
		 */
		boundaryType.getParticleBox(p, particleBox);

		int region = boundaryRegions.getRegion(particleBox);
		regionBoundaryMap[region].apply(p);
	}
}
