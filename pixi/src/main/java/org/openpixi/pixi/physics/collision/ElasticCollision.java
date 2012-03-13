/*Since the whole calculation is a little bit tricky for one to understand just from looking at the code,
 * I shall do the calculation on a different place soon and make sure that there is a link here.
 * Practically I am first doing a coordinate change, where the x - axis is the collision line of the 2 particles,
 * than I am doing a simple calculation based on the conservation of the momentum to calculate the new velocities and on the end
 * I am making another transformation back to the normal x - y - coordinate system.
 */

package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.solver.*;
import java.lang.Math;
import java.util.ArrayList;


public class ElasticCollision extends Collision{
	
	public ElasticCollision() {
		super();
	}
	
	public void doCollision(Particle2D p1, Particle2D p2)
	{
		//double convert = Math.PI / 180;
		double phi = 0.0;
		
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		
		//finding the angle between the normal coordinate system and the system, where the x - axis is the collision line
		if(dx == 0.)
			phi = Math.PI;
		else
			phi = Math.atan(dy / dx);
		
		//double v1 = Math.sqrt(p1.vx * p1.vx + p1.vy * p1.vy);
		//double v2 = Math.sqrt(p2.vx * p2.vx + p2.vy * p2.vy);
		
		//double theta1 = Math.atan(p1.vy / p1.vx);
		//double theta2 = Math.atan(p2.vy / p2.vx);
		
		//calculating the velocities in the new coordinate system
		/*double v1xNewCoor = v1 * Math.cos(theta1 - phi);
		double v1yNewCoor = v1 * Math.sin(theta1 - phi);
		double v2xNewCoor = v2 * Math.cos(theta2 - phi);
		double v2yNewCoor = v2 * Math.sin(theta2 - phi);*/
		double v1xNewCoor = p1.vx * Math.cos(phi) + p1.vy * Math.sin(phi);
		double v1yNewCoor = - p1.vx * Math.sin(phi) + p1.vy * Math.cos(phi);
		double v2xNewCoor = p2.vx * Math.cos(phi) + p2.vy * Math.sin(phi);
		double v2yNewCoor = - p2.vx * Math.sin(phi) + p2.vy * Math.sin(phi);
		
		//calculation the new velocities in the new coordinate system
		//http://en.wikipedia.org/wiki/Elastic_collision
		double newv1xNewCoor = ((p1.mass - p2.mass) * v1xNewCoor + 2 * p2.mass * v2xNewCoor) / (p1.mass + p2.mass);
		double newv2xNewCoor = (2 * p1.mass * v1xNewCoor + (p2.mass - p1.mass) * v2xNewCoor) / (p1.mass + p2.mass);
		
		//going in the old coordinate system, do not forget that the y coordinates in the new coordinate system have not changed
		//also I am using here that cos(pi + x) = - sin(x) & sin(pi + x) = cos(x)
		p1.vx = newv1xNewCoor * Math.cos(phi) + v1yNewCoor * Math.cos(phi + Math.PI);
		p1.vy = newv1xNewCoor * Math.sin(phi) + v1yNewCoor * Math.sin(phi + Math.PI);
		p2.vx = newv2xNewCoor * Math.cos(phi) + v2yNewCoor * Math.cos(phi + Math.PI);
		p2.vy = newv2xNewCoor * Math.sin(phi) + v2yNewCoor * Math.sin(phi + Math.PI);
	}
	
	/*for this method I need to integrate a few more things to avoid for example particles getting stuck together,
	 * and solve couple of more problems, be patient!
	 */
	public void check(ArrayList<Particle2D> parlist, Force f, Solver s, double step)
	{
		for(int i = 0; i < (parlist.size() - 1); i++)
		{
			Particle2D p1 = (Particle2D) parlist.get(i);
			//double x1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y);
			for(int k = (i + 1); k < parlist.size(); k++)
			{
				Particle2D p2 = (Particle2D) parlist.get(k);
				double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
				if(distance <= (p1.radius + p2.radius)) {
					s.complete(p1, f, step);
					s.complete(p2, f, step);
					doCollision(p1, p2);
					s.prepare(p1, f, step);
					s.prepare(p2, f, step);
				}
			}
			
		}
	}

}
