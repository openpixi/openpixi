package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class SimpleGridForce extends Force {

	Simulation s;

	/** New empty force */
	public SimpleGridForce(Simulation s) {
		super();
		this.s = s;
	}

	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * s.currentGrid.Ex[xCellPosition][yCellPosition] +
				par.charge * par.vy * s.currentGrid.Bz[xCellPosition][yCellPosition];
	}

	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * s.currentGrid.Ey[xCellPosition][yCellPosition] -
				par.charge * par.vx * s.currentGrid.Bz[xCellPosition][yCellPosition];
	}

	@Override
	public double getPositionComponentofForceX(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * s.currentGrid.Ex[xCellPosition][yCellPosition];
	}

	@Override
	public double getPositionComponentofForceY(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * s.currentGrid.Ey[xCellPosition][yCellPosition];
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * par.vy * s.currentGrid.Bz[xCellPosition][yCellPosition];
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return - par.charge * par.vx * s.currentGrid.Bz[xCellPosition][yCellPosition];
	}

	@Override
	public double getBz(Particle2D par) {
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return s.currentGrid.Bz[xCellPosition][yCellPosition];
	}
}
