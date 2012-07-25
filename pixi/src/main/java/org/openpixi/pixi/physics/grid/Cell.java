package org.openpixi.pixi.physics.grid;

import java.io.Serializable;

/**
 * Represents one cell of the grid.
 *
 * WHEN ADDING NEW FIELDS THE COPY() METHOD NEEDS TO BE UPDATED !!!
 */
public class Cell implements Serializable {
	/**electric current in x-Direction*/
	public double jx;
	/**electric current in y-Direction*/
	public double jy;

	/**sum of electric charges in a cell*/
	public double rho;
	/**electrostatic potential*/
	public double phi;

	/**electric field in x direction at time t+dt*/
	public double Ex;
	/**electric field in y direction at time t+dt*/
	public double Ey;
	/**magnetic field in z direction at time t+dt*/
	public double Bz;

	/**electric field in x direction at time t*/
	public double Exo;
	/**electric field in y direction at time t*/
	public double Eyo;
	/**magnetic field in z direction at time t*/
	public double Bzo;

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
