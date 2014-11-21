package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.particles.ParticleFull;

public class YamlParticle {
	public Double x;
	public Double y;
	public Double r;
	public Double vx;
	public Double vy;
	public Double m;
	public Double q;

	public void applyTo(Settings settings) {

		ParticleFull p = getParticle();

		settings.addParticle(p);
	}

	/**
	 * Creates a new particle and applies the settings from the
	 * YAML document to it.
	 * @return new particle
	 */
	public ParticleFull getParticle() {
		ParticleFull p = new ParticleFull();

		if (x != null) {
			p.setX(x);
		}

		if (y != null) {
			p.setY(y);
		}

		if (r != null) {
			p.setRadius(r);
		}

		if (vx != null) {
			p.setVx(vx);
		}

		if (vy != null) {
			p.setVy(vy);
		}

		if (m != null) {
			p.setMass(m);
		}

		if (q != null) {
			p.setCharge(q);
		}
		return p;
	}

}
