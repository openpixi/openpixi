package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.math.*;

import java.io.Serializable;

/**
 * Represents one cell of the grid.
 *
 * WHEN ADDING NEW FIELDS THE COPY() METHOD NEEDS TO BE UPDATED !!!
 */
public class Cell implements Serializable {
	/**Local electric current in d directions*/
	private AlgebraElement[] J;

	/**Local charge density at time */
	private AlgebraElement rho;

	/**Electric fields in d directions at time t */
	private AlgebraElement[] E;
	
	/**Purely spatial components of the field-strength tensor at time*/
	private AlgebraElement[][] F;
	
	/**Link matrices at time t - dt/2 */
	private GroupElement[] U;
	
	/**Link matrices at time t + dt/2 */
	private GroupElement[] Unext;

	/**Factory for group and algebra elements */
	ElementFactory factory;

	/**
	 * Constructor for Cell.
	 * @param dimensions Number of spatial dimensions (e.g. 3)
	 * @param colors Number of colors N for the gauge group SU(N)
	 */
	public Cell(int dimensions, int colors, ElementFactory factory) {
		this.factory = factory;

		F = new AlgebraElement[dimensions][dimensions];
		U = new GroupElement[dimensions];
		Unext = new GroupElement[dimensions];
		E = new AlgebraElement[dimensions];
		J = new AlgebraElement[dimensions];
		rho = factory.algebraZero(colors);

		for(int i = 0; i < dimensions; i++)
		{
			U[i] = factory.groupIdentity(colors);
			Unext[i] = factory.groupZero(colors);
			E[i] = factory.algebraZero(colors);
			J[i] = factory.algebraZero(colors);

			for(int j = 0; j < dimensions; j++) {
				F[i][j] = factory.algebraZero(colors);
			}
		}
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addJ(int dir, AlgebraElement current) {
		J[dir].addAssign(current);
	}

	public AlgebraElement getJ(int dir) {
		return J[dir];
	}

	public AlgebraElement getRho() {
		return rho;
	}

	public void setRho(AlgebraElement rho) {
		this.rho = rho;
	}

	/**
	 * Needs to be synchronized as we expect in the parallel version
	 * two threads trying to update the field at the same time.
	 */
	public synchronized void addRho(AlgebraElement rho) {
		this.rho.addAssign(rho);
	}

	public AlgebraElement getE(int dir) {
		return E[dir];
	}

	public void setE(int dir, AlgebraElement field) {
		E[dir].set(field);
	}
	
	public void addE(int dir, AlgebraElement field) {
		E[dir].addAssign(field);
	}
	
	public GroupElement getU(int dir) {
		return U[dir];
	}

	public void setU(int dir, GroupElement link) {
		U[dir].set(link);
	}
	
	public GroupElement getUnext(int dir) {
		return Unext[dir];
	}

	public void setUnext(int dir, GroupElement link) {
		Unext[dir].set(link);
	}

	public AlgebraElement getFieldStrength(int i, int j) {
		return F[i][j];
	}

	public void setFieldStrength(int i, int j, AlgebraElement field) {
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
	
	public AlgebraElement getEmptyField(int colors) {
		return factory.algebraZero(colors);
	}

	public void reassignLinks() {
		GroupElement[] temp = U;
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
