package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.YMField;

public class SU2Field extends YMField {
	
	
	public SU2Field () {
		
		v = new double[3];
			
		v[0] = 0;
		v[1] = 0;
		v[2] = 0;
			
	}
	
	public SU2Field (double a, double b, double c) {
		
		v = new double[3];
		
		v[0] = a;
		v[1] = b;
		v[2] = c;
		
	}
	
	public void reset () {
		
		v[0] = 0;
		v[1] = 0;
		v[2] = 0;
		
	}
	
	public YMField add (YMField a) {
		
		SU2Field b = new SU2Field();
		b.v[0] = v[0]+a.v[0];
		b.v[1] = v[1]+a.v[1];
		b.v[2] = v[2]+a.v[2];
		return b;
		
	}
	
public void addequate (YMField a) {
		
		v[0] += a.v[0];
		v[1] += a.v[1];
		v[2] += a.v[2];
		
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
	
	public void FieldFromForwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d) {
		
		double a0,a1,a2,a3;
		double b0,b1,b2,b3;
/*
		SU2Matrix a = (SU2Matrix) A;
		SU2Matrix b = (SU2Matrix) B;
		SU2Matrix c = (SU2Matrix) C;
		SU2Matrix d = (SU2Matrix) D;
*/
		 a0 =  a.get(0)*b.get(0) - a.get(1)*b.get(1) - a.get(2)*b.get(2) - a.get(3)*b.get(3);
		 a1 =  a.get(0)*b.get(1) + a.get(1)*b.get(0) - a.get(2)*b.get(3) + a.get(3)*b.get(2);
		 a2 =  a.get(0)*b.get(2) + a.get(2)*b.get(0) - a.get(3)*b.get(1) + a.get(1)*b.get(3);
		 a3 =  a.get(0)*b.get(3) + a.get(3)*b.get(0) - a.get(1)*b.get(2) + a.get(2)*b.get(1);
	
	
		 b0 = a0*c.get(0) + a1*c.get(1) + a2*c.get(2) + a3*c.get(3);
		 b1 = -a0*c.get(1) + a1*c.get(0) + a2*c.get(3) - a3*c.get(2);
		 b2 = -a0*c.get(2) + a2*c.get(0) + a3*c.get(1) - a1*c.get(3);
		 b3 = -a0*c.get(3) + a3*c.get(0) + a1*c.get(2) - a2*c.get(1);
		 
		 
		 v[0] = -b0*d.get(1) + b1*d.get(0) + b2*d.get(3) - b3*d.get(2);
		 v[1] = -b0*d.get(2) + b2*d.get(0) + b3*d.get(1) - b1*d.get(3);
		 v[2] = -b0*d.get(3) + b3*d.get(0) + b1*d.get(2) - b2*d.get(1);
		
	}

	public void FieldFromBackwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d) {
	
		double a0,a1,a2,a3;
		double b0,b1,b2,b3;
/*
		SU2Matrix a = (SU2Matrix) A;
		SU2Matrix b = (SU2Matrix) B;
		SU2Matrix c = (SU2Matrix) C;
		SU2Matrix d = (SU2Matrix) D;
*/		
		a0 = a.get(0)*b.get(0) + a.get(1)*b.get(1) + a.get(2)*b.get(2) + a.get(3)*b.get(3);
		a1 = -a.get(0)*b.get(1) + a.get(1)*b.get(0) + a.get(2)*b.get(3) - a.get(3)*b.get(2);
		a2 = -a.get(0)*b.get(2) + a.get(2)*b.get(0) + a.get(3)*b.get(1) - a.get(1)*b.get(3);
		a3 = -a.get(0)*b.get(3) + a.get(3)*b.get(0) + a.get(1)*b.get(2) - a.get(2)*b.get(1);
	
		b0 = a0*c.get(0) + a1*c.get(1) + a2*c.get(2) + a3*c.get(3);
		b1 = -a0*c.get(1) + a1*c.get(0) + a2*c.get(3) - a3*c.get(2);
		b2 = -a0*c.get(2) + a2*c.get(0) + a3*c.get(1) - a1*c.get(3);
		b3 = -a0*c.get(3) + a3*c.get(0) + a1*c.get(2) - a2*c.get(1);
		 
		v[0] = b0*d.get(1) + b1*d.get(0) - b2*d.get(3) + b3*d.get(2);
		v[1] = b0*d.get(2) + b2*d.get(0) - b3*d.get(1) + b1*d.get(3);
		v[2] = b0*d.get(3) + b3*d.get(0) - b1*d.get(2) + b2*d.get(1);
		
	}
	
	public void addfour (YMField a, YMField b, YMField c, YMField d) {
		
		v[0] = a.get(0)+b.get(0)+c.get(0)+d.get(0)+this.get(0);
		v[1] = a.get(1)+b.get(1)+c.get(1)+d.get(1)+this.get(1);
		v[2] = a.get(2)+b.get(2)+c.get(2)+d.get(2)+this.get(2);
			
	}
	
	public LinkMatrix getLink () {
		
		double sum = (v[0]*v[0]+v[1]*v[1]+v[2]*v[2])/4;
		SU2Matrix b = new SU2Matrix(Math.sqrt(1.0-sum), v[0]/2, v[1]/2, v[2]/2);
		return b;
	}
	
	public LinkMatrix getLinkExact () {
		
		double sum = v[0]*v[0]+v[1]*v[1]+v[2]*v[2];
		double mod = Math.sqrt(sum)/2;
		double sinfakt;
		if(mod < 1.E-20) {
			sinfakt = 0.0;
		} else {
			sinfakt = 0.5/mod*Math.sin(mod);
		}
		SU2Matrix b = new SU2Matrix(Math.cos(mod), v[0]*sinfakt, v[1]*sinfakt, v[2]*sinfakt);
		return b;
	}

	public double proj(int c)
	{
		return 0.5 * v[c];
	}

}
