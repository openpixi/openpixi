package org.openpixi.pixi.physics.particles;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;

import java.awt.Color;
import java.io.Serializable;

/**
 * Particle class to be used in CPIC simulations.
 */
public class YangMillsParticle implements IParticle, Serializable {
	protected int numberOfDimensions;
	protected int numberOfColors;
	protected int numberOfComponents;

	public AlgebraElement Q0;
	public AlgebraElement Q1;
	public double mass;

	public double pos0[];
	public double pos1[];
	public double vel[];
	public double acc[];

	public double r;
	public Color col;

	// CONSTRUCTOR

	public YangMillsParticle(int numberOfDimensions, int numberOfColors) {
		this.setNumberOfDimensions(numberOfDimensions);
		ElementFactory factory = new ElementFactory(numberOfColors);
		this.Q0 = factory.algebraZero();
		this.Q1 = factory.algebraZero();

		this.numberOfColors = numberOfColors;
		this.numberOfComponents = factory.numberOfComponents;
	}

	// GETTERS

	public double getPosition(int i) {
		return pos0[i];
	}

	public double getPrevPosition(int i) {
		return pos1[i];
	}

	public double getVelocity(int i) {
		return vel[i];
	}

	public double[] getPosition() {
		return pos0;
	}

	public double[] getPrevPosition() {
		return pos1;
	}

	public double[] getVelocity() {
		return vel;
	}

	public double getRadius() {
		return r;
	}

	public Color getDisplayColor() {
		return col;
	}

	public int getNumberOfDimensions() {
		return this.numberOfDimensions;
	}

	// SETTERS

	public void setPosition(int i, double value) {
		this.pos0[i] = value;
	}

	public void addPosition(int i, double value) {
		this.pos0[i] += value;
	}

	public void setPrevPosition(int i, double value) {
		this.pos1[i] = value;
	}

	public void addPrevPosition(int i, double value) {
		this.pos1[i] += value;
	}

	public void setVelocity(int i, double value) {
		this.vel[i] = value;
	}

	public void addVelocity(int i, double value) {
		this.vel[i] += value;
	}

	public void setNumberOfDimensions(int numberOfDimensions) {
		this.numberOfDimensions = numberOfDimensions;

		this.pos0 = new double[this.numberOfDimensions];
		this.pos1 = new double[this.numberOfDimensions];
		this.vel = new double[this.numberOfDimensions];
		this.acc = new double[this.numberOfDimensions];
	}

	public void setRadius(double r) {
		this.r = r;
	}

	public void setDisplayColor(Color color) {
		this.col = color;
	}

	public void storeValues() {
		double[] tempPos = pos0;
		pos0 = pos1;
		pos1 = tempPos;

		AlgebraElement tempQ = Q0;
		Q0 = Q1;
		Q1 = tempQ;
	}

	public IParticle copy() {
		YangMillsParticle p = new YangMillsParticle(this.numberOfDimensions, this.numberOfColors);

		for (int i = 0; i < this.numberOfDimensions; i++) {
			p.pos0[i] = this.pos0[i];
			p.pos1[i] = this.pos1[i];
			p.vel[i] = this.vel[i];
			p.acc[i] = this.acc[i];
		}

		p.mass = this.mass;
		p.setRadius(this.r);
		p.setDisplayColor(this.col);

		return p;
	}

}
