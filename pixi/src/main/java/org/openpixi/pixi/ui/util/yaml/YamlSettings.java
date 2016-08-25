package org.openpixi.pixi.ui.util.yaml;

import java.util.HashMap;
import java.util.List;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.SimulationType;
import org.openpixi.pixi.physics.movement.solver.LeapFrogRelativisticParticleSolver;

/**
 * Generic settings class into which the YAML parser parses
 */
public class YamlSettings {
	public String simulationType;
	public Double timeStep;
	public Double speedOfLight;
    public Integer numberOfDimensions;
    public Integer numberOfColors;
    public Integer numberOfThreads;
    public Double couplingConstant;
	public Double gridStep;
	public List<Double> gridSteps;
	public Double duration;
	public List<Integer> gridCells;

	public YamlRegion evaluationRegion;
	public YamlRegion activeRegion;

	public List<YamlYangMillsParticle> particles;
	public List<YamlYangMillsParticleStream> streams;
    public YamlFields fields;
	public YamlCurrents currents;
	public YamlInitialConditions initialConditions;
	public YamlOutput output;
	public YamlPanels panels;

	@Deprecated
	public String fieldsolver;

	@Deprecated
	public String poissonsolver;

	public void applyTo(Settings settings) {

		// Default settings:
		settings.setRelativistic(true);
		settings.setSimulationType(SimulationType.TemporalYangMills);
		settings.useGrid(true);
        settings.setSpeedOfLight(1.0);
        settings.setNumberOfDimensions(3);
        settings.setNumberOfColors(1);
        settings.setCouplingConstant(1.0);
        settings.setNumOfThreads(4);

		// Custom settings:
		if(simulationType != null) {
			HashMap<String, SimulationType> map = new HashMap<String, SimulationType>();
			map.put("temporal yang-mills", SimulationType.TemporalYangMills);
			map.put("temporal cgc", SimulationType.TemporalCGC);
			map.put("temporal cgc ngp", SimulationType.TemporalCGCNGP);
			map.put("temporal optimized cgc ngp", SimulationType.TemporalOptimizedCGCNGP);
			map.put("boost-invariant cgc", SimulationType.BoostInvariantCGC);

			if(map.containsKey(simulationType)) {
				settings.setSimulationType(map.get(simulationType));
			} else {
				throw new RuntimeException("Unknown simulation type specified in YAML file.");
			}
		}

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
            settings.setParticleSolver(new LeapFrogRelativisticParticleSolver(settings.getNumberOfDimensions(), speedOfLight));
		}

        if(numberOfColors != null)
            settings.setNumberOfColors(numberOfColors);

        if(couplingConstant != null)
            settings.setCouplingConstant(couplingConstant);

        if(numberOfThreads != null)
            settings.setNumOfThreads(numberOfThreads);

		// Setting lattice spacing(s). Prioritize gridSteps over gridStep.
		if(gridSteps != null) {
			if(gridSteps.size() != numberOfDimensions) {
				throw new RuntimeException("Size of gridSteps does not match numberOfDimensions.");
			} else {
				for (int i = 0; i < gridCells.size(); i++) {
					settings.setGridStep(i, gridSteps.get(i));
				}
			}
		} else if (gridStep != null) {
			settings.setGridStep(gridStep);
		}

		if (gridCells != null) {
			if(gridCells.size() != numberOfDimensions) {
				throw new RuntimeException("Size of gridCells does not match numberOfDimensions.");
			} else {
				for (int i = 0; i < gridCells.size(); i++) {
					settings.setGridCells(i, gridCells.get(i));
				}
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

		if(initialConditions != null)
		{
			initialConditions.applyTo(settings);
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
