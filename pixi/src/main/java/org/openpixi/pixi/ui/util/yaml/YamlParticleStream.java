package org.openpixi.pixi.ui.util.yaml;

import java.util.Random;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.particles.ParticleFull;

public class YamlParticleStream {
	public YamlParticle particle;
	public Double dx;
	public Double dy;
	public Double randomX;
	public Double randomY;
	public Double randomVx;
	public Double randomVy;
	public Double randomGaussX;
	public Double randomGaussY;
	public Double randomGaussVx;
	public Double randomGaussVy;
	public Integer number;

	private Random random = new Random();

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
				applyRandomModifications(p);
				settings.addParticle(p);
			}
		}
	}

	private void applyRandomModifications(ParticleFull p) {
		if (this.randomX != null) {
			p.addX(random.nextDouble() * this.randomX);
		}

		if (this.randomY != null) {
			p.addY(random.nextDouble() * this.randomY);
		}

		if (this.randomVx != null) {
			p.setVx(p.getVx() + random.nextDouble() * this.randomVx);
		}

		if (this.randomVy != null) {
			p.setVy(p.getVy() + random.nextDouble() * this.randomVy);
		}
		if (this.randomGaussX != null) {
			p.addX(random.nextGaussian() * this.randomGaussX);
		}

		if (this.randomGaussY != null) {
			p.addY(random.nextGaussian() * this.randomGaussY);
		}

		if (this.randomGaussVx != null) {
			p.setVx(p.getVx() + random.nextGaussian() * this.randomGaussVx);
		}

		if (this.randomGaussVy != null) {
			p.setVy(p.getVy() + random.nextGaussian() * this.randomGaussVy);
		}
	}
}
