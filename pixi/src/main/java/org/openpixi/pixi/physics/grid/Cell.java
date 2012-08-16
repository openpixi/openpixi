package org.openpixi.pixi.physics.grid;

import java.io.Serializable;

/**
 * Represents one cell of the grid.
 *
 * WHEN ADDING NEW FIELDS THE COPY() METHOD NEEDS TO BE UPDATED !!!
 */
public class Cell implements Serializable {
	/**electric current in x-Direction*/
	private double jx;
	/**electric current in y-Direction*/
	private double jy;

	/**sum of electric charges in a cell*/
	private double rho;
	/**electrostatic potential*/
	private double phi;

	/**electric field in x direction at time t+dt*/
	private double Ex;
	/**electric field in y direction at time t+dt*/
	private double Ey;
	/**magnetic field in z direction at time t+dt*/
	private double Bz;

	/**electric field in x direction at time t*/
	private double Exo;
	/**electric field in y direction at time t*/
	private double Eyo;
	/**magnetic field in z direction at time t*/
	private double Bzo;


	public double getJx() {
		return jx;
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addJx(double value) {
		this.jx += value;
	}

	public double getJy() {
		return jy;
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addJy(double value) {
		this.jy += value;
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addRho(double value) {
		this.rho += value;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public double getEx() {
		return Ex;
	}

	public void setEx(double ex) {
		Ex = ex;
	}

	public double getEy() {
		return Ey;
	}

	public void setEy(double ey) {
		Ey = ey;
	}

	public double getBz() {
		return Bz;
	}

	public void setBz(double bz) {
		Bz = bz;
	}

	public double getExo() {
		return Exo;
	}

	public void setExo(double exo) {
		Exo = exo;
	}

	public double getEyo() {
		return Eyo;
	}

	public void setEyo(double eyo) {
		Eyo = eyo;
	}

	public double getBzo() {
		return Bzo;
	}

	public void setBzo(double bzo) {
		Bzo = bzo;
	}


	public void resetCurrent() {
		jx = 0;
		jy = 0;
	}

	public void resetCharge() {
		rho = 0;
	}

	public void storeFields() {
		Exo = Ex;
		Eyo = Ey;
		Bzo = Bz;
	}

	/**
	 * Copies the values from other cell.
	 * A safer way would be to copy the fields through reflection
	 * with use of ClassCopier (see package util).
	 * However, since this code is used in distributed version in every step, it needs to be fast;
	 * thus, a manual solution is more preferable than reflection.
	 */
	public void copyFrom(Cell other) {
		this.jx = other.jx;
		this.jy = other.jy;
		this.rho = other.rho;
		this.phi = other.phi;
		this.Ex = other.Ex;
		this.Ey = other.Ey;
		this.Bz = other.Bz;
		this.Exo = other.Exo;
		this.Eyo = other.Eyo;
		this.Bzo = other.Bzo;
	}

	@Override
	public String toString() {
		return String.format("E[%.3f,%.3f] Bz[%.3f] J[%.3f,%.3f]", Ex, Ey, Bz, jx, jy);
	}
}
