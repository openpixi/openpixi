package org.openpixi.pixi.physics.grid;

import java.io.Serializable;

/**
 * Represents one cell of the grid.
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

	@Override
	public String toString() {
		return String.format("E[%.3f,%.3f] Bz[%.3f] J[%.3f,%.3f]", Ex, Ey, Bz, jx, jy);
	}
}
