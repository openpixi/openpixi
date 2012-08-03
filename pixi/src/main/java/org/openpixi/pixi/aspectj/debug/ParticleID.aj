package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;

/**
 * Adds id to each particle for easier debugging.
 */
public privileged aspect ParticleID {

	int Particle.id;

	@AdviceName("setParticlesID")
	after(Settings s): execution(* *..getParticles()) && target(s) {
		for (int i = 0; i < s.particles.size(); ++i) {
			s.particles.get(i).id = i;
		}
	}

	@AdviceName("copyParticleID")
	after(Particle copy, Particle p): execution(*..Particle.new(Particle)) && args(p) && this(copy) {
		copy.id = p.id;
	}
}
