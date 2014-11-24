package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.particles.ParticleFull;

public class YamlParticleStream {
	public YamlParticle particle;
	public Double dx;
	public Double dy;
	public Double number;

	/**
	 * Creates a stream of particles. The particle is copied
	 * 'number' times. Each time, the x- and y-values are
	 * adjusted by adding dx and dy.
	 * @param settings Settings object to which particles are added.
	 */
	public void applyTo(Settings settings) {
		ParticleFull p;
		double dx = 0;
		double dy = 0;
		double number = 0;

		if (this.dx != null) {
			dx = this.dx;
		}

		if (this.dy != null) {
			dy = this.dy;
		}

		if (this.number != null) {
			number = this.number;
		}

		if (this.particle != null) {
			p = particle.getParticle();

			double x = p.getX();
			double y = p.getY();

			for (int i = 0; i < number; i++) {
				p = particle.getParticle();
				p.setX(x + i * dx);
				p.setY(y + i * dy);
				settings.addParticle(p);
			}
		}
	}
}
