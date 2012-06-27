package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.BoundingBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the 8 possible boundary regions in 2D to actual boundaries.
 */
public class RegionBoundaryMap2D {

	private BoundaryRegionDecomposition boundaryRegions;
	private Map<Integer, ParticleBoundary> rbMap =
			new HashMap<Integer, ParticleBoundary>();


	public RegionBoundaryMap2D(BoundingBox sa, ParticleBoundaryType boundaryType) {
		boundaryRegions = new BoundaryRegionDecomposition(sa);

		rbMap.put(
				BoundaryRegionDecomposition.XMIN_YMIN,
				boundaryType.createBoundary(sa.xmin() - sa.xsize(), sa.ymin() - sa.ysize()));
		rbMap.put(
				BoundaryRegionDecomposition.XCEN_YMIN,
				boundaryType.createBoundary(0, sa.ymin() - sa.ysize()));
		rbMap.put(
				BoundaryRegionDecomposition.XMAX_YMIN,
				boundaryType.createBoundary(sa.xmax(), sa.ymin() - sa.ysize()));
		rbMap.put(
				BoundaryRegionDecomposition.XMIN_YCEN,
				boundaryType.createBoundary(sa.xmin() - sa.xsize(), 0));
		rbMap.put(
				BoundaryRegionDecomposition.XCEN_YCEN,
				new EmptyBoundary(0, 0));
		rbMap.put(
				BoundaryRegionDecomposition.XMAX_YCEN,
				boundaryType.createBoundary(sa.xmax(), 0));
		rbMap.put(
				BoundaryRegionDecomposition.XMIN_YMAX,
				boundaryType.createBoundary(sa.xmin() - sa.xsize(), sa.ymax()));
		rbMap.put(
				BoundaryRegionDecomposition.XCEN_YMAX,
				boundaryType.createBoundary(0, sa.ymax()));
		rbMap.put(
				BoundaryRegionDecomposition.XMAX_YMAX,
				boundaryType.createBoundary(sa.xmax(), sa.ymax()));
	}


	public void apply(Particle p) {
		int region = boundaryRegions.getRegion(p.getX(), p.getY());
		rbMap.get(region).apply(p);
	}
}
