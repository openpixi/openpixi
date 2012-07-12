package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class RegionBoundaryMap2D {
	private static final int NUM_OF_2D_REGIONS = 9;

	private BoundaryRegionDecomposition boundaryRegions;
	private ParticleBoundary[] regionBoundaryMap = new ParticleBoundary[NUM_OF_2D_REGIONS];


	/*
	 * In 3D case it might be easier to use for cycles for the initialization.
	 */
	public RegionBoundaryMap2D(DoubleBox sa, ParticleBoundaryType boundaryType) {
		boundaryRegions = new BoundaryRegionDecomposition(sa);

		regionBoundaryMap[BoundaryRegionDecomposition.X_MIN + BoundaryRegionDecomposition.Y_MIN] =
				boundaryType.createBoundary(-sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.X_CENTER + BoundaryRegionDecomposition.Y_MIN] =
				boundaryType.createBoundary(0, -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.X_MAX + BoundaryRegionDecomposition.Y_MIN] =
				boundaryType.createBoundary(sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.X_MIN + BoundaryRegionDecomposition.Y_CENTER] =
				boundaryType.createBoundary(-sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegionDecomposition.X_CENTER + BoundaryRegionDecomposition.Y_CENTER] =
				new EmptyBoundary(0, 0);
		regionBoundaryMap[BoundaryRegionDecomposition.X_MAX + BoundaryRegionDecomposition.Y_CENTER] =
				boundaryType.createBoundary(sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegionDecomposition.X_MIN + BoundaryRegionDecomposition.Y_MAX] =
				boundaryType.createBoundary(-sa.xsize(), sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.X_CENTER + BoundaryRegionDecomposition.Y_MAX] =
				boundaryType.createBoundary(0, sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.X_MAX + BoundaryRegionDecomposition.Y_MAX] =
				boundaryType.createBoundary(sa.xsize(), sa.ysize());
	}


	public void apply(Particle p) {
		int region = boundaryRegions.getRegion(p.getX(), p.getY());
		regionBoundaryMap[region].apply(p);
	}
}
