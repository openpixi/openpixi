package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle2D;

public class ChargeConservingAreaWeighting extends Interpolator {
		
	public ChargeConservingAreaWeighting(Grid g) {
		
		super(g);
		g.particledata = new ArrayList<Particle2DData>(g.s.particles.size());
		for (Particle2D p: g.s.particles){
			g.particledata.add(new Particle2DData());
		}
		
	}
	
	public void interpolateToGrid(ArrayList<Particle2D> particles) {
		
		for(int i = 0; i < g.numCellsX + 2; i++) {
			for(int k = 0; k < g.numCellsY + 2; k++) {
				g.jx[i][k] = 0.0;
				g.jy[i][k] = 0.0;
			}
		}

		//assuming rectangular particle shape i.e. area weighting
		for (int i = 0; i < particles.size(); i++) {
			
			Particle2D p = particles.get(i);
			Particle2DData pd = g.particledata.get(i);
			
			//boundary check
			if (p.x < 0 || p.x > g.s.width || p.y < 0 || p.y > g.s.height) {
				break;
			}
			
			//local origin i.e. nearest grid point BEFORE particle push
			int xStart = (int) (pd.x / g.cellWidth + 0.5 );
			int yStart = (int) (pd.y / g.cellHeight+ 0.5);
			
			int xEnd = (int) (pd.x / g.cellWidth + 0.5 );
			int yEnd = (int) (pd.y / g.cellHeight+ 0.5);
			
			double deltaX = p.x - pd.x;
			double deltaY = p.y - pd.y;
			
			//4-boundary move?
			if (xStart == xEnd && yStart == yEnd) {	
				/**local x coordinate BEFORE particle push*/
				double x = pd.x - xStart * g.cellWidth;
				/**local y coordinate BEFORE particle push*/
				double y = pd.y - yStart * g.cellHeight;
				
				fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY);				
				
				}
			//7-boundary move?
			else if (xStart == xEnd || yStart == yEnd) {
				
					sevenBoundaryMove(xStart, yStart, xEnd, yEnd, deltaX, deltaY, pd);
					
				}
				// 10-boundary move
					else {
						
						tenBoundaryMove(xStart, yStart, xEnd, yEnd, deltaX, deltaY, pd, p);
						
					}
						
			g.particledata.get(i).x = p.x;
			g.particledata.get(i).y = p.y;
			
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
	 * @param p Particle2D
	 */
	private void fourBoundaryMove(int lx, int ly, double x, double y, 
			double deltaX, double deltaY) {
		//will lead to boundary problems!
		g.jx[lx][ly-1] += deltaX * ((g.cellHeight - deltaY) / 2 - y);
		g.jx[lx][ly] += deltaX * ((g.cellHeight + deltaY) / 2 + y);
		g.jy[lx-1][ly] += deltaY * ((g.cellWidth - deltaX) / 2 - x);
		g.jy[lx][ly] += deltaY * ((g.cellWidth + deltaX) / 2 + x);
	}
	
	private void sevenBoundaryMove(int xStart, int yStart, int xEnd, int yEnd, 
			double deltaX, double deltaY, Particle2DData pd) {
		
		/**local x coordinate BEFORE particle push*/
		double x = pd.x - xStart * g.cellWidth;
		/**local y coordinate BEFORE particle push*/
		double y = pd.y - yStart * g.cellHeight;
		
		//7-boundary move with equal x?
		if (xStart == xEnd) {
			//particle moves to the right?
			if (yEnd > yStart) {
			
				double deltaX1 = (g.cellWidth / 2) - x;
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY);
							
			}
			//particle moves to the left
			else {
				
				double deltaX1 = -((g.cellWidth / 2) + x);
				double deltaY1 = (deltaY / deltaX) * deltaX1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, (g.cellWidth / 2), y, deltaX, deltaY);
				
			}
		}
		//7-boundary move with equal y?
		if (yStart == yEnd) {
			//particle moves up?
			if (xEnd > xStart) {
				
				double deltaY1 = (g.cellHeight / 2) - y;
				double deltaX1 = (deltaX / deltaY) * deltaY1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY);
				
			}
			//particle moves down
			else {
				
				double deltaY1 = -((g.cellHeight / 2) + y);
				double deltaX1 = (deltaX / deltaY) * deltaY1;
				fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
				
				deltaX -= deltaX1;
				deltaY -= deltaY1;
				y += deltaY1;
				fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY);
				
			}
		}

	}
	
	private void tenBoundaryMove (int xStart, int yStart, int xEnd, int yEnd, 
			double deltaX, double deltaY, Particle2DData pd, Particle2D p) {
		
		/**local x coordinate BEFORE particle push*/
		double x = pd.x - xStart * g.cellWidth;
		/**local y coordinate BEFORE particle push*/
		double y = pd.y - yStart * g.cellHeight;
		
		//moved right?
		if (xEnd == (xStart+1)) {
			//moved up?
			if (yEnd == (yStart+1)) {

				double deltaX1 = (g.cellWidth / 2) - x;
				
				//lower local origin
				if(((deltaY / deltaX) * deltaX1 + y) < (g.cellHeight / 2)) {
					
					double deltaY1 = (deltaY / deltaX) * deltaX1;
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaY2 = (g.cellHeight / 2) - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -(g.cellWidth / 2), y, deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = deltaX2 - (g.cellWidth / 2);					
					fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -(g.cellHeight / 2), deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, (g.cellHeight / 2), deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = -deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaY2 = -((g.cellHeight / 2) + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, -(g.cellWidth / 2), y, deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = deltaX2 - (g.cellWidth / 2);					
					fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaY2 = (g.cellHeight / 2) - y - deltaY1;
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart-1, yStart, (g.cellWidth / 2), y, deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = (g.cellWidth / 2) + deltaX2;					
					fourBoundaryMove(xEnd, yEnd, x, -(g.cellHeight / 2), deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaX2 = (g.cellWidth / 2) - x - deltaX1;
					double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart+1, x, -(g.cellHeight / 2), deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = deltaY2 - (g.cellHeight / 2);					
					fourBoundaryMove(xEnd, yEnd, -(g.cellWidth / 2), y, deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaX2 = (g.cellWidth / 2) + x + deltaX1;
					double deltaY2 = (deltaY / deltaX) * deltaX2;
					x += deltaX1;
					fourBoundaryMove(xStart, yStart-1, x, (g.cellHeight / 2), deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					y = (g.cellHeight / 2) + deltaY2;					
					fourBoundaryMove(xEnd, yEnd, (g.cellWidth / 2), y, deltaX, deltaY);
					
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
					fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1);
					
					double deltaY2 = -((g.cellHeight / 2) + y + deltaY1);
					double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
					y += deltaY1;
					fourBoundaryMove(xStart+1, yStart, (g.cellWidth / 2), y, deltaX2, deltaY2);
					
					deltaX -= (deltaX1 + deltaX2);
					deltaY -= (deltaY1 + deltaY1);
					x = (g.cellWidth / 2) + deltaX2;					
					fourBoundaryMove(xEnd, yEnd, x, (g.cellHeight / 2), deltaX, deltaY);
					
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

	
	public void interpolateToParticle(ArrayList<Particle2D> particles) {
		
	}


}
