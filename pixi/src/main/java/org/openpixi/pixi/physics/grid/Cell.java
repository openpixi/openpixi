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

	/** Temporal link matrix at time t - dt/2 */
	private GroupElement U0;

	/** Temporal link matrix at time t + dt/2 */
	private GroupElement U0next;

	/**Factory for group and algebra elements */
	ElementFactory factory;

	private int dimensions;
	private int colors;

	/** Evaluation region property. If true then the cell is considered in various calculations (energy density, Gauss law, ...). */
	private boolean evaluatable;

	/** Active region property. If true then the cell is considered in the equations of motion. */
	private boolean active;


	/**
	 * Constructor for Cell.
	 * @param dimensions Number of spatial dimensions (e.g. 3)
	 * @param colors Number of colors N for the gauge group SU(N)
	 */
	public Cell(int dimensions, int colors, ElementFactory factory) {
		this.dimensions = dimensions;
		this.colors = colors;
		this.factory = factory;
		this.evaluatable = true;
		this.active = true;

		F = new AlgebraElement[dimensions][dimensions];
		U = new GroupElement[dimensions];
		Unext = new GroupElement[dimensions];
		E = new AlgebraElement[dimensions];
		J = new AlgebraElement[dimensions];
		rho = factory.algebraZero(colors);

		for(int i = 0; i < dimensions; i++)
		{
			U[i] = factory.groupIdentity(colors);
			Unext[i] = factory.groupIdentity(colors);
			E[i] = factory.algebraZero(colors);
			J[i] = factory.algebraZero(colors);

			for(int j = 0; j < dimensions; j++) {
				F[i][j] = factory.algebraZero(colors);
			}
		}

		U0 = factory.groupIdentity(colors);
		U0next = factory.groupIdentity(colors);
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

	public GroupElement getU0() {return U0; }

	public void setU0(GroupElement link) { U0.set(link); }

	public GroupElement getU0next() {return U0next; }

	public void setU0next(GroupElement link) { U0next.set(link); }

	public AlgebraElement getFieldStrength(int i, int j) {
		return F[i][j];
	}

	public void setFieldStrength(int i, int j, AlgebraElement field) {
		F[i][j].set(field);
	}

	public boolean isEvaluatable() {
		return evaluatable;
	}

	public void setEvaluatable(boolean value) {
		this.evaluatable = value;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean value) {
		this.active = value;
	}


	public void resetCurrent() {
		for (int i=0;i<J.length;i++) {
			J[i].reset();
		}
	}

	public void resetUnext(int colors) {
		for (int i=0;i<Unext.length;i++) {
			Unext[i].set(factory.groupIdentity(colors));
		}
		U0next.set(factory.groupIdentity());
	}

	public void resetCharge() {
		rho.reset();
	}

	public void reassignLinks() {
		GroupElement[] temp = U;
	    U = Unext;
	    Unext = temp;

		GroupElement tmp = U0;
		U0 = U0next;
		U0next = tmp;
	}
	 
	/**
	 * Copies the values from other cell.
	 * A safer way would be to copy the fields through reflection
	 * with use of ClassCopier (see package util).
	 * However, since this code is used in distributed version in every step, it needs to be fast;
	 * thus, a manual solution is more preferable than reflection.
	 */
	public void copyFrom(Cell other) {
		
		for (int i = 0; i < F.length; i++) {
			for (int j = 0; j < F.length; j++) {
				this.F[i][j] = other.F[i][j].copy();
			}
		}

		for(int i = 0; i < E.length; i++) {
			this.E[i] = other.E[i].copy();
			this.J[i] = other.J[i].copy();
			this.U[i] = other.U[i].copy();
			this.Unext[i] = other.Unext[i].copy();
		}
		this.rho.set(other.rho);
		this.U0.set(other.U0);
		this.U0next.set(other.U0next);
		this.evaluatable = other.evaluatable;
		this.active = other.active;
	}

	/**
	 * Returns of a copy of the Cell instance.
	 *
	 * @return	copy of the current instance
	 */
	public Cell copy() {
		Cell copiedCell = new Cell(dimensions, colors, factory);
		copiedCell.copyFrom(this);
		return copiedCell;
	}
/*
	@Override
	public String toString() {
		return String.format("E[%.3f,%.3f] B[%.3f,%.3f] J[%.3f,%.3f]", Ex, Ey, Ez, Bx, By, Bz, jx, jy, jz);
	}*/
}
