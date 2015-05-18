package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;
import java.util.List;

import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.EmptyPoissonSolver;
import org.openpixi.pixi.physics.fields.FieldGenerators.IFieldGenerator;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.YangMillsSolver;
import org.openpixi.pixi.physics.grid.EmptyInterpolator;
import org.openpixi.pixi.physics.solver.relativistic.LeapFrogRelativistic;

/**
 * Generic settings class into which the YAML parser parses
 */
public class YamlSettings {
	public Double timeStep;
	public Double speedOfLight;
    public Integer numberOfDimensions;
    public Integer numberOfColors;
    public Integer numberOfThreads;
    public Double couplingConstant;
	public Double gridStep;
	public Double duration;
	public List<Integer> gridCells;
	public String poissonsolver;
	public List<YamlParticle> particles;
	public List<YamlParticleStream> streams;
    public YamlFields fields;
	public YamlOutput output;

	public void applyTo(Settings settings) {

		// Default settings:
		settings.setRelativistic(false);
		settings.setBoundary(GeneralBoundaryType.Periodic);
		settings.setGridSolver(new YangMillsSolver());
		settings.useGrid(true);
		settings.setInterpolator(new EmptyInterpolator());
        settings.setSpeedOfLight(1.0);
        settings.setNumberOfDimensions(3);
        settings.setNumberOfColors(1);
        settings.setCouplingConstant(1.0);
        settings.setParticleSolver(new LeapFrogRelativistic(settings.getNumberOfDimensions(), settings.getSpeedOfLight()));
        settings.setNumOfThreads(1);
        settings.setFieldGenerators(new ArrayList<IFieldGenerator>());

		// Custom settings:
		if (timeStep != null) {
			settings.setTimeStep(timeStep);
		}

		if (duration != null) {
			settings.setTMax(duration);
		}

        if(numberOfDimensions != null)
            settings.setNumberOfDimensions(numberOfDimensions);

		if (speedOfLight != null) {
            settings.setRelativistic(true);
			settings.setSpeedOfLight(speedOfLight);
            settings.setParticleSolver(new LeapFrogRelativistic(settings.getNumberOfDimensions(), speedOfLight));
		}

        if(numberOfColors != null)
            settings.setNumberOfColors(numberOfColors);

        if(couplingConstant != null)
            settings.setCouplingConstant(couplingConstant);

        if(numberOfThreads != null)
            settings.setNumOfThreads(numberOfThreads);

		if (gridStep != null) {
			settings.setGridStep(gridStep);
		}

		if (gridCells != null) {
			settings.setGridCellsX(gridCells.get(0));
			settings.setGridCellsY(gridCells.get(1));
			settings.setGridCellsZ(gridCells.get(2));
		}

		if (poissonsolver != null) {
			if (poissonsolver.equals("fft")) {
                System.out.println("Warning: FFT Poisson solver not yet implemented. Using EmptyPoissonSolver instead.");
				settings.setPoissonSolver(new EmptyPoissonSolver());
			} else if (poissonsolver.equals("empty")) {
				settings.setPoissonSolver(new EmptyPoissonSolver());
			} else {
				throw new RuntimeException("Unknown Poisson solver specified in YAML file.");
			}
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

        if(fields != null)
        {
            fields.applyTo(settings);
        }

		if (output != null) {
			output.applyTo(settings);
		}
	}
}
