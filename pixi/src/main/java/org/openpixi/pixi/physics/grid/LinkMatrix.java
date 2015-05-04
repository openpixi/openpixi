package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.YMField;

public abstract class LinkMatrix {
	
	public abstract LinkMatrix add (LinkMatrix a);
	
	public abstract LinkMatrix sub (LinkMatrix a);
	
	public abstract void set (int j, double value);
	
	public abstract double get (int j);
	
	public abstract void adj ();
	
	public abstract LinkMatrix mult (double number);
	
	public abstract LinkMatrix mult (LinkMatrix a);
	
	public abstract YMField getField ();
	
	public abstract void set (LinkMatrix a);

}
