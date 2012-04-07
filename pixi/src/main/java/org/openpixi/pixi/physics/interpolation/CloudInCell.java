package org.openpixi.pixi.physics.interpolation;

import java.util.ArrayList;

import org.openpixi.pixi.physics.SimpleGrid;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle2D;

public class CloudInCell extends Interpolator {
	
	public CloudInCell() {
		super();
	}
	
	public void interpolateToGrid(ArrayList<Particle2D> particles, SimpleGrid g) {
		
		for(Particle2D p : particles)
		{
			int xCellPosition = (int) (p.x / g.cellWidth + 1);
			int yCellPosition = (int) (p.y / g.cellHeight + 1);
			
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
				assert xCellPosition2 * g.cellWidth > p.x;
				assert p.x > (xCellPosition2 - 1) * g.cellWidth : p.x;
				assert yCellPosition2 * g.cellHeight > p.y;
				assert p.y > (yCellPosition2 - 1) * g.cellHeight : p.y;
			}

			g.jx[xCellPosition][yCellPosition] += p.charge * p.vx * (xCellPosition2 * g.cellWidth - p.x) *
					(yCellPosition2 * g.cellHeight - p.y) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition + 1][yCellPosition] += p.charge * p.vx * (p.x - (xCellPosition2-1) * g.cellWidth) *
					(yCellPosition2 * g.cellHeight - p.y) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition][yCellPosition + 1] += p.charge * p.vx * (xCellPosition2 * g.cellWidth - p.x) *
					(p.y - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition + 1][yCellPosition + 1] += p.charge * p.vx * (p.x - (xCellPosition2-1) * g.cellWidth) *
					(p.y - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
			
			g.jy[xCellPosition][yCellPosition] += p.charge * p.vy * (xCellPosition2 * g.cellWidth - p.x) *
					(yCellPosition2 * g.cellHeight - p.y) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition + 1][yCellPosition] += p.charge * p.vy * (p.x - (xCellPosition2-1) * g.cellWidth) *
					(yCellPosition2 * g.cellHeight - p.y) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition][yCellPosition + 1] += p.charge * p.vy * (xCellPosition2 * g.cellWidth - p.x) *
					(p.y - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition + 1][yCellPosition + 1] += p.charge * p.vy * (p.x - (xCellPosition2-1) * g.cellWidth) *
					(p.y - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
		}
		
	}
	
	public void interpolateToParticle(ArrayList<Particle2D> particles) {
		
	}

}
