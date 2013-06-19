package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.particles.Particle;

/**
 *Interpolates current from the particles to the grid in a way s.t. the continuity equation 
 * divJ = - drho/dt
 *is fulfilled. This condition depends also on the way how the charge density is calculated.
 *This algorithm assumes area weighting used in the CloudInCell algorithm.
 *NOTE: On a coarser grid this algorithm will give a lower current compared to a finer grid
 *when the particle travels the same absolute distance in both cases. 
 */
public class ChargeConservingCIC extends CloudInCell {

	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {
		/**X index of local origin i.e. nearest grid point BEFORE particle push*/
		int xStart;
		/**Y index of local origin i.e. nearest grid point BEFORE particle push*/
		int yStart;
		/**X index of local origin i.e. nearest grid point AFTER particle push*/
		int xEnd;
		/**Y index of local origin i.e. nearest grid point AFTER particle push*/
		int yEnd;
		/**Normalized local x coordinate BEFORE particle push*/
		double x;
		/**Normalized local y coordinate BEFORE particle push*/
		double y;
		/**Normalized distance covered in X direction*/
		double deltaX;
		/**Normalized distance covered in X direction*/
		double deltaY;
		
		x = p.getPrevX() / g.getCellWidth();
		y = p.getPrevY() / g.getCellHeight();
		
		xStart = (int) Math.floor(x + 0.5);
		yStart = (int) Math.floor(y + 0.5);
		
		deltaX = p.getX() / g.getCellWidth();
		deltaY = p.getY() / g.getCellHeight();
		
		xEnd = (int) Math.floor(deltaX + 0.5);
		yEnd = (int) Math.floor(deltaY + 0.5);
		
		deltaX -= x;
		deltaY -= y;
		
		x -= xStart;
		y -= yStart;
		
		//check if particle moves further than one cell
		if (Debug.asserts) {
			assert (Math.abs(deltaX) <= g.getCellWidth()) & (Math.abs(deltaY) <= g.getCellHeight()): "particle too fast";
		}

		//4-boundary move?
		if (xStart == xEnd && yStart == yEnd) {
			fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY, p, g, tstep);
			}
		//7-boundary move?
		else if (xStart == xEnd || yStart == yEnd) {
				sevenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, p, g, tstep);
			}
			// 10-boundary move
				else {
					tenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, p, g, tstep);
				}
	}

	/**
	 * Writes currents Jx and Jy to grid.
	 * @param lx x index of local origin
	 * @param ly y index of local origin
	 * @param x local x coordinate relative to lx BEFORE particle push
	 * @param y local y coordinate relative to ly BEFORE particle push
	 * @param deltaX x distance covered by particle (not absolute but only for this 4-boundary move)
	 * @param deltaY y distance covered by particle (not absolute but only for this 4-boundary move)
	 * @param tstep
	 */
	private void fourBoundaryMove(int lx, int ly, double x, double y,
								  double deltaX, double deltaY, Particle p, Grid g, double tstep) {
		//A few cancellations were made to reduce computation time. Till this point the algorithm has
		//calculated the area that swept over a cell boundary for a normalized grid (i.e. unit square cells).
		//and unit square charges. This area needs to be denormalized and then multiplied with the charge
		//density. But these operations cancel and no further calculations need to be done.
		g.addJx(lx, 	ly - 1, p.getCharge() * deltaX * ((1 - deltaY) / 2 - y));
		g.addJx(lx, 	ly, 	p.getCharge() * deltaX * ((1 + deltaY) / 2 + y));
		g.addJy(lx - 1, ly, 	p.getCharge() * deltaY * ((1 - deltaX) / 2 - x));
		g.addJy(lx, 	ly, 	p.getCharge() * deltaY * ((1 + deltaX) / 2 + x));

	}

	private void sevenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd,
								   double deltaX, double deltaY, Particle p, Grid g, double tstep) {
		//7-boundary move with equal y?
		if (yStart == yEnd) {
			//particle moves right?
			if (xEnd > xStart) {

				double deltaX1 = 0.5 - x;
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, - 0.5, y, deltaX, deltaY, p, g, tstep);

			}
			//particle moves left
			else {

				double deltaX1 = -(0.5 + x);
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, g, tstep);

			}
		}
		//7-boundary move with equal x?
		if (xStart == xEnd) {
			//particle moves up?
			if (yEnd > yStart) {

				double deltaY1 = 0.5 - y;
				double deltaX1 = deltaX  * (deltaY1 / deltaY);
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, g, tstep);

			}
			//particle moves down
			else {

				double deltaY1 = -(0.5 + y);
				double deltaX1 = (deltaX / deltaY) * deltaY1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, g, tstep);

			}
		}

	}

	private void tenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd,
								 double deltaX, double deltaY, Particle p, Grid g, double tstep) {
		//moved right?
		if (xEnd == (xStart+1)) {
			//moved up?
			if (yEnd == (yStart+1)) {

				double deltaX1 = 0.5 - x;

				//lower local origin
				if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

					double deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaY2 = 0.5 - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					x = deltaX2 - 0.5;
					fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert y >= 0 && y <= 0.5;
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY >=0: deltaY;
						assert x <= 0 && x >= -0.5;
					}

				}
				//upper local origin
				else {

					double deltaY1 = 0.5 - y;
					deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaX2 = 0.5 - x - deltaX1;
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					y = deltaY2 - 0.5;
					fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert x >= 0 && x <= 0.5;
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX >=0: deltaX;
						assert deltaY >=0: deltaY;
						assert y <= 0 && y >= -0.5;
					}

				}
			}
			//moved down
			else {

				double deltaY1 = -(0.5 + y);

				//lower local origin
				if(((deltaX / deltaY) * deltaY1 + x) < 0.5) {

					double deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaX2 = 0.5 - x - deltaX1;
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					y = 0.5 + deltaY2;
					fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaY1 <= 0: deltaY1;
						assert x >= 0 && x <= 0.5;
						assert deltaX1 >= 0: deltaX1;
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY <=0: deltaY;
						assert y >= 0 && y <= 0.5;
					}

				}
				//upper local origin
				else {

					double deltaX1 = 0.5 - x;
					deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaY2 = -(0.5 + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					x = deltaX2 - 0.5;
					fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 <= 0: deltaY1;
						assert y <= 0 && y >= -0.5;
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY <=0: deltaY;
						assert x <= 0 && x >= -0.5;
					}
				}
			}

		}
		//moved left
		else {
			//moved up?
			if (yEnd == (yStart+1)) {

				double deltaX1 = -(0.5 + x);
				//lower local origin
				if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

					double deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaY2 = 0.5 - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart-1, yStart, 0.5, y, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					x = 0.5 + deltaX2;
					fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert y >= 0 && y <= 0.5;
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY >=0: deltaY;
						assert x >= 0 && x <= 0.5;
					}
				}
				//upper local origin
				else {

					double deltaY1 = 0.5 - y;
					deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaX2 = -(0.5 + x + deltaX1);
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					y = deltaY2 - 0.5;
					fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY,p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert x <= 0 && x >= -0.5;
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX <=0: deltaX;
						assert deltaY >=0: deltaY;
						assert y <= 0 && y >= -0.5;
					}
				}
			}
			//moved down
			else {

				double deltaY1 = -(0.5 + y);
				//lower local origin
				if((-(deltaX / deltaY) * deltaY1 - x) < 0.5) {

					double deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1,p, g, tstep);

					double deltaX2 = -(0.5 + x + deltaX1);
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					y = 0.5 + deltaY2;
					fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaY1 <= 0: deltaY1;
						assert deltaX1 <= 0: deltaX1;
						assert x <= 0 && x >= -0.5;
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY <= 0: deltaY;
						assert y >= 0 && y <= 0.5;
					}

				}
				//upper local origin
				else {

					double deltaX1 = -(0.5 + x);
					deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, g, tstep);

					double deltaY2 = -(0.5 + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, 0.5, y, deltaX2, deltaY2, p, g, tstep);

					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY2);
					x = 0.5 + deltaX2;
					fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, g, tstep);

					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 <= 0: deltaY1;
						assert y <= 0 && y >= -0.5;
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY <= 0: deltaY;
						assert x >= 0 && x <= 0.5;
					}

				}
			}
		}
	}
}
