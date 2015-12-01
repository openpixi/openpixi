/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.physics.movement.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;

/**This class represents the LeapFrog algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 */
public class LeapFrogRelativisticParticleSolver implements ParticleSolver {

    private Simulation s;
    private int numberOfDimensions;
    RelativisticVelocity relVelocity;

    public LeapFrogRelativisticParticleSolver(Simulation s)
    {
        this(s.getNumberOfDimensions(), s.getSpeedOfLight());
        this.s = s;
    }

    public LeapFrogRelativisticParticleSolver(int numberOfDimensions, double speedOfLight)
    {
        this.numberOfDimensions = numberOfDimensions;
        this.relVelocity = new RelativisticVelocity(speedOfLight);
    }

    public LeapFrogRelativisticParticleSolver(double speedOfLight)
    {
        this(3, speedOfLight);
    }

    /**
     * LeapFrog algorithm. The damping is implemented with an linear error O(dt).
     * Warning: the velocity is stored half a time step ahead of the position.
     * @param p before the update: x(t), v(t+dt/2), a(t);
     *                 after the update: x(t+dt), v(t+3*dt/2), a(t+dt)
     */
    public void updatePosition(IParticle p, Force f, double dt)
    {
        /*
            Warning: This is really inefficient and should be changed in the future.
         */

		/*
		double gamma = relVelocity.calculateGamma(p);
        for(int i = 0 ; i < this.numberOfDimensions; i++)
        {
            // x(t+dt) = x(t) + v(t+dt/2)*dt
            p.addPosition(i, p.getVelocity(i)  * dt / gamma);

            // a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
            // WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
            p.setAcceleration(i, f.getForce(i, p) / p.getMass());

            // v(t+3*dt/2) = v(t+dt/2) + a(t+dt)*dt
            p.addVelocity(i, p.getAcceleration(i) * dt);
        }
        */
    }

	public void updateCharge(IParticle p, Force f, double dt) {

	}

    /**
     * prepare method for bringing the velocity in the desired half step
     * @param p before the update: v(t);
     *                 after the update: v(t+dt/2)
     */
    public void prepare(IParticle p, Force f, double dt)
    {
         /*
            Warning: This is really inefficient and should be changed in the future.
         */
		/*
        for(int i = 0 ; i < this.numberOfDimensions; i++)
        {
            //a(t) = F(v(t), x(t)) / m
            p.setAcceleration(i, f.getForce(i, p) / p.getMass());

            //v(t + dt / 2) = v(t) + a(t)*dt / 2
            p.addVelocity(i, p.getAcceleration(i) * dt / 2.0);
        }
        */

    }
    /**
     * complete method for bringing the velocity in the desired half step
     * @param p before the update: v(t+dt/2);
     *                 after the update: v(t)
     */
    public void complete(IParticle p, Force f, double dt)
    {
        /*
            Warning: This is really inefficient and should be changed in the future.
         */
		/*
        for(int i = 0 ; i < this.numberOfDimensions; i++)
        {
            //v(t) = v(t + dt / 2) - a(t)*dt / 2
            p.addVelocity(i,  - p.getAcceleration(i) * dt / 2.0);
        }
        */
    }

}
