package org.openpixi.pixi.diagnostics;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import java.util.ArrayList;
import java.util.List;

public class LocalDiagnostics {
	
	private Grid grid;
	private ArrayList<Particle> particles;
	
	private List<ParticleDiagnosticMethod> particleDiagnostics;
	private List<GridDiagnosticMethod> gridDiagnostics;
	
	
	public LocalDiagnostics(Grid grid, ArrayList<Particle> particles, Settings stt) {
		
		this.grid = grid;
		this.particles = particles;
		particleDiagnostics = stt.getParticleDiagnostics();
		gridDiagnostics = stt.getGridDiagnostics();
		particleDiagnostics.add(new KineticEnergy());
		
	}
	
	public void perform(int step) {
		for(ParticleDiagnosticMethod m : particleDiagnostics){
			m.calculate(particles);
		}
		for(GridDiagnosticMethod m : gridDiagnostics) {
			m.calculate(grid);
		}
	}
	
	public void output(ParticleDataOutput pout, GridDataOutput gout) {
		for(ParticleDiagnosticMethod m : particleDiagnostics){
			m.getData(pout);
		}
		for(GridDiagnosticMethod m : gridDiagnostics) {
			m.getData(gout);
		}
	}
}
