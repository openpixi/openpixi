package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 * Adds id to each particle for easier debugging.
 */
public privileged aspect ParticleID {

	/** Adds member variable id of type int to class Particle.*/
	int Particle.id;


	/**
	 * Modifies the return value of the method Settings.cloneParticles.
	 * Specifically, sets the id member of each particle.
	 *
	 * execution(* *..Settings.cloneParticles())
	 * -> This is called pointcut and it specifies where do we want to intercept the original code.
	 *    This places of interception are called join points.
	 *    The * and .. are wildcards where the first * stands for any return value and the *..
	 *    stands for any package.
	 *
	 * after() returning(List<Particle> particles)
	 * -> This is called advice and it specifies the action taken at a certain pointcut.
	 *    In this specific case it specifies that we want to do something with the return value of
	 *    the pointcut after the code specified in the pointcut returns.
	 */
	@AdviceName("setParticlesID")
	after() returning(List<Particle> particles): execution(* *..Settings.cloneParticles()) {
		for (int i = 0; i < particles.size(); ++i) {
			particles.get(i).id = i;
		}
	}


	/**
	 * execution(*..Particle.new(Particle)) && args(p) && this(copy)
	 * -> This is again pointcut which is combined of more pointcuts.
	 *    The args(p) and this(copy) pointcuts help us to expose the parameters
	 *    and the class executing the intercepted method, so that we can use them within the advice.
	 */
	@AdviceName("copyParticleID")
	after(Particle copy, Particle p): execution(*..Particle.new(Particle)) && args(p) && this(copy) {
		copy.id = p.id;
	}
}
