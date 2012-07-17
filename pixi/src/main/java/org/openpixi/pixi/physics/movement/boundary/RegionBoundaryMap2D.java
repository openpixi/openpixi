package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.BoundingBox;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class RegionBoundaryMap2D {
	private static final int NUM_OF_2D_REGIONS = 9;

	private BoundaryRegionDecomposition boundaryRegions;
	private ParticleBoundary[] regionBoundaryMap = new ParticleBoundary[NUM_OF_2D_REGIONS];
	private ParticleBoundaryType boundaryType;

	private BoundingBox particleBoundingBox = new BoundingBox(0,0,0,0);

	public RegionBoundaryMap2D(BoundingBox sa, ParticleBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
		boundaryRegions = new BoundaryRegionDecomposition(sa);

		regionBoundaryMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(-sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(0, -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(sa.xsize(), -sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YCENTER] =
				boundaryType.createBoundary(-sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YCENTER] =
				new EmptyBoundary(0, 0);
		regionBoundaryMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YCENTER] =
				boundaryType.createBoundary(sa.xsize(), 0);
		regionBoundaryMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(-sa.xsize(), sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(0, sa.ysize());
		regionBoundaryMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(sa.xsize(), sa.ysize());
	}


	public void apply(Particle p) {

		/*
		 * Since there can be a large number of particles,
		 * it is costly to create new bounding box for each particle in each time step;
		 * thus, we reuse single bounding box.
		 */
		boundaryType.getParticleBoundingBox(p, particleBoundingBox);

		int region = boundaryRegions.getRegion(particleBoundingBox);
		regionBoundaryMap[region].apply(p);
	}
}
