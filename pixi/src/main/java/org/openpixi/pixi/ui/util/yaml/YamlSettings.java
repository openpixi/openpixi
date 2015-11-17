package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;
import java.util.List;

import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.EmptyPoissonSolver;
import org.openpixi.pixi.physics.fields.LorenzYangMillsSolver;
import org.openpixi.pixi.physics.fields.fieldgenerators.IFieldGenerator;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.physics.fields.TemporalYangMillsSolver;
import org.openpixi.pixi.physics.grid.EmptyInterpolator;
import org.openpixi.pixi.physics.solver.relativistic.LeapFrogRelativistic;
import org.openpixi.pixi.diagnostics.Diagnostics;

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
	public String fieldsolver;
	public String poissonsolver;

	public YamlRegion evaluationRegion;
	public YamlRegion activeRegion;

	public List<YamlYangMillsParticle> particles;
	public List<YamlYangMillsParticleStream> streams;
    public YamlFields fields;
	public YamlCurrents currents;
	public YamlOutput output;
	public YamlPanels panels;

	public void applyTo(Settings settings) {

		// Default settings:
		settings.setRelativistic(false);
		settings.setBoundary(GeneralBoundaryType.Periodic);
		settings.setFieldSolver(new TemporalYangMillsSolver());//settings.setFieldSolver(new YangMillsSolver());
		settings.useGrid(true);
		settings.setInterpolator(new EmptyInterpolator());
        settings.setSpeedOfLight(1.0);
        settings.setNumberOfDimensions(3);
        settings.setNumberOfColors(1);
        settings.setCouplingConstant(1.0);
        settings.setParticleSolver(new LeapFrogRelativistic(settings.getNumberOfDimensions(), settings.getSpeedOfLight()));
        settings.setNumOfThreads(4);
        settings.setFieldGenerators(new ArrayList<IFieldGenerator>());
		settings.setCurrentGenerators(new ArrayList<ICurrentGenerator>());
        settings.setDiagnostics(new ArrayList<Diagnostics>());

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

		if(fieldsolver != null) {
			if(fieldsolver.equals("temporal yang mills")) {
				settings.setFieldSolver(new TemporalYangMillsSolver());
			} else if(fieldsolver.equals("lorenz yang mills")) {
				settings.setFieldSolver(new LorenzYangMillsSolver());
			}
		}

		if (particles != null) {
			for (YamlYangMillsParticle p : particles) {
				p.applyTo(settings);
			}
		}

		if (streams != null) {
			for (YamlYangMillsParticleStream s : streams) {
				s.applyTo(settings);
			}
		}

        if(fields != null)
        {
            fields.applyTo(settings);
        }

		if(currents != null)
		{
			currents.applyTo(settings);
		}

		if (output != null) {
			output.applyTo(settings);
		}

		if (panels != null) {
			settings.setYamlPanels(panels);
		}

		// Evaluation region (used for energy density, Gauss law, ... )
		if(evaluationRegion != null) {
			if(evaluationRegion.checkPoints(numberOfDimensions)) {
				if(evaluationRegion.enabled != null) {
					settings.setEvaluationRegionEnabled(evaluationRegion.enabled);
				}
				settings.setEvaluationRegionPoint1(evaluationRegion.getPoint1(gridCells));
				settings.setEvaluationRegionPoint2(evaluationRegion.getPoint2(gridCells));
			} else {
				System.out.println("Evaluation region: check region points.");
			}
		}

		// Active region (used for equations of motion)
		if(activeRegion != null) {
			if(activeRegion.checkPoints(numberOfDimensions)) {
				if(activeRegion.enabled != null) {
					settings.setActiveRegionEnabled(activeRegion.enabled);
				}
				settings.setActiveRegionPoint1(activeRegion.getPoint1(gridCells));
				settings.setActiveRegionPoint2(activeRegion.getPoint2(gridCells));
			} else {
				System.out.println("Active region: check region points.");
			}
		}
	}
}
