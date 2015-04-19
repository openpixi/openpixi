package org.openpixi.pixi.ui.util.yaml;

import java.awt.Color;
import java.lang.reflect.Field;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.particles.Particle;

public class YamlParticle {
	public Double x;
	public Double y;
	public Double z;
	public Double r;
	public Double vx;
	public Double vy;
	public Double vz;
	public Double m;
	public Double q;
	public String color;

	public void applyTo(Settings settings) {

		Particle p = getParticle();

		settings.addParticle(p);
	}

	/**
	 * Creates a new particle and applies the settings from the
	 * YAML document to it.
	 * @return new particle
	 */
	public Particle getParticle() {
		Particle p = new Particle();

		if (x != null) {
			p.setX(x);
		}

		if (y != null) {
			p.setY(y);
		}

		if (z != null) {
			p.setZ(z);
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

		if (vz != null) {
			p.setVz(vz);
		}

		if (m != null) {
			p.setMass(m);
		}

		if (q != null) {
			p.setCharge(q);
		}

		if (color != null) {
			p.setColor(getColorFromString(color));
		}
		return p;
	}

	/**
	 * Convert a color string from the YAML file into Java color.
	 * @param colorstring This can be a name (e.g. "red", "blue", ...),
	 * a HEX code (e.g. "FFFFFF"), or "random".
	 * @return
	 */
	static private Color getColorFromString(String colorstring) {
		// Default is black
		Color c = null;

		// Check if it is a Java string (e.g. "blue", "red", ...)
		try {
			// Access Color.blue, Color.red, ... by reflection:
			Field field = Color.class.getField(colorstring);
			c = (Color)field.get(null);
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}

		if (c == null) {
			// Check if it is a HEX color code (e.g. "FFFFFF")
			try {
				c = Color.decode("#" + colorstring);
			} catch (NumberFormatException e) {
			}
		}

		if (colorstring.equals("random")) {
			// Use random color
			c = new Color((int)(Math.random() * 0x1000000));
		}

		if (c == null) {
			// Fallback color
			c = Color.black;
		}
		return c;
	}

}
