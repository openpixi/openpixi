package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.initial.CGC.*;
import org.openpixi.pixi.physics.initial.IInitialCondition;

import java.util.ArrayList;

public class YamlCGC {

	public String poissonSolver;
	public Boolean computeTadpole = false;
	public Boolean computeDipole = false;
	public String tadpoleFilename = "tadpole.txt";
	public String dipoleFilename = "dipole.txt";

	public ArrayList<YamlMVModel> MVModel = new ArrayList<YamlMVModel>();
	public ArrayList<YamlMVModelCoherent> MVModelCoherent = new ArrayList<YamlMVModelCoherent>();
	public ArrayList<YamlNucleusCoherent> NucleusCoherent = new ArrayList<YamlNucleusCoherent>();
	public ArrayList<YamlNucleus> Nucleus = new ArrayList<YamlNucleus>();


	/**
	 * Creates IInitialCondition instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		for (YamlMVModel init : MVModel) {
			CGCInitialCondition ic = init.getInitialCondition();
			applyOptions(ic);
			s.addInitialConditions(ic);
		}

		for (YamlMVModelCoherent init : MVModelCoherent) {
			CGCInitialCondition ic = init.getInitialCondition();
			applyOptions(ic);
			s.addInitialConditions(ic);
		}

		for (YamlNucleusCoherent init : NucleusCoherent) {
			CGCInitialCondition ic = init.getInitialCondition();
			applyOptions(ic);
			s.addInitialConditions(ic);
		}

		for (YamlNucleus init : Nucleus) {
			CGCInitialCondition ic = init.getInitialCondition();
			applyOptions(ic);
			s.addInitialConditions(ic);
		}
	}

	private void applyOptions(CGCInitialCondition ic) {
		ic.setPoissonSolver(getPoissonSolver(poissonSolver));
		ic.computeTadpole = computeTadpole;
		ic.computeDipole = computeDipole;
		ic.tadpoleFilename = tadpoleFilename;
		ic.dipoleFilename = dipoleFilename;
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
