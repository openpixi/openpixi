package org.openpixi.pixi.physics;

import java.util.List;

public interface ParticleLoader {
	
	public List<Particle> load (int numOfParticles, int numCellsX, int numCellsY,
			double simulationWidth, double simulationHeight, double radius);
	
}
