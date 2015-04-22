package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.YMField;

public class SU2Field extends YMField {
	
	private double[] v = new double[3];
	
	public SU2Field () {
			
			v[0] = 0;
			v[1] = 0;
			v[2] = 0;
			
		}
	
	public SU2Field (double a, double b, double c) {
		
		v[0] = a;
		v[1] = b;
		v[2] = c;
		
	}
	
	public YMField add (YMField a) {
		
		SU2Field b = new SU2Field();
		b.v[0] = v[0]+a.v[0];
		b.v[1] = v[1]+a.v[1];
		b.v[2] = v[2]+a.v[2];
		return b;
		
	}
	
	public YMField sub (YMField a) {
		
		SU2Field b = new SU2Field();
		b.v[0] = v[0]-a.v[0];
		b.v[1] = v[1]-a.v[1];
		b.v[2] = v[2]-a.v[2];
		return b;
		
	}
	
	public void set (int j, double value) {
		
		v[j] = value;
		
	}
	
	public double get (int j) {
		
		double b = v[j];
		return b;
		
	}
	
	public double square () {
		
		return v[0]*v[0]+v[1]*v[1]+v[2]*v[2];
		
	}
	
	public YMField mult (double number) {
		
		SU2Field b = new SU2Field();
		b.v[0] = v[0]*number;
		b.v[1] = v[1]*number;
		b.v[2] = v[2]*number;
		return b;
		
	}
	
	public void set (YMField a) {
		
		v[0] = a.v[0];
		v[1] = a.v[1];
		v[2] = a.v[2];
		
	}
	
	public LinkMatrix getLink () {
		
		double sum = (v[0]*v[0]+v[1]*v[1]+v[2]*v[2])/4;
		if (sum>1) { System.out.println("Electric fields too large!\n"); System.exit(1); return new SU2Matrix(); }
		else { 
			SU2Matrix b = new SU2Matrix(Math.sqrt(1.0-sum), v[0]/2, v[1]/2, v[2]/2);
			return b;
		}
	}
	
	public LinkMatrix getLinkExact () {
		
		double sum = (v[0]*v[0]+v[1]*v[1]+v[2]*v[2])/4;
		double mod = Math.sqrt(sum);
		if (sum>1) { System.out.println("Electric fields too large!\n"); System.exit(1); return new SU2Matrix(); }
		else { 
			SU2Matrix b = new SU2Matrix(Math.cos(mod), v[0]/2/mod*Math.sin(mod), v[1]/2/mod*Math.sin(mod), v[2]/2/mod*Math.sin(mod));
			return b;
		}
	}

}
