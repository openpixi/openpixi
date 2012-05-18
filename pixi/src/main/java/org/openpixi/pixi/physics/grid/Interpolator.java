package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.*;

public class Interpolator {
	
	Grid g;
	
	public Interpolator(Grid g) {
		
		this.g = g;
		
		for (Particle p: g.simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.setChargedensity(p.getCharge() / (g.cellWidth * g.cellHeight));
			p.setPrevX(p.getX());
			p.setPrevY(p.getY());
		}
		
	}
	
	public void interpolateToGrid(ArrayList<Particle> particles) {
		
	}
	
	public void interpolateToParticle(ArrayList<Particle> particles) {
		
		for (int i = 0; i < particles.size(); i++) {
			
		Particle p = g.simulation.particles.get(i);
		int xCellPosition = (int) Math.floor(p.getX() / g.cellWidth);
		int yCellPosition = (int) Math.floor(p.getY() / g.cellHeight);
		
		int xCellPosition2 = xCellPosition;
		int yCellPosition2 = yCellPosition;
		
		//periodic boundaries
		int xm = xCellPosition - 1;
		int xp = xCellPosition + 1;
		int ym = yCellPosition - 1;
		int yp = yCellPosition + 1;
		
		xCellPosition = checkPeriodicBoundary(xCellPosition, g.numCellsX);
		xm = checkPeriodicBoundary(xm, g.numCellsX);
		xp = checkPeriodicBoundary(xp, g.numCellsX);
		yCellPosition = checkPeriodicBoundary(yCellPosition, g.numCellsY);
		ym = checkPeriodicBoundary(ym, g.numCellsY);
		yp = checkPeriodicBoundary(yp, g.numCellsY);
		
		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.cellWidth > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.cellWidth : p.getX();
			assert yCellPosition2 * g.cellHeight > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.cellHeight : p.getY();
		}

			p.setEx((g.Ex[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ex[xp][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ex[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Ex[xp][yp] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));
		
			p.setEy((g.Ey[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ey[xp][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ey[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Ey[xp][yp] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));
		
			p.setBz((g.Bz[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Bz[xp][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Bz[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Bz[xp][yp] * (p.getX() - (xCellPosition2 -1) * g.cellWidth) * (p.getY() - (yCellPosition2 -1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));
		
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
