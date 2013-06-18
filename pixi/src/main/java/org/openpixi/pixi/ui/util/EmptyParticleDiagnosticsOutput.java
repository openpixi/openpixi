package org.openpixi.pixi.ui.util;

import org.openpixi.pixi.diagnostics.ParticleDataOutput;

public class EmptyParticleDiagnosticsOutput implements ParticleDataOutput {

	public void kineticEnergy(double var) {
		//DO NOTHING
	}
	
	public void closeStreams() {
		//DO NOTHING
	}
}
