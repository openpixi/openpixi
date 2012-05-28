package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle;

public class Interpolator {

	public void interpolateToGrid(ArrayList<Particle> particles, Grid g) {

	}

	public void interpolateToParticle(ArrayList<Particle> particles, Grid g) {

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

		p.setEx((g.getEx(xCellPosition, yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEx(xp, yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEx(xCellPosition, yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getEx(xp, yp) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		p.setEy((g.getEy(xCellPosition, yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEy(xp, yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getEy(xCellPosition, yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getEy(xp, yp) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		p.setBz((g.getBz(xCellPosition, yCellPosition) *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getBz(xp, yCellPosition) * (p.getX() - (xCellPosition2 - 1) *
				g.getCellWidth()) * (yCellPosition2 * g.getCellHeight() - p.getY()) +
				g.getBz(xCellPosition, yp) * (xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
				g.getBz(xp, yp) * (p.getX() - (xCellPosition2 -1) * g.getCellWidth()) *
				(p.getY() - (yCellPosition2 -1) * g.getCellHeight())) /
				(g.getCellWidth() * g.getCellHeight()));

		}

	}

	public void interpolateChargedensity(ArrayList<Particle> particles, Grid g) {
		g.resetCurrentAndCharge();		
		double cellArea = g.getCellWidth() * g.getCellHeight();
		
		for(Particle p : particles) {
			//nearest grid point that has a lower x and y coordinate than the particle
			int xCellPosition = (int) (Math.floor(p.getX() / g.getCellWidth()));
			int yCellPosition = (int) (Math.floor(p.getY() / g.getCellHeight()));
			
			//assign a portion of the charge to the four surrounding points depending on distance
			//Math.abs is for the case when a particle is outside of the simulation area,
			//i.e. when xCellPosition or yCellPosition are > than p.getX() or p.getY() respectively
			g.addRho(xCellPosition, yCellPosition, p.getCharge() * Math.abs(((xCellPosition+1) * g.getCellWidth() - p.getX()) *
					((yCellPosition+1) * g.getCellHeight() - p.getY()) / cellArea));
			g.addRho(xCellPosition+1, yCellPosition, p.getCharge() * Math.abs((p.getX() - xCellPosition * g.getCellWidth()) *
					((yCellPosition+1) * g.getCellHeight() - p.getY()) / cellArea));
			g.addRho(xCellPosition,yCellPosition+1, p.getCharge() * Math.abs(((xCellPosition+1) * g.getCellWidth() - p.getX()) *
					(p.getY() - yCellPosition * g.getCellHeight()) / cellArea));
			g.addRho(xCellPosition + 1,yCellPosition + 1, p.getCharge() * Math.abs((p.getX() - xCellPosition * g.getCellWidth()) *
					(p.getY() - yCellPosition * g.getCellHeight()) / cellArea));
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
