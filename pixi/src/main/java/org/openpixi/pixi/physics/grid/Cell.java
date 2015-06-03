package org.openpixi.pixi.physics.grid;

import java.io.Serializable;

/**
 * Represents one cell of the grid.
 *
 * WHEN ADDING NEW FIELDS THE COPY() METHOD NEEDS TO BE UPDATED !!!
 */
public class Cell implements Serializable {
	/**Local electric current in d directions*/
	private YMField[] J;

	/**Local charge density at time */
	private YMField rho;

	/**Electric fields in d directions at time t */
	private YMField[] E;
	
	/**Purely spatial components of the field-strength tensor at time*/
	private YMField[][] F;
	
	/**Link matrices at time t - dt/2 */
	private LinkMatrix[] U;
	
	/**Link matrices at time t + dt/2 */
	private LinkMatrix[] Unext;

	
	public Cell(int dimensions, int colors) {
		if(colors == 2) {
			F = new SU2Field[dimensions][dimensions];
			U = new SU2Matrix[dimensions];
			Unext = new SU2Matrix[dimensions];
			E = new SU2Field[dimensions];
			J = new SU2Field[dimensions];
			rho = new SU2Field();
			
			for(int i = 0; i < dimensions; i++)
			{
				U[i] = new SU2Matrix();
				Unext[i] = new SU2Matrix();
				E[i] = new SU2Field();
				J[i] = new SU2Field();
				
				for(int j = 0; j < dimensions; j++) {
					F[i][j] = new SU2Field();
				}
			}
		}
		else {}
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addJ(int dir, YMField current) {
		J[dir].addequate(current);
	}

	public YMField getJ(int dir) {
		return J[dir];
	}

	public YMField getRho() {
		return rho;
	}

	public void setRho(YMField rho) {
		this.rho = rho;
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addRho(YMField rho) {
		this.rho.addequate(rho);
	}

	public YMField getE(int dir) {
		return E[dir];
	}

	public void setE(int dir, YMField field) {
		E[dir].set(field);
	}
	
	public void addE(int dir, YMField field) {
		E[dir].addequate(field);
	}
	
	public LinkMatrix getU(int dir) {
		return U[dir];
	}

	public void setU(int dir, LinkMatrix link) {
		U[dir].set(link);
	}
	
	public LinkMatrix getUnext(int dir) {
		return Unext[dir];
	}

	public void setUnext(int dir, LinkMatrix link) {
		Unext[dir].set(link);
	}

	public YMField getFieldStrength(int i, int j) {
		return F[i][j];
	}

	public void setFieldStrength(int i, int j, YMField field) {
		F[i][j].set(field);
	}
	

	public void resetCurrent() {
		for (int i=0;i<J.length;i++) {
			J[i].reset();
		}
	}

	public void resetCharge() {
		rho.reset();
	}
	
	public YMField getEmptyField(int colors) {
		if(colors == 2) {
			return new SU2Field();
		} else {System.out.println("Error!! Number of colors should be equal to 2!!"); return null;}
	}

	public void reassignLinks() {
		LinkMatrix[] temp = U;
	    U = Unext;
	    Unext = temp;
	}

	/**
	 * Copies the values from other cell.
	 * A safer way would be to copy the fields through reflection
	 * with use of ClassCopier (see package util).
	 * However, since this code is used in distributed version in every step, it needs to be fast;
	 * thus, a manual solution is more preferable than reflection.
	 */
	public void copyFrom(Cell other) {
		
		for (int i=0;i<other.F.length;i++) {
			System.arraycopy(other.F[i],0,this.F[i],0,other.F[i].length);
		}

		System.arraycopy(other.J, 0, this.J, 0, other.J.length);
		System.arraycopy(other.U, 0, this.U, 0, other.U.length);
		System.arraycopy(other.Unext, 0, this.Unext, 0, other.Unext.length);
		System.arraycopy(other.E, 0, this.E, 0, other.E.length);
		this.rho.set(other.rho);
		
	}
/*
	@Override
	public String toString() {
		return String.format("E[%.3f,%.3f] B[%.3f,%.3f] J[%.3f,%.3f]", Ex, Ey, Ez, Bx, By, Bz, jx, jy, jz);
	}*/
}
