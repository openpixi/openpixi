

package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.Particle;


/**
 *
 * @author Clemens
 */
public class ParticleBoundingBoxPoint {

    private Particle par;
    private double x;
    private double y;
    
    public ParticleBoundingBoxPoint(Particle par, double x, double y) {
        this.par = par;
        this.x = x;
        this.y = y;
    }

    public Particle getPar() {
        return par;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    
}
