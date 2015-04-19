package org.openpixi.pixi.ui.util.yaml;

import java.util.Random;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.particles.Particle;

public class YamlParticleStream {
	public YamlParticle particle;
	public Double dx;
	public Double dy;
	public Double dz;
	public Double randomX;
	public Double randomY;
	public Double randomZ;
	public Double randomVx;
	public Double randomVy;
	public Double randomVz;
	public Double randomGaussX;
	public Double randomGaussY;
	public Double randomGaussZ;
	public Double randomGaussVx;
	public Double randomGaussVy;
	public Double randomGaussVz;
	public Integer number;

	private Random random = new Random();

	/**
	 * Creates a stream of particles. The particle is copied
	 * 'number' times. Each time, the x-, y-, and z-values are
	 * adjusted by adding dx, dy, and dz.
	 * @param settings Settings object to which particles are added.
	 */
	public void applyTo(Settings settings) {
		Particle p;
		double dx = 0;
		double dy = 0;
		double dz = 0;
		double number = 0;

		if (this.dx != null) {
			dx = this.dx;
		}

		if (this.dy != null) {
			dy = this.dy;
		}

		if (this.dz != null) {
			dz = this.dz;
		}

		if (this.number != null) {
			number = this.number;
		}

		if (this.particle != null) {
			p = particle.getParticle();

			double x = p.getX();
			double y = p.getY();
			double z = p.getZ();

			for (int i = 0; i < number; i++) {
				p = particle.getParticle();
				p.setX(x + i * dx);
				p.setY(y + i * dy);
				p.setZ(z + i * dz);
				applyRandomModifications(p);
				settings.addParticle(p);
			}
		}
	}

	private void applyRandomModifications(Particle p) {
		if (this.randomX != null) {
			p.addX(random.nextDouble() * this.randomX);
		}

		if (this.randomY != null) {
			p.addY(random.nextDouble() * this.randomY);
		}

		if (this.randomZ != null) {
			p.addZ(random.nextDouble() * this.randomZ);
		}

		if (this.randomVx != null) {
			p.setVx(p.getVx() + random.nextDouble() * this.randomVx);
		}

		if (this.randomVy != null) {
			p.setVy(p.getVy() + random.nextDouble() * this.randomVy);
		}

		if (this.randomVz != null) {
			p.setVz(p.getVz() + random.nextDouble() * this.randomVz);
		}

		if (this.randomGaussX != null) {
			p.addX(random.nextGaussian() * this.randomGaussX);
		}

		if (this.randomGaussY != null) {
			p.addY(random.nextGaussian() * this.randomGaussY);
		}

		if (this.randomGaussZ != null) {
			p.addZ(random.nextGaussian() * this.randomGaussZ);
		}

		if (this.randomGaussVx != null) {
			p.setVx(p.getVx() + random.nextGaussian() * this.randomGaussVx);
		}

		if (this.randomGaussVy != null) {
			p.setVy(p.getVy() + random.nextGaussian() * this.randomGaussVy);
		}

		if (this.randomGaussVz != null) {
			p.setVz(p.getVz() + random.nextGaussian() * this.randomGaussVz);
		}
	}
}
