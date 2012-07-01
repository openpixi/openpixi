package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.BoundingBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class RegionBoundaryMap2D {
	private static final int NUM_OF_2D_REGIONS = 9;

	private BoundaryRegionDecomposition boundaryRegions;
	private ParticleBoundary[] rbMap = new ParticleBoundary[NUM_OF_2D_REGIONS];


	public RegionBoundaryMap2D(BoundingBox sa, ParticleBoundaryType boundaryType) {
		boundaryRegions = new BoundaryRegionDecomposition(sa);

		rbMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(-sa.xsize(), -sa.ysize());
		rbMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(0, -sa.ysize());
		rbMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YMIN] =
				boundaryType.createBoundary(sa.xsize(), -sa.ysize());
		rbMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YCENTER] =
				boundaryType.createBoundary(-sa.xsize(), 0);
		rbMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YCENTER] =
				new EmptyBoundary(0, 0);
		rbMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YCENTER] =
				boundaryType.createBoundary(sa.xsize(), 0);
		rbMap[BoundaryRegionDecomposition.XMIN + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(-sa.xsize(), sa.ysize());
		rbMap[BoundaryRegionDecomposition.XCENTER + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(0, sa.ysize());
		rbMap[BoundaryRegionDecomposition.XMAX + BoundaryRegionDecomposition.YMAX] =
				boundaryType.createBoundary(sa.xsize(), sa.ysize());
	}


	public void apply(Particle p) {
		int region = boundaryRegions.getRegion(p.getX(), p.getY());
		rbMap[region].apply(p);
	}
}
