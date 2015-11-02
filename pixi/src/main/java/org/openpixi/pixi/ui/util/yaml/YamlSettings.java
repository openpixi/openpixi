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

	public Boolean restrictedRegionEnabled;
	public ArrayList<Integer> regionPoint1;
	public ArrayList<Integer> regionPoint2;

	public List<YamlParticle> particles;
	public List<YamlParticleStream> streams;
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

		// Restricted region
		if(regionPoint1 != null && regionPoint2 != null) {
			// Check length of vectors.
			if(regionPoint1.size() == numberOfDimensions && regionPoint2.size() == numberOfDimensions) {
				settings.setRestrictedRegionEnabled(true);

				// Convert from List<Integer> to int[].
				int[] point1 = new int[numberOfDimensions];
				int[] point2 = new int[numberOfDimensions];

				for (int i = 0; i < numberOfDimensions; i++) {
					point1[i] = regionPoint1.get(i);
					point2[i] = regionPoint2.get(i);
				}

				settings.setRegionPoint1(point1);
				settings.setRegionPoint2(point2);
			} else {
				System.out.println("Please check length of regionPoint1 and regionPoint2.");
				settings.setRestrictedRegionEnabled(false);
			}
		} else {
			if(restrictedRegionEnabled != null) {
				if(restrictedRegionEnabled) {
					System.out.println("Please define region points for restricted region.");
					settings.setRestrictedRegionEnabled(false);
				}
			}
		}

		if(restrictedRegionEnabled != null) {
			// If true then region points must be defined. This is taken care of in the code above.
			// If false then override restricted region settings.
			if(restrictedRegionEnabled == false) {
				settings.setRestrictedRegionEnabled(false);
			}
		}
	}
}
