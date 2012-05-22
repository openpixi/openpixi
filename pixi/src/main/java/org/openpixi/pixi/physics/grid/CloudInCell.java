package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle;

public class CloudInCell extends Interpolator {

	public CloudInCell(Grid g) {

		super(g);

	}

	@Override
	public void interpolateToGrid(ArrayList<Particle> particles) {
		g.resetCurrentAndCharge();

		for(Particle p : particles)
		{
			int xCellPosition = (int) (Math.floor((p.getX() / g.cellWidth + 1)));
			int yCellPosition = (int) (Math.floor((p.getY() / g.cellHeight + 1)));

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
				assert xCellPosition2 * g.cellWidth > p.getX() : p.getX();
				assert p.getX() > (xCellPosition2 - 1) * g.cellWidth : p.getX();
				assert yCellPosition2 * g.cellHeight > p.getY() : p.getY();
				assert p.getY() > (yCellPosition2 - 1) * g.cellHeight : p.getY();
			}

			g.jx[xCellPosition][yCellPosition] += p.getCharge() * p.getVx() * (xCellPosition2 * g.cellWidth - p.getX()) *
					(yCellPosition2 * g.cellHeight - p.getY()) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition + 1][yCellPosition] += p.getCharge() * p.getVx() * (p.getX() - (xCellPosition2-1) * g.cellWidth) *
					(yCellPosition2 * g.cellHeight - p.getY()) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition][yCellPosition + 1] += p.getCharge() * p.getVx() * (xCellPosition2 * g.cellWidth - p.getX()) *
					(p.getY() - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
			g.jx[xCellPosition + 1][yCellPosition + 1] += p.getCharge() * p.getVx() * (p.getX() - (xCellPosition2-1) * g.cellWidth) *
					(p.getY() - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);

			g.jy[xCellPosition][yCellPosition] += p.getCharge() * p.getVy() * (xCellPosition2 * g.cellWidth - p.getX()) *
					(yCellPosition2 * g.cellHeight - p.getY()) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition + 1][yCellPosition] += p.getCharge() * p.getVy() * (p.getX() - (xCellPosition2-1) * g.cellWidth) *
					(yCellPosition2 * g.cellHeight - p.getY()) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition][yCellPosition + 1] += p.getCharge() * p.getVy() * (xCellPosition2 * g.cellWidth - p.getX()) *
					(p.getY() - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
			g.jy[xCellPosition + 1][yCellPosition + 1] += p.getCharge() * p.getVy() * (p.getX() - (xCellPosition2-1) * g.cellWidth) *
					(p.getY() - (yCellPosition2-1) * g.cellHeight) / (g.cellWidth * g.cellHeight);
		}

	}

	public void interpolateToParticle(ArrayList<Particle> particles) {

		for (int i = 0; i < particles.size(); i++) {

		Particle p = g.simulation.particles.get(i);
		int xCellPosition = (int) Math.floor(p.getX() / g.cellWidth + 1);
		int yCellPosition = (int) Math.floor(p.getY() / g.cellHeight + 1);

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
			assert xCellPosition2 * g.cellWidth > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.cellWidth : p.getX();
			assert yCellPosition2 * g.cellHeight > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.cellHeight : p.getY();
		}

			particles.get(i).setEx((g.Ex[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ex[xCellPosition + 1][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ex[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Ex[xCellPosition + 1][yCellPosition + 1] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));

			particles.get(i).setEy((g.Ey[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ey[xCellPosition + 1][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Ey[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Ey[xCellPosition + 1][yCellPosition + 1] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));

			particles.get(i).setBz((g.Bz[xCellPosition][yCellPosition] * (xCellPosition2 * g.cellWidth - p.getX()) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Bz[xCellPosition + 1][yCellPosition] * (p.getX() - (xCellPosition2 - 1) * g.cellWidth) * (yCellPosition2 * g.cellHeight - p.getY()) + g.Bz[xCellPosition][yCellPosition + 1] * (xCellPosition2 * g.cellWidth - p.getX()) * (p.getY() - (yCellPosition2 - 1) * g.cellHeight) + g.Bz[xCellPosition + 1][yCellPosition + 1] * (p.getX() - (xCellPosition2 -1) * g.cellWidth) * (p.getY() - (yCellPosition2 -1) * g.cellHeight)) / (g.cellWidth * g.cellHeight));

		}

	}


}
