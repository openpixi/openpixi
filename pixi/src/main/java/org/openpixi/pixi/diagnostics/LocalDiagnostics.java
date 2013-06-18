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
	
	public void perform(int step) {
		for(ParticleMethod m : particleDiagnostics){
			m.calculate(particles);
		}
		for(GridMethod m : gridDiagnostics) {
			m.calculate(grid);
		}
	}
	
	public void output(ParticleDataOutput pout, GridDataOutput gout) {
		for(ParticleMethod m : particleDiagnostics){
			m.getData(pout);
		}
		for(GridMethod m : gridDiagnostics) {
			m.getData(gout);
		}
	}
}
