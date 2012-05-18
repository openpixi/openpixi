package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.*;

public class Interpolator {
	
	Grid g;
	
	public Interpolator(Grid g) {
		
		this.g = g;
		
		for (Particle p: g.simulation.particles){
			p.data = new Particle2DData();
			//assuming rectangular particle shape i.e. area weighting
			p.data.chargedensity = p.charge / (g.cellWidth * g.cellHeight);
			p.data.x = p.x;
			p.data.y = p.y;
		}
		
	}
	
	public void interpolateToGrid(ArrayList<Particle> particles) {
		
	}
	
	public void interpolateToParticle(ArrayList<Particle> particles) {
		
		for (int i = 0; i < particles.size(); i++) {
			
		Particle p = g.simulation.particles.get(i);
		int xCellPosition = (int) Math.floor(p.x / g.cellWidth);
		int yCellPosition = (int) Math.floor(p.y / g.cellHeight);
		
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
			assert xCellPosition2 * g.cellWidth > p.x : p.x;
			assert p.x > (xCellPosition2 - 1) * g.cellWidth : p.x;
			assert yCellPosition2 * g.cellHeight > p.y : p.y;
			assert p.y > (yCellPosition2 - 1) * g.cellHeight : p.y;
			assert p.data != null;
		}

		p.data.Ex = ( g.Ex[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ex[xp][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ex[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Ex[xp][yp] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
		p.data.Ey = ( g.Ey[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ey[xp][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ey[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Ey[xp][yp] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
		p.data.Bz = ( g.Bz[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Bz[xp][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Bz[xCellPosition][yp] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Bz[xp][yp] * (p.x - (xCellPosition2 -1) * g.cellWidth) *
				(p.y - (yCellPosition2 -1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
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
