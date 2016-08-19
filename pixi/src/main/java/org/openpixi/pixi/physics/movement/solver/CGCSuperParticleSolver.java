package org.openpixi.pixi.physics.movement.solver;

import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Particle solver for the CGCSuperParticle class. Due to optimizations this class doesn't really do anything.
 */
public class CGCSuperParticleSolver implements ParticleSolver {

    public void updatePosition(IParticle p, Force f, double dt) {
        // Nothing to update here. Look into CGCSuperParticleInterpolationNGP.
    }


    public void updateCharge(IParticle p, Force f, double dt) {
        // Nothing to update here. Look into CGCSuperParticleInterpolationNGP.
    }

    public void prepare(IParticle p, Force f, double step) {
        // Not implemented.
    }

    public void complete(IParticle p, Force f, double step) {
        // Not implemented.
    }
}
