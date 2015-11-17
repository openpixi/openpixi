package org.openpixi.pixi.physics.particles;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;

import java.awt.Color;
import java.io.Serializable;

public class YangMillsParticle implements IParticle, Serializable
{
	protected int     numberOfDimensions;
	protected int     numberOfColors;
	protected int     numberOfComponents;

	public AlgebraElement Q0;
	public AlgebraElement Q1;
	public double  mass;

	protected double  pos[];
	protected double  prevPos[];
	protected double  vel[];
	protected double  acc[];

	protected double  r;
	protected Color   col;

	/*
			CONSTRUCTOR
	 */

	public YangMillsParticle(int numberOfDimensions, int numberOfColors)
	{
		this.setNumberOfDimensions(numberOfDimensions);
		ElementFactory factory = new ElementFactory(numberOfColors);
		this.Q0 = factory.algebraZero();
		this.Q1 = factory.algebraZero();

		this.numberOfColors = numberOfColors;
		this.numberOfComponents = factory.numberOfComponents;
	}

	/*
			GETTERS
	 */

	public double getPosition(int i)        {   return pos[i];                  }
	public double getPrevPosition(int i)    {   return prevPos[i];              }
	public double getVelocity(int i)        {   return vel[i];                  }

    public double[] getPosition()           {   return pos;                     }
    public double[] getPrevPosition()       {   return prevPos;                 }
    public double[] getVelocity()           {   return vel;                     }

	public double getRadius()               {   return r;                       }
	public Color getDisplayColor()                {   return col;                     }

    public int getNumberOfDimensions()      {   return this.numberOfDimensions; }

	/*
			SETTERS
	 */

	public void setPosition(int i, double value)                {   this.pos[i] = value;            }
	public void addPosition(int i, double value)                {   this.pos[i] += value;           }

	public void setPrevPosition(int i, double value)            {   this.prevPos[i] = value;        }
	public void addPrevPosition(int i, double value)            {   this.prevPos[i] += value;       }

	public void setVelocity(int i, double value)                {   this.vel[i] = value;            }
	public void addVelocity(int i, double value)                {   this.vel[i] += value;           }

	public void setNumberOfDimensions(int numberOfDimensions)
	{
		this.numberOfDimensions = numberOfDimensions;

		this.pos = new double[this.numberOfDimensions];
		this.prevPos = new double[this.numberOfDimensions];
		this.vel = new double[this.numberOfDimensions];
		this.acc = new double[this.numberOfDimensions];
	}

	public void setRadius(double r)
	{
		this.r = r;
	}

	public void setDisplayColor(Color color)
	{
		this.col = color;
	}

	public void storePosition()
	{
        prevPos = pos.clone();
	}

	public IParticle copy()
	{
		YangMillsParticle p = new YangMillsParticle(this.numberOfDimensions, this.numberOfColors);

		for (int i = 0; i < this.numberOfDimensions; i++)
		{
			p.pos[i] = this.pos[i];
			p.prevPos[i] = this.prevPos[i];
			p.vel[i] = this.vel[i];
			p.acc[i] = this.acc[i];
		}

		p.mass = this.mass;
		p.setRadius(this.r);
		p.setDisplayColor(this.col);

		return p;
	}

}
