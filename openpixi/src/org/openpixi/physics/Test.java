package org.openpixi.physics;
import org.openpixi.physics.Particle;

public class Test {
	
	   public static void main(String[] args)
	   {
		   Particle par1 = new Particle(1, 1, 1, 0, 0, 0, 8, 5);
		   
		   Particle par2 = new Particle(2, 2, 2, 1, 1, 1, 9, 7);
		   
		   System.out.println(par1.x);
		   System.out.println(par1.y);
		   System.out.println(par1.z);
		   System.out.println(par1.vx);
		   System.out.println(par1.vy);
		   System.out.println(par1.vz);
		   System.out.println(par1.getMass());
		   par1.setMass(18);
		   System.out.println(par1.getMass());
		   System.out.println(par1.getCharge());
		   par1.setCharge(15);
		   System.out.println(par1.getCharge());
		   
		   System.out.println(par2.x);
		   System.out.println(par2.y);
		   System.out.println(par2.z);
		   System.out.println(par2.vx);
		   System.out.println(par2.vy);
		   System.out.println(par2.vz);
		   System.out.println(par2.getMass());
		   par2.setMass(19);
		   System.out.println(par2.getMass());
		   System.out.println(par2.getCharge());
		   par2.setCharge(17);
		   System.out.println(par2.getCharge());
		   
		   System.out.println(par1.rangeFromCenter3D());
		   System.out.println(par2.rangeFromCenter3D());
		   System.out.println(par1.rangeBetween3D(par2));
		   System.out.println(par2.rangeBetween3D(par1));
		   
		   System.out.println(par1.rangeBetween2D(par2));
		   if (par1.rangeBetween2D(par2) != Math.sqrt(2.))
		   {
			   System.out.println("Error, expect sqrt(2)");
		   }
	   }
	   
	
}
