package org.openpixi.pixi.physics.particles;

import java.awt.Color;
import java.io.Serializable;

public class Particle implements IParticle, Serializable
{
	protected int     numberOfDimensions;
	protected int     numberOfColors;
	protected int     numberOfComponents;
	protected double  charge[];
	protected double  mass;

	protected double  pos[];
	protected double  prevPos[];
	protected double  vel[];
	protected double  acc[];

	protected double  E[][];
	protected double  F[][][];

	protected double  r;
	protected Color   col;

	/*
			CONSTRUCTOR
	 */

	public Particle()
	{
		this(3, 1);
	}

	public Particle(int numberOfDimensions, int numberOfColors)
	{
		this.setNumberOfDimensions(numberOfDimensions);
		this.setNumberOfColors(numberOfColors);
	}

	/*
			GETTERS
	 */

	public double getPosition(int i)        {   return pos[i];                  }
	public double getPrevPosition(int i)    {   return prevPos[i];              }
	public double getVelocity(int i)        {   return vel[i];                  }
	public double getAcceleration(int i)    {   return acc[i];                  }

    public double[] getPosition()           {   return pos;                     }
    public double[] getPrevPosition()       {   return prevPos;                 }
    public double[] getVelocity()           {   return vel;                     }
    public double[] getAcceleration()       {   return acc;                     }

	public double getE(int i, int c)        {   return E[i][c];                 }
	public double getF(int i, int j, int c) {   return F[i][j][c];              }

	public double getCharge(int c)          {   return charge[c];               }
	public double getMass()                 {   return mass;                    }

	public double getRadius()               {   return r;                       }
	public Color  getColor()                {   return col;                     }

    public int getNumberOfColors()          {   return this.numberOfColors;     }
    public int getNumberOfDimensions()      {   return this.numberOfDimensions; }
    public int getNumberOfComponents()      {   return this.numberOfComponents; }

	/*
			SETTERS
	 */

	public void setPosition(int i, double value)                {   this.pos[i] = value;            }
	public void addPosition(int i, double value)                {   this.pos[i] += value;           }

	public void setPrevPosition(int i, double value)            {   this.prevPos[i] = value;        }
	public void addPrevPosition(int i, double value)            {   this.prevPos[i] += value;       }

	public void setVelocity(int i, double value)                {   this.vel[i] = value;            }
	public void addVelocity(int i, double value)                {   this.vel[i] += value;           }

	public void setAcceleration(int i, double value)            {   this.acc[i] = value;            }
	public void addAcceleration(int i, double value)            {   this.acc[i] += value;           }

	public void setE(int i, int c, double E)                    {   this.E[i][c] = E;               }
	public void setF(int i, int j, int c, double F)             {   this.F[i][j][c] = F;            }

	public void setNumberOfColors(int numberOfColors)
	{
		this.numberOfColors = numberOfColors;
		if(this.numberOfColors > 1)
		{
			this.numberOfComponents = numberOfColors * numberOfColors - 1;
		}
		else
		{
			this.numberOfComponents = 1;
		}

		this.charge = new double[this.numberOfComponents];

		if(this.numberOfDimensions > 0)
			initializeFields();
	}

	public void setNumberOfDimensions(int numberOfDimensions)
	{
		this.numberOfDimensions = numberOfDimensions;

		this.pos = new double[this.numberOfDimensions];
		this.prevPos = new double[this.numberOfDimensions];
		this.vel = new double[this.numberOfDimensions];
		this.acc = new double[this.numberOfDimensions];

		if(this.numberOfColors > 0)
			initializeFields();
	}

	public void setCharge(int c, double q)
	{
		this.charge[c] = q;
	}

	public void setMass(double m)
	{
		this.mass = m;
	}

	public void setRadius(double r)
	{
		this.r = r;
	}

	public void setColor(Color color)
	{
		this.col = color;
	}

	public void storePosition()
	{
        prevPos = pos.clone();
	}

	public IParticle copy()
	{
		Particle p = new Particle();

		p.setNumberOfDimensions(this.numberOfDimensions);
		p.setNumberOfColors(this.numberOfColors);

		for (int i = 0; i < this.numberOfDimensions; i++)
		{
			p.setPosition(i, this.pos[i]);
			p.setPrevPosition(i, this.prevPos[i]);
			p.setVelocity(i, this.vel[i]);
			p.setAcceleration(i, this.acc[i]);
		}



		p.setMass(this.mass);
		p.setRadius(this.r);
		p.setColor(this.col);

		for (int c = 0; c < this.numberOfComponents; c++)
		{
			p.setCharge(c, this.charge[c]);
			for(int i = 0; i < this.numberOfDimensions; i++)
			{
				p.setE(i, c, this.E[i][c]);
				for(int j = 0; j < this.numberOfDimensions; j++)
				{
					p.setF(i, j, c, this.F[i][j][c]);
				}
			}
		}

		return p;
	}

	public String toString()
	{
		String output = "[";
		for(int i = 0; i < this.numberOfDimensions; i++)
		{
			output += this.pos[i];
			if(i < this.numberOfDimensions - 1)
				output += ",";
		}
		output += "]";
		return output;
	}

	private void initializeFields()
	{
		this.E = new double[this.numberOfDimensions][this.numberOfComponents];
		this.F = new double[this.numberOfDimensions][this.numberOfDimensions][this.numberOfComponents];
	}
}
