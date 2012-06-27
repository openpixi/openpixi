package org.openpixi.pixi.physics.movement.boundary;

/**
 * Determines the type of particle boundary.
 */
public enum ParticleBoundaryType {

	Hardwall {
		@Override
		public ParticleBoundary createBoundary(double xoffset, double yoffset) {
			return new HardwallBoundary(xoffset, yoffset);
		}
	},

	Periodic {
		@Override
		public ParticleBoundary createBoundary(double xoffset, double yoffset) {
			return new PeriodicBoundary(xoffset, yoffset);
		}
	};

	public abstract ParticleBoundary createBoundary(double xoffset, double yoffset);
}
