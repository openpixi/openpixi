package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.physics.Simulation;

public interface ICurrentGenerator {
	void applyCurrent(Simulation s);
	void initializeCurrent(Simulation s);
}
