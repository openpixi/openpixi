package org.openpixi.pixi.diagnostics;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import java.util.ArrayList;
import java.util.List;

public class LocalDiagnostics {
	
	private Grid grid;
	private ArrayList<Particle> particles;
	
	private List<DiagnosticMethod> methods;
	
	public LocalDiagnostics(Grid grid, ArrayList<Particle> particles, Settings stt) {
		
		this.grid = grid;
		this.particles = particles;
		
	}
	
	public void perform(int step) {
		for(DiagnosticMethod m : methods){
			m.calculate();
		}
	}
}
