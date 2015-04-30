package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.particles.IParticle;

public interface IParticleBoundaryConditions
{
    void applyOnParticle(IParticle particle);
}
