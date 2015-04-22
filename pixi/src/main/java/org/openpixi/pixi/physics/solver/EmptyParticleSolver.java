package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Created by dmueller on 4/22/15.
 */
public class EmptyParticleSolver implements Solver
{
    public EmptyParticleSolver(Simulation s)
    {

    }

    public void step(IParticle p, Force f, double step)
    {

    }

    public void prepare(IParticle p, Force f, double step)
    {

    }

    public void complete(IParticle p, Force f, double step)
    {

    }
}
