package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;

import java.util.List;

/**
 *  Registers the border particles (particles which belong to this node
 *  but need to be send to neighbors as they influence their interpolation).
 */
public class BorderGate extends ParticleBoundary {

	/** The particles at the border sometimes need to be send to more than one neighbor.
	 * (Think about the particles in corner of local simulation area). */
	private List<SharedData> sharedDatas;


	public BorderGate(double xoffset, double yoffset, List<SharedData> sharedDatas) {
		super(xoffset, yoffset);
		this.sharedDatas = sharedDatas;
	}


	@Override
	public void apply(Particle p) {
		for (SharedData sd: sharedDatas) {
			sd.registerBorderParticle(p);
		}
	}
}
