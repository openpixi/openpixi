package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;


public class SimpleGridForceRelativistic implements Force {


    private int numberOfDimensions;
    private int numberOfColors;
    private int numberOfComponents;

    private double couplingConstant;
    private double speedOfLight;

    private RelativisticVelocity relVelocity;

    public SimpleGridForceRelativistic(Simulation s)
    {
        this(s.getNumberOfDimensions(), s.getNumberOfColors(), s.getCouplingConstant(), s.getSpeedOfLight());
    }

    public SimpleGridForceRelativistic(int numberOfDimensions, int numberOfColors, double couplingConstant, double speedOfLight)
    {
        this.numberOfDimensions = numberOfDimensions;
        this.numberOfColors = numberOfColors;
        this.couplingConstant = couplingConstant;

        if(this.numberOfColors > 1)
        {
            this.numberOfComponents = this.numberOfColors * this.numberOfColors - 1;
        }
        else
        {
            this.numberOfComponents = 1;
        }

        this.speedOfLight = speedOfLight;
        this.relVelocity = new RelativisticVelocity(this.speedOfLight);
    }

    public SimpleGridForceRelativistic()
    {
        this(3, 1, 1.0, 1.0);
    }

    public double getForce(int i, IParticle p)
    {
        double gamma = relVelocity.calculateGamma(p);
        double f = 0.0;
        for(int c = 0; c < this.numberOfComponents; c++)
        {
            f += p.getCharge(c) * p.getE(i, c);
            for(int j = 0; j < this.numberOfDimensions; j++)
            {
                f += p.getCharge(c) * p.getVelocity(j) * p.getF(i,j,c) / gamma;
            }
        }
        f *= this.couplingConstant;
        return f;
    }

    public double getForceX(IParticle p) {
        return  getForce(0, p);
    }

    public double getForceY(IParticle p) {
        return  getForce(1, p);
    }

    public double getForceZ(IParticle p) {
        return  getForce(2, p);
    }

}
