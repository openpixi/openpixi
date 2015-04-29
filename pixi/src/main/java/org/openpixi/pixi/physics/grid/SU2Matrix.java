package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.grid.LinkMatrix;

public class SU2Matrix extends LinkMatrix {

	
	public SU2Matrix () {

		e = new double[4];

		e[0] = 0;
		e[1] = 0;
		e[2] = 0;
		e[3] = 0;
		
	}
	
	public SU2Matrix (double a, double b, double c, double d) {

		e = new double[4];

		e[0] = a;
		e[1] = b;
		e[2] = c;
		e[3] = d;
		
	}

	public LinkMatrix add (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();
		b.e[0] = e[0]+a.e[0];
		b.e[1] = e[1]+a.e[1];
		b.e[2] = e[2]+a.e[2];
		b.e[3] = e[3]+a.e[3];
		return b;
		
	}
	
	public LinkMatrix sub (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();
		b.e[0] = e[0]-a.e[0];
		b.e[1] = e[1]-a.e[1];
		b.e[2] = e[2]-a.e[2];
		b.e[3] = e[3]-a.e[3];
		return b;
		
	}
	
	public void set (int j, double value) {
		
		e[j] = value;
		
	}
	
	public double get (int j) {
		
		double b = e[j];
		return b;
		
	}
	
	public void adj () {
		
		e[1]=-e[1];
		e[2]=-e[2];
		e[3]=-e[3];
		
	}
	
	public void makeFirst () {
		
		double sum = e[1]*e[1]+e[2]*e[2]+e[3]*e[3];
		if (sum>1) { System.out.println("Gauge fields too large!\n"); System.exit(1); }
		else { e[0]=Math.sqrt(1.0-sum); }
		
	}
	
	public double checkUnitarity () {
		
		return e[0]*e[0]+e[1]*e[1]+e[2]*e[2]+e[3]*e[3];
		
	}
	
	public LinkMatrix mult (double number) {
		
		SU2Matrix b = new SU2Matrix();
		b.e[0] = e[0]*number;
		b.e[1] = e[1]*number;
		b.e[2] = e[2]*number;
		b.e[3] = e[3]*number;
		return b;
		
	}
	
	public LinkMatrix mult (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();

		b.e[0] = e[0] * a.e[0] - e[1] * a.e[1] - e[2] * a.e[2] - e[3] * a.e[3];
		b.e[1] = e[1] * a.e[0] + e[0] * a.e[1] + e[3] * a.e[2] - e[2] * a.e[3];
		b.e[2] = e[2] * a.e[0] - e[3] * a.e[1] + e[0] * a.e[2] + e[1] * a.e[3];
		b.e[3] = e[3] * a.e[0] + e[2] * a.e[1] - e[1] * a.e[2] + e[0] * a.e[3];

		return b;
		
	}
	
	public YMField getField () {
		
		SU2Field b = new SU2Field(e[1]*2, e[2]*2, e[3]*2);
		return b;
		
	}
	
	public void set (LinkMatrix a) {
		
		e[0] = a.e[0];
		e[1] = a.e[1];
		e[2] = a.e[2];
		e[3] = a.e[3];
		
	}

}
