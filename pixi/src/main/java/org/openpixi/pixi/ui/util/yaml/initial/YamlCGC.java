package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.initial.CGC.*;
import org.openpixi.pixi.physics.initial.IInitialCondition;
import org.openpixi.pixi.ui.util.yaml.fieldgenerators.YamlSU2PlaneWave;

import java.util.ArrayList;

public class YamlCGC {

	public String poissonSolver;
	public ArrayList<YamlMVModel> MVModel = new ArrayList<YamlMVModel>();
	public ArrayList<YamlMVModelCoherent> MVModelCoherent = new ArrayList<YamlMVModelCoherent>();


	/**
	 * Creates IInitialCondition instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		for (YamlMVModel init : MVModel) {
			IInitialCondition ic = init.getInitialCondition();
			((CGCInitialCondition) ic).setPoissonSolver(getPoissonSolver(poissonSolver));
			s.addInitialConditions(ic);
		}

		for (YamlMVModelCoherent init : MVModelCoherent) {
			IInitialCondition ic = init.getInitialCondition();
			((CGCInitialCondition) ic).setPoissonSolver(getPoissonSolver(poissonSolver));
			s.addInitialConditions(ic);
		}
	}

	private ICGCPoissonSolver getPoissonSolver(String name) {
		if(name != null) {
			switch (name.toLowerCase().trim()) {
				case "regular":
					return new LightConePoissonSolver();
				case "improved":
					return new LightConePoissonSolverImproved();
				case "improved full":
					return new LightConePoissonSolverImprovedFull();
				case "refined":
					return new LightConePoissonSolverRefined();
				default:
					System.out.println("YamlCGC: Unknown Poisson solver. Using improved as default.");
					return new LightConePoissonSolverImproved();
			}
		} else {
			System.out.println("YamlCGC: Please specify Poisson solver. Using improved as default.");
			return new LightConePoissonSolverImproved();
		}

	}
}
