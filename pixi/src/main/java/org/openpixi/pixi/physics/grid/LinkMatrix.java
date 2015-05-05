package org.openpixi.pixi.physics.grid;

public interface LinkMatrix {

	LinkMatrix add(LinkMatrix a);

	LinkMatrix sub(LinkMatrix a);

	LinkMatrix adj();

	LinkMatrix mult(double number);

	LinkMatrix mult(LinkMatrix a);

	YMField getAlgebraElement();

	YMField proj();

	void set(LinkMatrix a);

}
