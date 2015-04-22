package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.particles.IParticle;

public class RelativisticVelocity {
	
	private double c;
	
	public RelativisticVelocity(double c) {
		this.c = c;
	}
	
	/**gamma = sqrt[1 + (u / c)^2]. This expression is obtained by solving gamma = 1/sqrt(1-(v/c)^2) with v = u/gamma for gamma.*/
	public double calculateGamma(IParticle p) {
        return calculateGamma(p.getVelocity());
	}

    public double calculateGamma(double[] u)
    {
        double magnitude = 0.0;
        for(int i = 0; i < u.length; i++)
        {
            magnitude += u[i] * u[i];
        }
        return Math.sqrt(1 + magnitude/c*c);
    }

}
