package org.openpixi.pixi.physics.util;

import java.io.Serializable;

public class DoubleBox implements Serializable
{

	private int dim;
	private double[] min;
	private double[] max;

	public DoubleBox(int dim, double[] min, double[] max)
	{
		this.dim = dim;
		this.min = min;
		this.max = max;
	}

	public DoubleBox(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax)
	{
		this(3, new double[] {xmin, ymin, zmin}, new double[] {xmax, ymax, zmax});
	}

	public int getDim()
	{
		return this.dim;
	}

	public double getMin(int i)
	{
		return this.min[i];
	}

	public double getMax(int i)
	{
		return this.max[i];
	}

	public double getSize(int i)
	{
		return getMax(i) - getMin(i);
	}

    /*
        Legacy getters
     */

	public double xmin()               {  return getMin(0);        }
	public double xmax()               {  return getMax(0);        }
	public double xsize()              {  return getSize(0);       }

	public double ymin()               {  return getMin(1);        }
	public double ymax()               {  return getMax(1);        }
	public double ysize()              {  return getSize(1);       }

	public double zmin()               {  return getMin(2);        }
	public double zmax()               {  return getMax(2);        }
	public double zsize()              {  return getSize(2);       }

	public boolean contains(double[] p)
	{
		for(int i = 0; i < this.dim; i++)
		{
			if(p[i] < this.min[i] || p[i] > this.max[i])
				return false;
		}
		return true;
	}

	public boolean contains(double x, double y, double z) {
		return this.contains(new double[] {x,y,z});
	}

	@Override
	public String toString()
	{
		String output = "[";
		for(int i = 0; i < this.dim; i++)
		{
			output += this.min[i] + ", " + this.max[i];
			if(i < this.dim - 1)
				output += ",";
		}
		output += "]";
		return output;
	}
}
