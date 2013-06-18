package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.ParticleDataOutput;
import org.openpixi.pixi.physics.Particle;
import java.util.ArrayList;

public class KineticEnergy implements ParticleMethod {
	
	private double totalKineticEnergy;
	
	public void calculate(ArrayList<Particle> particles) {
		totalKineticEnergy = 0;
		
		for(Particle p : particles) {
			totalKineticEnergy += p.getMass()*(p.getVx() * p.getVx() + p.getVy()*p.getVy());
		}
		
		totalKineticEnergy = totalKineticEnergy/2;
	}
	
	public void getData(ParticleDataOutput out) {
		out.kineticEnergy(totalKineticEnergy);
	}
}
