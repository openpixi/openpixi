package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.particles.IParticle;

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
	public void interpolateToGrid(IParticle p, Grid g, double tstep) {
		
		if(g.getNumCellsZ() > 1) {
			interpolateToGrid3D(p, g, tstep);
			return;
		}
		
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
		
        //we start by shifting the particle by (-0.5, -0.5)
        //since the grid for the current is shifted by (0.5,0.5)
        p.addPrevX(-0.5*g.getCellWidth());
        p.addPrevY(-0.5*g.getCellHeight());
        p.addX(-0.5*g.getCellWidth());
        p.addY(-0.5*g.getCellHeight());
		
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
        p.addPrevX(0.5*g.getCellWidth());
        p.addX(0.5*g.getCellWidth());
        p.addPrevY(0.5*g.getCellHeight());
        p.addY(0.5*g.getCellHeight());
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
								  double deltaX, double deltaY, IParticle p, Grid g, double tstep) {
		//A few cancellations were made to reduce computation time. Till this point the algorithm has
		//calculated the area that swept over a cell boundary for a normalized grid (i.e. unit square cells).
		//and unit square charges. This area needs to be denormalized and then multiplied with the charge
		//density. But these operations cancel and no further calculations need to be done.
		//g.addJx(lx, 	ly - 1, p.getCharge() * deltaX * ((1 - deltaY) / 2 - y));
		//g.addJx(lx, 	ly, 	p.getCharge() * deltaX * ((1 + deltaY) / 2 + y));
		//g.addJy(lx - 1, ly, 	p.getCharge() * deltaY * ((1 - deltaX) / 2 - x));
		//g.addJy(lx, 	ly, 	p.getCharge() * deltaY * ((1 + deltaX) / 2 + x));
		/*
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), p.getCharge() * deltaX * ((1 - deltaY) / 2 - y) * g.getCellWidth() / tstep);
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + 1 + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge()* deltaX * ((1 + deltaY) / 2 + y) * g.getCellWidth() / tstep);
		g.addJy((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 - deltaX) / 2 - x) * g.getCellHeight() / tstep);
		g.addJy((lx +1 + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 + deltaX) / 2 + x) * g.getCellHeight() / tstep);
		*/
		
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), p.getCharge() * deltaX * ((1 - deltaY) / 2 - y) / g.getCellWidth() / tstep);
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + 1 + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge()* deltaX * ((1 + deltaY) / 2 + y) / g.getCellWidth() / tstep);
		g.addJy((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 - deltaX) / 2 - x) / g.getCellHeight() / tstep);
		g.addJy((lx +1 + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 + deltaX) / 2 + x) / g.getCellHeight() / tstep);
		
		/*
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), p.getCharge() * deltaX * ((1 - deltaY) / 2 - y));
		g.addJx((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + 1 + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge()* deltaX * ((1 + deltaY) / 2 + y));
		g.addJy((lx + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 - deltaX) / 2 - x));
		g.addJy((lx +1 + g.getNumCellsX())%g.getNumCellsX(), (ly + g.getNumCellsY())%g.getNumCellsY(), 	p.getCharge() * deltaY * ((1 + deltaX) / 2 + x));
		*/

	}

	private void sevenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd,
								   double deltaX, double deltaY, IParticle p, Grid g, double tstep) {
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
								 double deltaX, double deltaY, IParticle p, Grid g, double tstep) {
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
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;//Error source!!!
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
	
private void interpolateToGrid3D(IParticle p, Grid g, double tstep) {
		
		/**X index of local origin i.e. nearest grid point BEFORE particle push*/
		int xStart;
		/**Y index of local origin i.e. nearest grid point BEFORE particle push*/
		int yStart;
		/**Z index of local origin i.e. nearest grid point BEFORE particle push*/
		int zStart;
		/**X index of local origin i.e. nearest grid point AFTER particle push*/
		int xEnd;
		/**Y index of local origin i.e. nearest grid point AFTER particle push*/
		int yEnd;
		/**Z index of local origin i.e. nearest grid point AFTER particle push*/
		int zEnd;
		/**Normalized local x coordinate BEFORE particle push*/
		double x;
		/**Normalized local y coordinate BEFORE particle push*/
		double y;
		/**Normalized local z coordinate BEFORE particle push*/
		double z;
		/**Normalized distance covered in X direction*/
		double deltaX;
		/**Normalized distance covered in Y direction*/
		double deltaY;
		/**Normalized distance covered in Z direction*/
		double deltaZ;    
		
		x = p.getPrevX() / g.getCellWidth();
		y = p.getPrevY() / g.getCellHeight();
		z = p.getPrevZ() / g.getCellDepth();
		
		xStart = (int) Math.floor(x);
		yStart = (int) Math.floor(y);
		zStart = (int) Math.floor(z);
		
		deltaX = p.getX() / g.getCellWidth();
		deltaY = p.getY() / g.getCellHeight();
		deltaZ = p.getZ() / g.getCellDepth();
		
		xEnd = (int) Math.floor(deltaX);
		yEnd = (int) Math.floor(deltaY);
		zEnd = (int) Math.floor(deltaZ);
		
		deltaX -= x;
		if(Math.abs(deltaX) > 1) {
			if(deltaX < 0) {deltaX += g.getNumCellsX();}
			else {deltaX -= g.getNumCellsX();}
		}
		
		deltaY -= y;
		if(Math.abs(deltaY) > 1) {
			if(deltaY < 0) {deltaY += g.getNumCellsY();}
			else {deltaY -= g.getNumCellsY();}
		}
		
		deltaZ -= z;
		if(Math.abs(deltaZ) > 1) {
			if(deltaZ < 0) {deltaZ += g.getNumCellsZ();}
			else {deltaZ -= g.getNumCellsZ();}
		}
		
		int cellCheck = Math.abs(xEnd - xStart)%(g.getNumCellsX()-2) + Math.abs(yEnd - yStart)%(g.getNumCellsY()-2) + Math.abs(zEnd - zStart)%(g.getNumCellsZ()-2);
		int checkSum = Math.abs(xEnd - xStart)%(g.getNumCellsX()-2) + (Math.abs(yEnd - yStart)%(g.getNumCellsY()-2))*2 + (Math.abs(zEnd - zStart)%(g.getNumCellsZ()-2))*3;
		//if(deltaY < 0) {System.out.println(yStart);System.out.println(yEnd);}if(cellCheck != 0) {System.out.println(cellCheck);System.out.println(checkSum);System.out.println("Test");}
		switch(cellCheck) {
		
        	case 0:  oneCellMove(xStart, yStart, zStart, x, y, z, deltaX, deltaY, deltaZ, p.getCharge(), g, tstep);
        
        	break;
        	
        	case 1:  twoCellMove(xStart, yStart, zStart, checkSum, xEnd, yEnd, zEnd, x, y, z, deltaX, deltaY, deltaZ, p.getCharge(), g, tstep);
        	
        	break;
        	
        	case 2:  threeCellMove(xStart, yStart, zStart, checkSum, xEnd, yEnd, zEnd, x, y, z, deltaX, deltaY, deltaZ, p.getCharge(), g, tstep);
        	//System.out.println(cellCheck-checkSum);System.out.println(checkSum+cellCheck);
        	break;
        	
        	case 3:  fourCellMove(xStart, yStart, zStart, xEnd, yEnd, zEnd, x, y, z, deltaX, deltaY, deltaZ, p.getCharge(), g, tstep);
        	//System.out.println(cellCheck-checkSum);System.out.println(checkSum+cellCheck);
        	break;
        	
        	default: System.out.println("Interpolation error!!! Particle moved through more than 4 cells!!!");
        	
        	break;

		}		
	}

private void oneCellMove(int xStart, int yStart, int zStart, double x, double y, double z, double deltaX, double deltaY, double deltaZ, double charge, Grid g, double tstep) {

		double cellArea = g.getCellWidth() * g.getCellHeight();
		double locX = x - xStart - 0.5;
		double locY = y - yStart - 0.5;
		double locZ = z - zStart - 0.5;
		
		g.addJx(xStart, yStart, zStart, charge * deltaX * ((1 - deltaY) / 2 - locY) * ((1 - deltaZ) / 2 - locZ) / cellArea / tstep);
		g.addJx(xStart, (yStart + 1)%g.getNumCellsY(), zStart, 	charge * deltaX * ((1 + deltaY) / 2 + locY) * ((1 - deltaZ) / 2 - locZ) / cellArea / tstep);
		g.addJx(xStart, yStart, (zStart + 1)%g.getNumCellsZ(), 	charge * deltaX * ((1 - deltaY) / 2 - locY) * ((1 + deltaZ) / 2 + locZ) / cellArea / tstep);
		g.addJx(xStart, (yStart + 1)%g.getNumCellsY(), (zStart + 1)%g.getNumCellsZ(), 	charge * deltaX * ((1 + deltaY) / 2 + locY) * ((1 + deltaZ) / 2 + locZ) / cellArea / tstep);
		
		g.addJy(xStart, yStart, zStart, charge * deltaY * ((1 - deltaZ) / 2 - locZ) * ((1 - deltaX) / 2 - locX) / cellArea / tstep);
		g.addJy(xStart, yStart, (zStart + 1)%g.getNumCellsZ(), 	charge * deltaY * ((1 + deltaZ) / 2 + locZ) * ((1 - deltaX) / 2 - locX) / cellArea / tstep);
		g.addJy((xStart + 1)%g.getNumCellsX(), yStart, zStart, 	charge * deltaY * ((1 - deltaZ) / 2 - locZ) * ((1 + deltaX) / 2 + locX) / cellArea / tstep);
		g.addJy((xStart + 1)%g.getNumCellsX(), yStart, (zStart + 1)%g.getNumCellsZ(), 	charge * deltaY * ((1 + deltaZ) / 2 + locZ) * ((1 + deltaX) / 2 + locX) / cellArea / tstep);
		
		g.addJz(xStart, yStart, zStart, charge * deltaZ * ((1 - deltaX) / 2 - locX) * ((1 - deltaY) / 2 - locY) / cellArea / tstep);
		g.addJz((xStart + 1)%g.getNumCellsX(), yStart, zStart, 	charge * deltaZ * ((1 + deltaX) / 2 + locX) * ((1 - deltaY) / 2 - locY) / cellArea / tstep);
		g.addJz(xStart, (yStart + 1)%g.getNumCellsY(), zStart, 	charge * deltaZ * ((1 - deltaX) / 2 - locX) * ((1 + deltaY) / 2 + locY) / cellArea / tstep);
		g.addJz((xStart + 1)%g.getNumCellsX(), (yStart + 1)%g.getNumCellsY(), zStart, 	charge * deltaZ * ((1 + deltaX) / 2 + locX) * ((1 + deltaY) / 2 + locY) / cellArea / tstep);
		
		/*System.out.println( g.getJy(xStart, yStart, zStart)+g.getJy(xStart, yStart, (zStart + 1)%g.getNumCellsZ())
				+g.getJy((xStart + 1)%g.getNumCellsX(), yStart, zStart)+g.getJy((xStart + 1)%g.getNumCellsX(), yStart, (zStart + 1)%g.getNumCellsZ()) );
		System.out.println(charge*deltaY/cellArea/tstep);System.out.println("Test");*/
	}

private void twoCellMove(int xStart, int yStart, int zStart, int checkSum, int xEnd, int yEnd, int zEnd, double x, double y, double z, double deltaX, double deltaY, double deltaZ, double charge, Grid g, double tstep) {

	int cellBound;
	double t, newX, newY, newZ, deltaX1, deltaY1, deltaZ1, deltaX2, deltaY2, deltaZ2;
	
		switch(checkSum) {
	
		case 1: 
			
			if( Math.abs(xStart-xEnd) != (g.getNumCellsX()-1) ) {
				if(xEnd < xStart) cellBound = xStart;
				else cellBound = xEnd;
			} else {
				if(xEnd > xStart) cellBound = 0;
				else cellBound = g.getNumCellsX();
			}
			
			t = (cellBound - x)/deltaX;
			
			deltaX1 = cellBound - x;
			deltaY1 = t * deltaY;
			deltaZ1 = t * deltaZ;
			
			if( Math.abs(xStart-xEnd) == (g.getNumCellsX()-1) ) {
				if(xEnd > xStart) newX = g.getNumCellsX();
				else newX = 0;
			} else {
				newX = cellBound;
			}
			
			newY = y + deltaY1;
			newZ = z + deltaZ1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			oneCellMove(xEnd, yStart, zStart, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
    
		break;
		
		case 2: 
			
			if( Math.abs(yStart-yEnd) != (g.getNumCellsY()-1) ) {
				if(yEnd < yStart) cellBound = yStart;
				else cellBound = yEnd;
			} else {
				if(yEnd > yStart) cellBound = 0;
				else cellBound = g.getNumCellsY();
			}
			
			t = (cellBound - y)/deltaY;
			
			deltaX1 = t * deltaX;
			deltaY1 = cellBound - y;
			deltaZ1 = t * deltaZ;
			//if(cellBound == 10) {System.out.println(deltaY1);System.out.println(t);System.out.println(deltaY);System.out.println("Test");}
			if( Math.abs(yStart-yEnd) == (g.getNumCellsY()-1) ) {
				if(yEnd > yStart) newY = g.getNumCellsY();
				else newY = 0;
			} else {
				newY = cellBound;
			}
			
			newX = x + deltaX1;
			newZ = z + deltaZ1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			//if(Math.abs(deltaY) != 0) {System.out.println(deltaY);System.out.println(deltaY1);System.out.println(deltaY2);System.out.println("Test");}
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			oneCellMove(xStart, yEnd, zStart, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
	    
		break;
		
		case 3: 
			
			if( Math.abs(zStart-zEnd) != (g.getNumCellsZ()-1) ) {
				if(zEnd < zStart) cellBound = zStart;
				else cellBound = zEnd;
			} else {
				if(zEnd > zStart) cellBound = 0;
				else cellBound = g.getNumCellsZ();
			}
			
			t = (cellBound - z)/deltaZ;
			
			deltaX1 = t * deltaX;
			deltaY1 = t * deltaY;
			deltaZ1 = cellBound - z;
			
			if( Math.abs(zStart-zEnd) == (g.getNumCellsZ()-1) ) {
				if(zEnd > zStart) newZ = g.getNumCellsZ();
				else newZ = 0;
			} else {
				newZ = cellBound;
			}
			
			newX = x + deltaX1;
			newY = y + deltaY1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			oneCellMove(xStart, yStart, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
	    
		break;
		
		default: System.out.println("Interpolation error!!! Particle moved through more or less than 2 cells!!!");
		
		break;

		}
	
	}

private void threeCellMove(int xStart, int yStart, int zStart, int checkSum, int xEnd, int yEnd, int zEnd, double x, double y, double z, double deltaX, double deltaY, double deltaZ, double charge, Grid g, double tstep) {

	int cellBound1, cellBound2;
	double t1, t2, newX, newY, newZ, deltaX1, deltaY1, deltaZ1, deltaX2, deltaY2, deltaZ2;
	
		switch(checkSum) {
	
		case 3: 
			
			if( Math.abs(xStart-xEnd) != (g.getNumCellsX()-1) ) {
				if(xEnd < xStart) cellBound1 = xStart;
				else cellBound1 = xEnd;
			} else {
				if(xEnd > xStart) cellBound1 = 0;
				else cellBound1 = g.getNumCellsX();
			}
			
			if( Math.abs(yStart-yEnd) != (g.getNumCellsY()-1) ) {
				if(yEnd < yStart) cellBound2 = yStart;
				else cellBound2 = yEnd;
			} else {
				if(yEnd > yStart) cellBound2 = 0;
				else cellBound2 = g.getNumCellsY();
			}
			
			t1 = (cellBound1 - x)/deltaX;
			t2 = (cellBound2 - y)/deltaY;
			
			if(t1 < t2) {
				
				deltaX1 = cellBound1 - x;
				deltaY1 = t1 * deltaY;
				deltaZ1 = t1 * deltaZ;
				
				if( Math.abs(xStart-xEnd) == (g.getNumCellsX()-1) ) {
					if(xEnd > xStart) newX = g.getNumCellsX();
					else newX = 0;
				} else {
					newX = cellBound1;
				}
				
				newY = y + deltaY1;
				newZ = z + deltaZ1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xEnd, yStart, zStart, 2, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			} else {
				
				deltaX1 = t2 * deltaX;
				deltaY1 = cellBound2 - y;
				deltaZ1 = t2 * deltaZ;
				
				if( Math.abs(yStart-yEnd) == (g.getNumCellsY()-1) ) {
					if(yEnd > yStart) newY = g.getNumCellsY();
					else newY = 0;
				} else {
					newY = cellBound2;
				}
				
				newX = x + deltaX1;
				newZ = z + deltaZ1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xStart, yEnd, zStart, 1, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			}
    
		break;
		
		case 4: 
			
			if( Math.abs(xStart-xEnd) != (g.getNumCellsX()-1) ) {
				if(xEnd < xStart) cellBound1 = xStart;
				else cellBound1 = xEnd;
			} else {
				if(xEnd > xStart) cellBound1 = 0;
				else cellBound1 = g.getNumCellsX();
			}
			
			if( Math.abs(zStart-zEnd) != (g.getNumCellsZ()-1) ) {
				if(zEnd < zStart) cellBound2 = zStart;
				else cellBound2 = zEnd;
			} else {
				if(zEnd > zStart) cellBound2 = 0;
				else cellBound2 = g.getNumCellsZ();
			}
			
			t1 = (cellBound1 - x)/deltaX;
			t2 = (cellBound2 - z)/deltaZ;
			
			if(t1 < t2) {
				
				deltaX1 = cellBound1 - x;
				deltaY1 = t1 * deltaY;
				deltaZ1 = t1 * deltaZ;
				
				if( Math.abs(xStart-xEnd) == (g.getNumCellsX()-1) ) {
					if(xEnd > xStart) newX = g.getNumCellsX();
					else newX = 0;
				} else {
					newX = cellBound1;
				}
				
				newY = y + deltaY1;
				newZ = z + deltaZ1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xEnd, yStart, zStart, 3, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			} else {
				
				deltaX1 = t2 * deltaX;
				deltaY1 = t2 * deltaY;
				deltaZ1 = cellBound2 - z;
				
				if( Math.abs(zStart-zEnd) == (g.getNumCellsZ()-1) ) {
					if(zEnd > zStart) newZ = g.getNumCellsZ();
					else newZ = 0;
				} else {
					newZ = cellBound2;
				}
				
				newX = x + deltaX1;
				newY = y + deltaY1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xStart, yStart, zEnd, 1, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			}
	    
		break;
		
		case 5: 
			
			if( Math.abs(zStart-zEnd) != (g.getNumCellsZ()-1) ) {
				if(zEnd < zStart) cellBound1 = zStart;
				else cellBound1 = zEnd;
			} else {
				if(zEnd > zStart) cellBound1 = 0;
				else cellBound1 = g.getNumCellsZ();
			}
			
			if( Math.abs(yStart-yEnd) != (g.getNumCellsY()-1) ) {
				if(yEnd < yStart) cellBound2 = yStart;
				else cellBound2 = yEnd;
			} else {
				if(yEnd > yStart) cellBound2 = 0;
				else cellBound2 = g.getNumCellsY();
			}
			
			t1 = (cellBound1 - z)/deltaZ;
			t2 = (cellBound2 - y)/deltaY;
			
			if(t1 < t2) {
				
				deltaX1 = t1 * deltaX;
				deltaY1 = t1 * deltaY;
				deltaZ1 = cellBound1 - z;
				
				if( Math.abs(zStart-zEnd) == (g.getNumCellsZ()-1) ) {
					if(zEnd > zStart) newZ = g.getNumCellsZ();
					else newZ = 0;
				} else {
					newZ = cellBound1;
				}
				
				newY = y + deltaY1;
				newX = x + deltaX1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xStart, yStart, zEnd, 2, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			} else {
				
				deltaX1 = t2 * deltaX;
				deltaY1 = cellBound2 - y;
				deltaZ1 = t2 * deltaZ;
				
				if( Math.abs(yStart-yEnd) == (g.getNumCellsY()-1) ) {
					if(yEnd > yStart) newY = g.getNumCellsY();
					else newY = 0;
				} else {
					newY = cellBound2;
				}
				
				newX = x + deltaX1;
				newZ = z + deltaZ1;
				
				deltaX2 = deltaX - deltaX1;
				deltaY2 = deltaY - deltaY1;
				deltaZ2 = deltaZ - deltaZ1;
				
				oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
				twoCellMove(xStart, yEnd, zStart, 3, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
				
			}
	    
		break;
		
		default: System.out.println("Interpolation error!!! Particle moved through more or less than 3 cells!!!");
		
		break;

		}
	//System.out.println(checkSum);
	}

private void fourCellMove(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, double x, double y, double z, double deltaX, double deltaY, double deltaZ, double charge, Grid g, double tstep) {

	int cellBound1, cellBound2, cellBound3;
	double newX, newY, newZ, deltaX1, deltaY1, deltaZ1, deltaX2, deltaY2, deltaZ2;
	double[] t;
	t = new double[3];
	
		if( Math.abs(xStart-xEnd) != (g.getNumCellsX()-1) ) {
			if(xEnd < xStart) cellBound1 = xStart;
			else cellBound1 = xEnd;
		} else {
			if(xEnd > xStart) cellBound1 = 0;
			else cellBound1 = g.getNumCellsX();
		}
		
		if( Math.abs(yStart-yEnd) != (g.getNumCellsY()-1) ) {
			if(yEnd < yStart) cellBound2 = yStart;
			else cellBound2 = yEnd;
		} else {
			if(yEnd > yStart) cellBound2 = 0;
			else cellBound2 = g.getNumCellsY();
		}
		
		if( Math.abs(zStart-zEnd) != (g.getNumCellsZ()-1) ) {
			if(zEnd < zStart) cellBound3 = zStart;
			else cellBound3 = zEnd;
		} else {
			if(zEnd > zStart) cellBound3 = 0;
			else cellBound3 = g.getNumCellsZ();
		}
		
		t[0] = (cellBound1 - x)/deltaX;
		t[1] = (cellBound2 - y)/deltaY;
		t[2] = (cellBound3 - z)/deltaZ;
		
		int index = 0;
		double smallest = t[0];
		
		for(int i=0;i<3;i++) {
			if(t[i] < smallest) {
				smallest = t[i];
				index = i;
			}
		}
		
		switch(index) {
		
		case 0:
			
			deltaX1 = cellBound1 - x;
			deltaY1 = t[0] * deltaY;
			deltaZ1 = t[0] * deltaZ;
			
			if( Math.abs(xStart-xEnd) == (g.getNumCellsX()-1) ) {
				if(xEnd > xStart) newX = g.getNumCellsX();
				else newX = 0;
			} else {
				newX = cellBound1;
			}
			
			newY = y + deltaY1;
			newZ = z + deltaZ1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			threeCellMove(xEnd, yStart, zStart, 5, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
			
		break;
		
		case 1:
			
			deltaX1 = t[1] * deltaX;
			deltaY1 = cellBound2 - y;
			deltaZ1 = t[1] * deltaZ;
			
			if( Math.abs(yStart-yEnd) == (g.getNumCellsY()-1) ) {
				if(yEnd > yStart) newY = g.getNumCellsY();
				else newY = 0;
			} else {
				newY = cellBound2;
			}
			
			newX = x + deltaX1;
			newZ = z + deltaZ1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			threeCellMove(xStart, yEnd, zStart, 4, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
			
		break;
		
		case 2:
			
			deltaX1 = t[2] * deltaX;
			deltaY1 = t[2] * deltaY;
			deltaZ1 = cellBound3 - z;
			
			if( Math.abs(zStart-zEnd) == (g.getNumCellsZ()-1) ) {
				if(zEnd > zStart) newZ = g.getNumCellsZ();
				else newZ = 0;
			} else {
				newZ = cellBound3;
			}
			
			newY = y + deltaY1;
			newX = x + deltaX1;
			
			deltaX2 = deltaX - deltaX1;
			deltaY2 = deltaY - deltaY1;
			deltaZ2 = deltaZ - deltaZ1;
			
			oneCellMove(xStart, yStart, zStart, x, y, z, deltaX1, deltaY1, deltaZ1, charge, g, tstep);
			threeCellMove(xStart, yStart, zEnd, 3, xEnd, yEnd, zEnd, newX, newY, newZ, deltaX2, deltaY2, deltaZ2, charge, g, tstep);
			
		break;
		
		default: System.out.println("Interpolation error!!! Error in the fourCellMove!!!");
		
		break;
			
		}
	
	}
}
