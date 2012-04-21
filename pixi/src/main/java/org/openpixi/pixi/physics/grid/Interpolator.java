package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.*;

public class Interpolator {
	
	Grid g;
	
	public Interpolator(Grid g) {
		
		this.g = g;
		
		for (Particle2D p: g.s.particles){
			p.pd = new Particle2DData();
			//assuming rectangular particle shape i.e. area weighting
			p.pd.cd = p.charge / (g.cellWidth * g.cellHeight);
		}
		
	}
	
	public void interpolateToGrid(ArrayList<Particle2D> particles) {
		
	}
	
	public void interpolateToParticle(ArrayList<Particle2D> particles) {
		
		for (int i = 0; i < particles.size(); i++) {
			
		Particle2D p = g.s.particles.get(i);
		int xCellPosition = (int) Math.floor(p.x / g.cellWidth + 1);
		int yCellPosition = (int) Math.floor(p.y / g.cellHeight + 1);
		
		int xCellPosition2 = xCellPosition;
		int yCellPosition2 = yCellPosition;
		
		if(xCellPosition >= g.numCellsX + 1) {
			xCellPosition = g.numCellsX + 1;
		} else if(xCellPosition < 1) {
				xCellPosition = 1;
		}
		if(yCellPosition >= g.numCellsY + 1) {
			yCellPosition = g.numCellsY + 1;
		} else if(yCellPosition < 1) {
			yCellPosition = 1;
		}

		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.cellWidth > p.x : p.x;
			assert p.x > (xCellPosition2 - 1) * g.cellWidth : p.x;
			assert yCellPosition2 * g.cellHeight > p.y : p.y;
			assert p.y > (yCellPosition2 - 1) * g.cellHeight : p.y;
		}

		particles.get(i).pd.Ex = ( g.Ex[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ex[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ex[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Ex[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
		particles.get(i).pd.Ey = ( g.Ey[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ey[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Ey[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Ey[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
		particles.get(i).pd.Bz = ( g.Bz[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.x) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Bz[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * g.cellWidth) *
				(yCellPosition2 * g.cellHeight - p.y) +
				g.Bz[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * g.cellHeight) +
				g.Bz[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 -1) * g.cellWidth) *
				(p.y - (yCellPosition2 -1) * g.cellHeight) ) / (g.cellWidth * g.cellHeight);
		
		}
		
	}


}
