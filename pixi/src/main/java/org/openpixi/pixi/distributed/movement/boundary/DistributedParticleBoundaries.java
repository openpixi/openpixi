package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.boundary.*;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps the border and boundary regions to boundaries which should be applied.
 */
public class DistributedParticleBoundaries implements ParticleBoundaries {

	private BoundaryRegions boundaryRegions;
	private ParticleBoundary[] boundaryMap =
			new ParticleBoundary[BoundaryRegions.NUM_OF_REGIONS];

	private BorderRegions borderRegions;
	private ParticleBoundary[] borderMap =
			new ParticleBoundary[BorderRegions.NUM_OF_REGIONS];

	private Map<Integer, Double> boundaryXOffsets = new HashMap<Integer, Double>();
	private Map<Integer, Double> boundaryYOffsets = new HashMap<Integer, Double>();
	private Map<Integer, Double> borderXOffsets = new HashMap<Integer, Double>();
	private Map<Integer, Double> borderYOffsets = new HashMap<Integer, Double>();

	/**
	 * Box around the particle which is used to determine
	 * whether the particle lies outside of the simulation area or not.
	 */
	private DoubleBox particleBox = new DoubleBox(0,0,0,0);

	private ParticleBoundaryType boundaryType;


	public DistributedParticleBoundaries(
			DoubleBox simulationArea,
			DoubleBox innerArea,
			ParticleBoundaryType boundaryType,
			SharedDataManager sharedDataManager) {

		this.boundaryType = boundaryType;

		boundaryRegions = new BoundaryRegions(simulationArea);
		borderRegions = new BorderRegions(simulationArea, innerArea);

		createBoundaryOffsets(simulationArea);
		createBorderOffsets(simulationArea);

		createBoundaryMap(boundaryType, sharedDataManager);
		createBorderMap(sharedDataManager);
	}


	private void createBoundaryOffsets(DoubleBox simulationArea) {
		boundaryXOffsets.put(BoundaryRegions.X_MIN, -simulationArea.xsize());
		boundaryXOffsets.put(BoundaryRegions.X_CENTER, 0.0);
		boundaryXOffsets.put(BoundaryRegions.X_MAX, simulationArea.xsize());

		boundaryXOffsets.put(BoundaryRegions.Y_MIN, -simulationArea.ysize());
		boundaryXOffsets.put(BoundaryRegions.Y_CENTER, 0.0);
		boundaryXOffsets.put(BoundaryRegions.Y_MAX, simulationArea.ysize());
	}


	private void createBorderOffsets(DoubleBox simulationArea) {
		borderXOffsets.put(BorderRegions.X_BOUNDARY_MIN, -simulationArea.xsize());
		borderXOffsets.put(BorderRegions.X_BORDER_MIN, -simulationArea.xsize());
		borderXOffsets.put(BorderRegions.X_CENTER, 0.0);
		borderXOffsets.put(BorderRegions.X_BORDER_MAX, simulationArea.xsize());
		borderXOffsets.put(BorderRegions.X_BOUNDARY_MAX, simulationArea.xsize());

		borderXOffsets.put(BorderRegions.Y_BOUNDARY_MIN, -simulationArea.ysize());
		borderXOffsets.put(BorderRegions.Y_BORDER_MIN, -simulationArea.ysize());
		borderXOffsets.put(BorderRegions.Y_CENTER, 0.0);
		borderXOffsets.put(BorderRegions.Y_BORDER_MAX, simulationArea.ysize());
		borderXOffsets.put(BorderRegions.Y_BOUNDARY_MAX, simulationArea.ysize());
	}


	private void createBoundaryMap(
			ParticleBoundaryType boundaryType, SharedDataManager sharedDataManager) {

		for (int region = 0; region < BoundaryRegions.NUM_OF_REGIONS; ++region) {
			SharedData sd = sharedDataManager.getBoundarySharedData(region);
			double xoffset = boundaryXOffsets.get(BoundaryRegions.decomposeRegionID(region).x);
			double yoffset = boundaryXOffsets.get(BoundaryRegions.decomposeRegionID(region).y);

			if (sd != null) {
				boundaryMap[region] = new BoundaryGate(xoffset, yoffset, sd);
			}
			else if (region == BoundaryRegions.X_CENTER + BoundaryRegions.Y_CENTER) {
				boundaryMap[region] = new EmptyBoundary(xoffset, yoffset);
			}
			else {
				boundaryMap[region] = boundaryType.createBoundary(xoffset, yoffset);
			}

		}
	}


	private void createBorderMap(SharedDataManager sharedDataManager) {
		for (int region = 0; region < BorderRegions.NUM_OF_REGIONS; ++region) {
			List<SharedData> sds = sharedDataManager.getBorderSharedData(region);
			double xoffset = borderXOffsets.get(BoundaryRegions.decomposeRegionID(region).x);
			double yoffset = borderXOffsets.get(BoundaryRegions.decomposeRegionID(region).y);

			if (sds.size() > 0) {
				borderMap[region] = new BorderGate(xoffset, yoffset, sds);
			}
			else {
				borderMap[region] = new EmptyBoundary(xoffset, yoffset);
			}
		}
	}


	public ParticleBoundaryType getType() {
		return boundaryType;
	}


	public void changeType(ParticleBoundaryType boundaryType) {
		throw new UnsupportedOperationException(
				"The type of the boundary in distributed simulation can not be changed!");
	}


	public void apply(Particle p) {
		boundaryType.getParticleBox(p, particleBox);

		int boundaryRegion = boundaryRegions.getRegion(particleBox);
		boundaryMap[boundaryRegion].apply(p);

		int borderRegion = borderRegions.getRegion(p.getX(), p.getY());
		borderMap[borderRegion].apply(p);
	}
}
