package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle2D;

public class ChargeConservingAreaWeighting extends Interpolator {
	
	public ChargeConservingAreaWeighting(Grid g) {
		
		super(g);

	}
	
	public void interpolateToGrid(ArrayList<Particle2D> particles) {
		
		//assuming rectangular particle shape i.e. area weighting
		for (int i = 0; i < particles.size(); i++) {
			
			Particle2D p = particles.get(i);
			//Particle2DData pd = g.particledata.get(i);
			
			//boundary check
			if (p.x < 0 || p.x > g.s.width || p.y < 0 || p.y > g.s.height) {
				break;
			}
			
			//local origin i.e. nearest grid point BEFORE particle push
			int xStart = (int) (p.pd.x / g.cellWidth + 0.5 );
			int yStart = (int) (p.pd.y / g.cellHeight+ 0.5);
			
			//local origin i.e. nearest grid point AFTER particle push
			int xEnd = (int) (p.x / g.cellWidth + 0.5 );
			int yEnd = (int) (p.y / g.cellHeight+ 0.5);
			
			double deltaX = p.x - p.pd.x;
			double deltaY = p.y - p.pd.y;
			
			//4-boundary move?
			if (xStart == xEnd && yStart == yEnd) {	
				/**local x coordinate BEFORE particle push*/
				double x = p.pd.x - xStart * g.cellWidth;
				/**local y coordinate BEFORE particle push*/
				double y = p.pd.y - yStart * g.cellHeight;
				
				fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY, p);				
				
				}
			//7-boundary move?
			else if (xStart == xEnd || yStart == yEnd) {
				
					sevenBoundaryMove(xStart, yStart, xEnd, yEnd, deltaX, deltaY, p);
					
				}
				// 10-boundary move
					else {
						
						tenBoundaryMove(xStart, yStart, xEnd, yEnd, deltaX, deltaY, p);
						
					}
						
			particles.get(i).pd.x = p.x;
			particles.get(i).pd.y = p.y;
			
		}
	}
	
	/**
	 * Writes current to jx and jy arrays
	 * @param lx x index of local origin
	 * @param ly y index of local origin
	 * @param x local x coordinate relative to lx BEFORE particle push
	 * @param y local y coordinate relative to ly BEFORE particle push
	 * @param deltaX x distance covered by particle (not absolute but only for this 4-boundary move)
	 * @param deltaY y distance covered by particle (not absolute but only for this 4-boundary move)
	 * @param pd Particle2DData
	 */
	private void fourBoundaryMove(int lx, int ly, double x, double y, 
			double deltaX, double deltaY, Particle2D p) {
		
		int lxm = lx - 1;
		int lym = ly - 1;
		
		//System.out.println(lx + " " + lxm + " " + ly + " " + lym);
		
		lx = checkPeriodicBoundary(lx, g.numCellsX);
		lxm = checkPeriodicBoundary(lxm, g.numCellsX);
		ly = checkPeriodicBoundary(ly, g.numCellsY);
		lym = checkPeriodicBoundary(lym, g.numCellsY);
		
		//System.out.println(lx + " " + lxm + " " + ly + " " + lym);
		
		g.jx[lx][lym] += p.pd.cd * deltaX * ((g.cellHeight - deltaY) / 2 - y);
		g.jx[lx][ly] += p.pd.cd * deltaX * ((g.cellHeight + deltaY) / 2 + y);
		g.jy[lxm][ly] += p.pd.cd * deltaY * ((g.cellWidth - deltaX) / 2 - x);
		g.jy[lx][ly] += p.pd.cd * deltaY * ((g.cellWidth + deltaX) / 2 + x);
	}
	
	private void sevenBoundaryMove(int xStart, int yStart, int xEnd, int yEnd, 
			double deltaX, double deltaY, Particle2D p) {
		
		/**local x coordinate BEFORE particle push*/
		double x = p.pd.x - xStart * g.cellWidth;
		/**local y coordinate BEFORE particle push*/
		double y = p.pd.y - yStart * g.cellHeight;
		
		//7-boundary move with equal x?
		if (xStart == xEnd) {
			//particle moves to the right?
			if (yEnd > yStart) {
			
				double deltaX1 = (g.cellWidth / 2) - x;
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY, p);
							
			}
			//particle moves to the left
			else {
				
				double deltaX1 = -((g.cellWidth / 2) + x);
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, (g.cellWidth / 2), y, deltaX, deltaY, p);
				
			}
		}
		//7-boundary move with equal y?
		if (yStart == yEnd) {
			//particle moves up?
			if (xEnd > xStart) {
				
				double deltaY1 = (g.cellHeight / 2) - y;
				double deltaX1 = (deltaX / deltaY) * deltaY1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY, p);
				
			}
			//particle moves down
			else {
				
				double deltaY1 = -((g.cellHeight / 2) + y);
				double deltaX1 = (deltaX / deltaY) * deltaY1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY, p);
				
			}
		}

	}
	
	private void tenBoundaryMove (int xStart, int yStart, int xEnd, int yEnd, 
			double deltaX, double deltaY, Particle2D p) {
		
		/**local x coordinate BEFORE particle push*/
		double x = p.pd.x - xStart * g.cellWidth;
		/**local y coordinate BEFORE particle push*/
		double y = p.pd.y - yStart * g.cellHeight;
		
		//moved right?
		if (xEnd == (xStart+1)) {
			//moved up?
			if (yEnd == (yStart+1)) {

				double deltaX1 = (g.cellWidth / 2) - x;
				
				//lower local origin
				if(((deltaY / deltaX) * deltaX1 + y) < (g.cellHeight / 2)) {
					
					double deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaY2 = (g.cellHeight / 2) - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -(g.cellWidth / 2), y, deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = deltaX2 - (g.cellWidth / 2);					
					fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert y >= 0 && y <= (g.cellHeight / 2);
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY >=0: deltaY;
						assert x <= 0 && x >= -(g.cellWidth / 2);
					}
					
				}
				//upper local origin
				else {
					
					double deltaY1 = (g.cellHeight / 2) - y;
					deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -(g.cellHeight / 2), deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert x >= 0 && x <= (g.cellWidth / 2);
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX >=0: deltaX;
						assert deltaY >=0: deltaY;
						assert y <= 0 && y >= -(g.cellHeight / 2);
					}
					
				}
			}
			//moved down
			else {

				double deltaY1 = -((g.cellHeight / 2) + y);
				
				//lower local origin
				if(((deltaX / deltaY) * deltaY1 + x) < (g.cellWidth / 2)) {
					
					double deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, (g.cellHeight / 2), deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = -deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaY1 <= 0: deltaY1;
						assert x >= 0 && x <= (g.cellWidth / 2);
						assert deltaX1 >= 0: deltaX1;
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY <=0: deltaY;
						assert y >= 0 && y <= (g.cellHeight / 2);
					}
					
				}
				//upper local origin
				else {
					
					double deltaX1 = (g.cellWidth /2) - x;
					deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaY2 = -((g.cellHeight / 2) + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -(g.cellWidth / 2), y, deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = deltaX2 - (g.cellWidth / 2);					
					fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaX1 >= 0: deltaX1;
						assert deltaY1 <= 0: deltaY1;
						assert y <= 0 && y >= -(g.cellHeight /2);
						assert deltaX2 >= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX >= 0: deltaX;
						assert deltaY <=0: deltaY;
						assert x <= 0 && x >= -(g.cellWidth / 2);
					}
				}				
			}
			
		}
		//moved left
		else {
			//moved up?
			if (yEnd == (yStart+1)) {
				
				double deltaX1 = -((g.cellWidth / 2) + x);
				//lower local origin
				if(((deltaY / deltaX) * deltaX1 + y) < (g.cellHeight/ 2)) {
					
					double deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaY2 = (g.cellHeight / 2) - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart-1, yStart, (g.cellWidth / 2), y, deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = (g.cellWidth / 2) + deltaX2;					
					fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert y >= 0 && y <= (g.cellHeight / 2);
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY >=0: deltaY;
						assert x >= 0 && x <= (g.cellWidth / 2);
					}
				}
				//upper local origin
				else {
					
					double deltaY1 = (g.cellHeight / 2) - y;
					deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -(g.cellHeight / 2), deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY,p);
					
					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 >= 0: deltaY1;
						assert x <= 0 && x >= -(g.cellWidth / 2);
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 >= 0: deltaY2;
						assert deltaX <=0: deltaX;
						assert deltaY >=0: deltaY;
						assert y <= 0 && y >= -(g.cellHeight / 2);
					}
				}				
			}
			//moved down
			else {
				
				double deltaY1 = -((g.cellHeight / 2) + y);
				//lower local origin
				if((-(deltaX / deltaY) * deltaY1 - x) < (g.cellWidth/ 2)) {
					
					double deltaX1 = (deltaX / deltaY) * deltaY1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1,p);
					
					double deltaX2 = (g.cellWidth / 2) + x + deltaX1;
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, (g.cellHeight / 2), deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = (g.cellHeight / 2) + deltaY2;					
					fourBoundaryMove(xEnd, yEnd, (g.cellWidth / 2), y, deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaY1 <= 0: deltaY1;
						assert deltaX1 <= 0: deltaX1;
						assert x <= 0 && x >= -(g.cellWidth / 2);
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY <= 0: deltaY;
						assert y >= 0 && y <= (g.cellHeight / 2);
					}
					
				}
				//upper local origin
				else {
					
					double deltaX1 = -((g.cellWidth /2) + x);
					deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p);
					
					double deltaY2 = -((g.cellHeight / 2) + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, (g.cellWidth / 2), y, deltaX2, deltaY2, p);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = (g.cellWidth / 2) + deltaX2;					
					fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY, p);
					
					if (Debug.asserts) {
						assert deltaX1 <= 0: deltaX1;
						assert deltaY1 <= 0: deltaY1;
						assert y <= 0 && y >= -(g.cellHeight /2);
						assert deltaX2 <= 0: deltaX2;
						assert deltaY2 <= 0: deltaY2;
						assert deltaX <= 0: deltaX;
						assert deltaY <= 0: deltaY;
						assert x >= 0 && x <= (g.cellWidth / 2);
					}
					
				}			
			}
		}
	}

}
