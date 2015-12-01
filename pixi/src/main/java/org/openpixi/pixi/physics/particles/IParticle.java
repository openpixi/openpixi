package org.openpixi.pixi.physics.particles;
import org.openpixi.pixi.math.AlgebraElement;

import java.awt.Color;


public interface IParticle
{
	//----------------------------------------------------------------------------------------------
	// GETTERS
	//----------------------------------------------------------------------------------------------

	/*
			GETTERS
	 */

	/*
		Particle position, velocity getters
		Note: in a relativistic context 'velocity' refers to the relativistic velocity usually denoted as 'u'.
	 */

	double getPosition(int d);
	double getPrevPosition(int d);
	double getVelocity(int d);

    double[] getPosition();
    double[] getPrevPosition();
    double[] getVelocity();

    int     getNumberOfDimensions();

	/*
		Getters for display properties
	 */

	double getRadius();
	Color getDisplayColor();

	/*
			SETTERS
	 */

	/*
		Setters for particle position, velocity and acceleration
	 */

	void setPosition(int d, double x);
	void addPosition(int d, double x);

	void setPrevPosition(int d, double x);
	void addPrevPosition(int d, double x);

	void setVelocity(int d, double v);
	void addVelocity(int d, double v);

	void setNumberOfDimensions(int numberOfDimensions);

	/*
		Setters for display properties
	 */

	void setRadius(double r);
	void setDisplayColor(Color color);


	//----------------------------------------------------------------------------------------------
	// UTILITY METHODS
	//----------------------------------------------------------------------------------------------

	void reassignValues();

	IParticle copy();
}
