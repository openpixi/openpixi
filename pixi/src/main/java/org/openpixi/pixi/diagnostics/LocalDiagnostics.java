package org.openpixi.pixi.diagnostics;

import org.openpixi.pixi.diagnostics.methods.GridMethod;
import org.openpixi.pixi.diagnostics.methods.ParticleMethod;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import java.util.ArrayList;
import java.util.List;

public class LocalDiagnostics {
	
	private Grid grid;
	private ArrayList<Particle> particles;
	
	private List<ParticleMethod> particleDiagnostics;
	private List<GridMethod> gridDiagnostics;
	
	
	public LocalDiagnostics(Grid grid, ArrayList<Particle> particles, Settings stt) {
		
		this.grid = grid;
		this.particles = particles;
		particleDiagnostics = stt.getParticleDiagnostics();
		gridDiagnostics = stt.getGridDiagnostics();		
	}
	
	public void particles() {
		for(ParticleMethod m : particleDiagnostics){
			m.calculate(particles);
		}
	}
	
	public void grid() {
		for(GridMethod m : gridDiagnostics) {
			m.calculate(grid);
		}
	}
	
	public void outputParticles(ParticleDataOutput pout) {
		for(ParticleMethod m : particleDiagnostics){
			m.getData(pout);
		}
	}
	
	public void outputGrid(GridDataOutput gout) {
		for(GridMethod m : gridDiagnostics) {
			m.getData(gout);
		}
	}
}
