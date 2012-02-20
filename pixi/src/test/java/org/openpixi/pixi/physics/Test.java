package org.openpixi.pixi.physics;

public class Test {
	
	   public static void main(String[] args)
	   {
		   Particle3D par1 = new Particle3D();
		   par1.x = 1;
		   par1.y = 1;
		   par1.z = 1;
		   par1.setCharge(8);
		   par1.setMass(5);
		   
		   Particle3D par2 = new Particle3D();
		   par1.x = 2;
		   par1.y = 2;
		   par1.z = 2;
		   par1.vx = 1;
		   par1.vy = 1;
		   par1.vz = 1;
		   par1.setCharge(9);
		   par1.setMass(7);
		   
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
		   
		   System.out.println(par1.rangeBetween3D(par2));
		   if (par1.rangeBetween3D(par2) != Math.sqrt(2.))
		   {
			   System.out.println("Error, expect sqrt(2)");
		   }
	   }
	   
	
}
