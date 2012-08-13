package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 * Adds id to each particle for easier debugging.
 */
public privileged aspect ParticleID {

	int Particle.id;

	@AdviceName("setParticlesID")
	after() returning(List<Particle> particles): execution(* *..Settings.cloneParticles()) {
		for (int i = 0; i < particles.size(); ++i) {
			particles.get(i).id = i;
		}
	}

	@AdviceName("copyParticleID")
	after(Particle copy, Particle p): execution(*..Particle.new(Particle)) && args(p) && this(copy) {
		copy.id = p.id;
	}
}
