package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle;

public class Interpolator {

	Grid g;

	public Interpolator(Grid g) {

		this.g = g;

		for (Particle p: g.simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.setChargedensity(p.getCharge() / (g.getCellWidth() * g.getCellHeight()));
		}

	}

	public void interpolateToGrid(ArrayList<Particle> particles) {

	}

	public void interpolateToParticle(ArrayList<Particle> particles) {

		for (int i = 0; i < particles.size(); i++) {

		Particle p = g.simulation.particles.get(i);
		int xCellPosition = (int) Math.floor(p.getX() / g.getCellWidth());
		int yCellPosition = (int) Math.floor(p.getY() / g.getCellHeight());

		int xCellPosition2 = xCellPosition;
		int yCellPosition2 = yCellPosition;

		//periodic boundaries
		int xm = xCellPosition - 1;
		int xp = xCellPosition + 1;
		int ym = yCellPosition - 1;
		int yp = yCellPosition + 1;

		xCellPosition = checkPeriodicBoundary(xCellPosition, g.getNumCellsX());
		xm = checkPeriodicBoundary(xm, g.getNumCellsX());
		xp = checkPeriodicBoundary(xp, g.getNumCellsX());
		yCellPosition = checkPeriodicBoundary(yCellPosition, g.getNumCellsY());
		ym = checkPeriodicBoundary(ym, g.getNumCellsY());
		yp = checkPeriodicBoundary(yp, g.getNumCellsY());

		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.getCellWidth() > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.getCellWidth() : p.getX();
			assert yCellPosition2 * g.getCellHeight() > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.getCellHeight() : p.getY();
		}

		p.setEx((g.getEx(xCellPosition,yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEx(xp,yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEx(xCellPosition,yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getEx(xp,yp) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		p.setEy((g.getEy(xCellPosition,yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEy(xp,yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEy(xCellPosition,yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getEy(xp,yp) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		p.setBz((g.getBz(xCellPosition,yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getBz(xp,yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getBz(xCellPosition,yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getBz(xp,yp) * (p.getX() - (xCellPosition2 -1) * g.getCellWidth()) *
				(p.getY() - (yCellPosition2 -1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		}

	}

	public int checkPeriodicBoundary(int a, int b) {

		if (a >= b) {
			a -= b;
		}
		else {
			if (a < 0) {
				a += b;
			}
		}

		return a;
	}


}
