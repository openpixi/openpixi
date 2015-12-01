package org.openpixi.pixi.physics.movement.solver;

import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;

public class EmptyParticleSolver implements ParticleSolver
{
    public EmptyParticleSolver()
    {

    }

    public void updatePosition(IParticle p, Force f, double step)
    {

    }

	public void updateCharge(IParticle p, Force f, double dt) {

	}

    public void prepare(IParticle p, Force f, double step)
    {

    }

    public void complete(IParticle p, Force f, double step)
    {

    }
}
