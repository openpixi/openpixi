package org.openpixi.pixi.physics.grid;

public abstract class LinkMatrix {

	public abstract LinkMatrix add(LinkMatrix a);

	public abstract LinkMatrix sub(LinkMatrix a);

	public abstract void set(int j, double value);

	public abstract double get(int j);

	public abstract LinkMatrix adj();

	public abstract LinkMatrix mult(double number);

	public abstract LinkMatrix mult(LinkMatrix a);

	public abstract YMField getAlgebraElement();

	public abstract void set(LinkMatrix a);

}
