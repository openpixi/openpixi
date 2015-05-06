package org.openpixi.pixi.physics.grid;

public interface LinkMatrix {

	LinkMatrix add(LinkMatrix a);

	LinkMatrix sub(LinkMatrix a);

	LinkMatrix adj();
	
	void selfadj();

	LinkMatrix mult(double number);

	LinkMatrix mult(LinkMatrix a);

	YMField getAlgebraElement();

	YMField proj();

	void set(LinkMatrix a);
	
	//Following methods could be exported to a new, "dirtier" interface in the future.
	void set (int j, double value);
	
	double get (int j);
	
	YMField getLinearizedAlgebraElement();

}
