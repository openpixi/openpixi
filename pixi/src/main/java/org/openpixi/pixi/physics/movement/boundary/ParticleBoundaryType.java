package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Determines the type of particle boundary.
 */
public enum ParticleBoundaryType {

	Hardwall {
		@Override
		public ParticleBoundary createBoundary(double xoffset, double yoffset) {
			return new HardwallBoundary(xoffset, yoffset);
		}

		@Override
		public DoubleBox getParticleBox(Particle p, DoubleBox pbb) {
			pbb.set(
					p.getX() - p.getRadius(), p.getX() + p.getRadius(),
					p.getY() - p.getRadius(), p.getY() + p.getRadius());
			return pbb;
		}
	},

	Periodic {
		@Override
		public ParticleBoundary createBoundary(double xoffset, double yoffset) {
			return new PeriodicBoundary(xoffset, yoffset);
		}

		@Override
		public DoubleBox getParticleBox(Particle p, DoubleBox pbb) {
			pbb.set(p.getX(), p.getX(), p.getY(), p.getY());
			return pbb;
		}
	};

	public abstract ParticleBoundary createBoundary(double xoffset, double yoffset);

	/**
	 * Determines a bounding box around the particle.
	 * For the determination of whether the particle is outside of the simulation area
	 * we can use the particle's bounding box.
	 */
	public abstract DoubleBox getParticleBox(Particle p, DoubleBox pbb);
}
