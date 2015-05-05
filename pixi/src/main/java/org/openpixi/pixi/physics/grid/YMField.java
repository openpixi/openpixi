package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.LinkMatrix;

public abstract class YMField {
	
	protected double[] v;
	
	public abstract YMField add (YMField a);
	
	public abstract void addequate (YMField a);
	
	public abstract YMField sub (YMField a);
	
	public abstract void set (int j, double value);
	
	public abstract double get (int j);
	
	public abstract double square ();
	
	public abstract YMField mult (double number);
	
	public abstract void set (YMField a);
	
	public abstract void reset ();
	
	public abstract void FieldFromForwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d);
	
	public abstract void FieldFromBackwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d);
	
	public abstract void addfour (YMField a, YMField b, YMField c, YMField d);
	
	public abstract LinkMatrix getLink ();
	
	public abstract LinkMatrix getLinkExact ();

}
