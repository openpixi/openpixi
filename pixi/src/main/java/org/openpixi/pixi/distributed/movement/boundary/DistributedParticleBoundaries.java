package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.boundary.*;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the border and boundary regions to boundaries which should be applied.
 */
public class DistributedParticleBoundaries implements ParticleBoundaries {

	private BoundaryRegions boundaryRegions;
	private ParticleBoundary[] boundaryMap =
			new ParticleBoundary[BoundaryRegions.NUM_OF_REGIONS];

	private BorderRegions borderRegions;
	private List<List<BorderGate>> borderMap =
			new ArrayList<List<BorderGate>>(BorderRegions.NUM_OF_REGIONS);

	/**
	 * Box around the particle which is used to determine
	 * whether the particle lies outside of the simulation area or not.
	 */
	private DoubleBox particleBox = new DoubleBox(0,0,0,0);

	private ParticleBoundaryType boundaryType;
	private DoubleBox simulationArea;


	public ParticleBoundaryType getType() {
		return boundaryType;
	}


	public DistributedParticleBoundaries(
			DoubleBox simulationArea,
			DoubleBox innerArea,
			ParticleBoundaryType boundaryType,
			SharedDataManager sharedDataManager) {

		this.boundaryType = boundaryType;
		this.simulationArea = simulationArea;

		boundaryRegions = new BoundaryRegions(simulationArea);
		borderRegions = new BorderRegions(simulationArea, innerArea);

		createBoundaryMap(boundaryType, sharedDataManager);
		createBorderMap(sharedDataManager);
	}


	private void createBoundaryMap(
			ParticleBoundaryType boundaryType, SharedDataManager sharedDataManager) {

		for (int region = 0; region < BoundaryRegions.NUM_OF_REGIONS; ++region) {
			SharedData sd = sharedDataManager.getBoundarySharedData(region);
			double xoffset = getXOffset(sharedDataManager.getBoundaryDirections(region));
			double yoffset = getYOffset(sharedDataManager.getBoundaryDirections(region));

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
			List<SharedData> sharedDatas = sharedDataManager.getBorderSharedData(region);
			List<Point> directions = sharedDataManager.getBorderDirections(region);
			assert sharedDatas.size() == directions.size();

			List<BorderGate> borderGates = new ArrayList<BorderGate>();
			for (int i = 0; i < sharedDatas.size(); ++i) {
				double xoffset = getXOffset(directions.get(i));
				double yoffset = getYOffset(directions.get(i));

				borderGates.add(new BorderGate(xoffset, yoffset, sharedDatas.get(i)));
			}
			borderMap.set(region, borderGates);
		}
	}


	private double getYOffset(Point direction) {
		return direction.y * simulationArea.ysize();
	}


	private double getXOffset(Point direction) {
		return direction.x * simulationArea.xsize();
	}


	public void changeType(ParticleBoundaryType boundaryType) {
		throw new UnsupportedOperationException(
				"The type of the boundary in distributed simulation can not be changed!");
	}


	public void apply(Particle particle) {
		boundaryType.getParticleBox(particle, particleBox);

		int boundaryRegion = boundaryRegions.getRegion(particleBox);
		boundaryMap[boundaryRegion].apply(particle);

		int borderRegion = borderRegions.getRegion(particle.getX(), particle.getY());
		for (BorderGate bg: borderMap.get(borderRegion)) {
			bg.apply(particle);
		}
	}
}
