package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle;

public class CloudInCell extends InterpolatorAlgorithm {

	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {

		int xCellPosition = (int) (Math.floor((p.getX() / g.getCellWidth())));
		int yCellPosition = (int) (Math.floor((p.getY() / g.getCellHeight())));

		int xCellPosition2 = xCellPosition + 1;
		int yCellPosition2 = yCellPosition + 1;

		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.getCellWidth() > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.getCellWidth() : p.getX();
			assert yCellPosition2 * g.getCellHeight() > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.getCellHeight() : p.getY();
		}

		g.addJx(xCellPosition, yCellPosition,
				p.getCharge() * p.getVx() *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJx(xCellPosition + 1, yCellPosition,
				p.getCharge() * p.getVx() *
				(p.getX() - (xCellPosition2-1) * g.getCellWidth()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJx(xCellPosition,yCellPosition + 1,
				p.getCharge() * p.getVx() *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2-1) * g.getCellHeight()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJx(xCellPosition + 1,yCellPosition + 1,
				p.getCharge() * p.getVx() *
				(p.getX() - (xCellPosition2-1) * g.getCellWidth()) *
				(p.getY() - (yCellPosition2-1) * g.getCellHeight()) /
				(g.getCellWidth() * g.getCellHeight()));

		g.addJy(xCellPosition, yCellPosition,
				p.getCharge() * p.getVy() *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJy(xCellPosition + 1, yCellPosition,
				p.getCharge() * p.getVy() *
				(p.getX() - (xCellPosition2-1) * g.getCellWidth()) *
				(yCellPosition2 * g.getCellHeight() - p.getY()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJy(xCellPosition, yCellPosition + 1,
				p.getCharge() * p.getVy() *
				(xCellPosition2 * g.getCellWidth() - p.getX()) *
				(p.getY() - (yCellPosition2-1) * g.getCellHeight()) /
				(g.getCellWidth() * g.getCellHeight()));
		g.addJy(xCellPosition + 1, yCellPosition + 1,
				p.getCharge() * p.getVy() *
				(p.getX() - (xCellPosition2-1) * g.getCellWidth()) *
				(p.getY() - (yCellPosition2-1) * g.getCellHeight()) /
				(g.getCellWidth() * g.getCellHeight()));

	}

	@Override
	public void interpolateToParticle(Particle p, Grid g) {

		int xCellPosition = (int) Math.floor(p.getX() / g.getCellWidth());
		int yCellPosition = (int) Math.floor(p.getY() / g.getCellHeight());

		int xCellPosition2 = xCellPosition + 1;
		int yCellPosition2 = yCellPosition + 1;

		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.getCellWidth() > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.getCellWidth() : p.getX();
			assert yCellPosition2 * g.getCellHeight() > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.getCellHeight() : p.getY();
		}
                
                //Adaption since the electric field is stored in the edges of the cells
                if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
                {
                    xCellPosition--;
                    xCellPosition2--;
                }

                p.setEx((g.getEx(xCellPosition, yCellPosition) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx(xCellPosition, yCellPosition + 1) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
                                g.getEx(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));

                if(p.getX()/g.getCellWidth() - xCellPosition >1)
                {
                    xCellPosition++;
                    xCellPosition2++;
                }
                
                //redo the adaption for the x-component
                
                if(p.getY()/g.getCellHeight()-yCellPosition <0.5)
                {
                    yCellPosition--;
                    yCellPosition2--;
                }
                
                p.setEy((g.getEy(xCellPosition, yCellPosition) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy(xCellPosition, yCellPosition + 1) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getEy(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));

                //adapt the values of x/y CellPosition (2) since the B-field is located in the middle of the grid
                if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
                {
                    xCellPosition--;
                    xCellPosition2--;
                }
                
                p.setBz((g.getBz(xCellPosition, yCellPosition) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz(xCellPosition, yCellPosition + 1) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getBz(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 -0.5) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 -0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));
	}


}
