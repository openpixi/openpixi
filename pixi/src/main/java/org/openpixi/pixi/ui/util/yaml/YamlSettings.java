package org.openpixi.pixi.ui.util.yaml;

import java.util.List;

import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.grid.ChargeConservingCIC;

/**
 * Generic settings class into which the YAML parser parses
 */
public class YamlSettings {
	public Double timeStep;
	public Double speedOfLight;
	public Double gridStep;
	public Integer gridCellsX;
	public Integer gridCellsY;
	public List<YamlParticle> particles;
	public List<YamlParticleStream> streams;
	
	public void applyTo(Settings settings) {

		// Default settings:
		settings.setRelativistic(true);
		settings.setBoundary(GeneralBoundaryType.Periodic);
		settings.setGridSolver(new SimpleSolver());
		settings.setPoissonSolver(new PoissonSolverFFTPeriodic());
		settings.useGrid(true);
		settings.setInterpolator(new ChargeConservingCIC());

		// Custom settings:
		if (timeStep != null) {
			settings.setTimeStep(timeStep);
		}

		if (speedOfLight != null) {
			settings.setSpeedOfLight(speedOfLight);
		}

		if (gridStep != null) {
			settings.setGridStep(gridStep);
		}

		if (gridCellsX != null) {
			settings.setGridCellsX(gridCellsX);
		}

		if (gridCellsY != null) {
			settings.setGridCellsY(gridCellsY);
		}

		if (particles != null) {
			for (YamlParticle p : particles) {
				p.applyTo(settings);
			}
		}

		if (streams != null) {
			for (YamlParticleStream s : streams) {
				s.applyTo(settings);
			}
		}
	}
}
