package org.openpixi.pixi.physics.particles;
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
		Particle position, velocity and acceleration getters
	 */

	double getPosition(int d);
	double getPrevPosition(int d);
	double getVelocity(int d);
	double getAcceleration(int d);

	/*
		Getters for fields at particle position
	 */

	double getE(int d, int c);
	double getF(int i, int j, int c);

	/*
		Getters for particle properties
	 */

	double getCharge(int c);
	double getMass();

	/*
		Getters for display properties
	 */

	double getRadius();
	Color getColor();

	/*
		Legacy getters
		Note: these should be removed in the future.
	 */

	double getX();
	double getY();
	double getZ();

	double getPrevX();
	double getPrevY();
	double getPrevZ();

	double getVx();
	double getVy();
	double getVz();

	double getAx();
	double getAy();
	double getAz();

	double getEx();
	double getEy();
	double getEz();

	double getBx();
	double getBy();
	double getBz();

	double getCharge();


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

	void setAcceleration(int d, double a);
	void addAcceleration(int d, double a);


	/*
		Setters for fields at particle position
	 */

	void setE(int d, int c, double E);
	void setF(int i, int j, int c, double F);

	/*
		Setters for particle properties
	 */

	void setNumberOfColors(int numberOfColors);
	void setNumberOfDimensions(int numberOfDimensions);
	void setCharge(int c, double q);
	void setMass(double m);

	/*
		Setters for display properties
	 */

	void setRadius(double r);
	void setColor(Color color);


	/*
		Legacy setters
		Note: these should be removed in the future.
	 */

	void setX(double x);

	void addX(double x);

	void setPrevX(double prevX);

	void addPrevX(double x);

	void setY(double y);

	void addY(double y);

	void setPrevY(double prevY);

	void addPrevY(double y);

	void setZ(double z);

	void addZ(double z);

	void setPrevZ(double prevZ);

	void addPrevZ(double z);

	void setVx(double vx);

	void setVy(double vy);

	void setVz(double vz);

	void setAx(double ax);

	void setAy(double ay);

	void setAz(double az);

	void setCharge(double charge);

	void setEx(double Ex);

	void setEy(double Ey);

	void setEz(double Ez);

	void setBx(double Bx);

	void setBy(double By);

	void setBz(double Bz);


	//----------------------------------------------------------------------------------------------
	// UTILITY METHODS
	//----------------------------------------------------------------------------------------------

	void storePosition();

	void applyPeriodicBoundary(double argument1, double argument2, double argument3);

	IParticle copy();

	String toString();
}
