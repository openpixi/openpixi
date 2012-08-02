package org.openpixi.pixi.aspectj;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;

/**
 * Adds id to each particle for easier debugging.
 */
public privileged aspect ParticleID {

	int Particle.id;

	after(Settings s): execution(* *..getParticles()) && target(s) {
		for (int i = 0; i < s.particles.size(); ++i) {
			s.particles.get(i).id = i;
		}
	}
}
