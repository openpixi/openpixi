package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.ui.util.yaml.currentgenerators.*;

import java.util.ArrayList;

public class YamlCurrents {

    /**
     * List of current generators.
     */
    public ArrayList<YamlSU2WireCurrent> SU2WireCurrent = new ArrayList<YamlSU2WireCurrent>();

    public ArrayList<YamlSU2DeltaPulseCurrent> SU2DeltaPulseCurrent = new ArrayList<YamlSU2DeltaPulseCurrent>();

    public ArrayList<YamlSU2LightConeDeltaPulseCurrent> SU2LightConeDeltaPulseCurrent = new ArrayList<YamlSU2LightConeDeltaPulseCurrent>();

    public ArrayList<YamlSU2LightConeGaussPulseCurrent> SU2LightConeGaussPulseCurrent = new ArrayList<YamlSU2LightConeGaussPulseCurrent>();

    public ArrayList<YamlSU2LorenzLightConeGaussPulseCurrent> SU2LorenzLightConeGaussPulseCurrent = new ArrayList<YamlSU2LorenzLightConeGaussPulseCurrent>();

    public ArrayList<YamlNewLCCurrent> newLCCurrents = new ArrayList<YamlNewLCCurrent>();

	public ArrayList<YamlPointChargeLCCurrent> pointChargeLCCurrents = new ArrayList<YamlPointChargeLCCurrent>();

    public ArrayList<YamlNewLorenzLCCurrent> newLorenzLCCurrents = new ArrayList<YamlNewLorenzLCCurrent>();

    public ArrayList<YamlRandomLorenzColorCurrent> randomLorenzColorCurrents = new ArrayList<YamlRandomLorenzColorCurrent>();

    public ArrayList<YamlRandomTemporalColorCurrent> randomTemporalColorCurrents = new ArrayList<YamlRandomTemporalColorCurrent>();

    public ArrayList<YamlRandomTemporalColorCurrentProton> randomTemporalColorCurrentsProton = new ArrayList<YamlRandomTemporalColorCurrentProton>();

	public ArrayList<YamlRandomTemporalParticleColorCurrent> randomTemporalParticleColorCurrents = new ArrayList<YamlRandomTemporalParticleColorCurrent>();

    public ArrayList<YamlRandomTemporalParticleColorCurrentSphericalProton> randomTemporalParticleColorCurrentsSphericalProton = new ArrayList<YamlRandomTemporalParticleColorCurrentSphericalProton>();

    public ArrayList<YamlRandomTemporalParticleColorCurrentConstituentProton> randomTemporalParticleColorCurrentsConstituentProton = new ArrayList<YamlRandomTemporalParticleColorCurrentConstituentProton>();

    public ArrayList<YamlRandomTemporalParticleColorCurrentNucleus> randomTemporalParticleColorCurrentsNucleus = new ArrayList<YamlRandomTemporalParticleColorCurrentNucleus>();

	public ArrayList<YamlMVModel> MVModels = new ArrayList<YamlMVModel>();

	public ArrayList<YamlDualMVModel> dualMVModels = new ArrayList<YamlDualMVModel>();



	/**
     * Creates CurrentGenerator instances and applies them to the Settings instance.
     * @param s
     */
    public void applyTo(Settings s) {
        for (YamlSU2WireCurrent wire : SU2WireCurrent) {
            if (wire.checkConsistency(s)) {
                s.addCurrentGenerator(wire.getCurrentGenerator());
            }
        }

        for (YamlSU2DeltaPulseCurrent pulse : SU2DeltaPulseCurrent) {
            if (pulse.checkConsistency(s)) {
                s.addCurrentGenerator(pulse.getCurrentGenerator());
            }
        }

        for (YamlSU2LightConeDeltaPulseCurrent lightcone : SU2LightConeDeltaPulseCurrent) {
            if (lightcone.checkConsistency(s)) {
                s.addCurrentGenerator(lightcone.getCurrentGenerator());
            }
        }

        for (YamlSU2LightConeGaussPulseCurrent gauss : SU2LightConeGaussPulseCurrent) {
            if (gauss.checkConsistency(s)) {
                s.addCurrentGenerator(gauss.getCurrentGenerator());
            }
        }

        for (YamlSU2LorenzLightConeGaussPulseCurrent lorenz : SU2LorenzLightConeGaussPulseCurrent) {
            if (lorenz.checkConsistency(s)) {
                s.addCurrentGenerator(lorenz.getCurrentGenerator());
            }
        }

		for (YamlNewLCCurrent current : newLCCurrents) {
			if (current.checkConsistency(s)) {
				s.addCurrentGenerator(current.getCurrentGenerator());
			}
		}

		for (YamlPointChargeLCCurrent current : pointChargeLCCurrents) {
			if (current.checkConsistency(s)) {
				s.addCurrentGenerator(current.getCurrentGenerator());
			}
		}

		for (YamlNewLorenzLCCurrent current : newLorenzLCCurrents) {
			if (current.checkConsistency(s)) {
				s.addCurrentGenerator(current.getCurrentGenerator());
			}
		}

        for (YamlRandomLorenzColorCurrent current : randomLorenzColorCurrents) {
            s.addCurrentGenerator(current.getCurrentGenerator());
        }

		for (YamlRandomTemporalColorCurrent current : randomTemporalColorCurrents) {
			s.addCurrentGenerator(current.getCurrentGenerator());
		}

        for (YamlRandomTemporalColorCurrentProton current : randomTemporalColorCurrentsProton) {
            s.addCurrentGenerator(current.getCurrentGenerator());
        }

		for (YamlRandomTemporalParticleColorCurrent current : randomTemporalParticleColorCurrents) {
			s.addCurrentGenerator(current.getCurrentGenerator());
		}

        for (YamlRandomTemporalParticleColorCurrentSphericalProton current : randomTemporalParticleColorCurrentsSphericalProton) {
            s.addCurrentGenerator(current.getCurrentGenerator());
        }

        for (YamlRandomTemporalParticleColorCurrentConstituentProton current : randomTemporalParticleColorCurrentsConstituentProton) {
            s.addCurrentGenerator(current.getCurrentGenerator());
        }

        for (YamlRandomTemporalParticleColorCurrentNucleus current : randomTemporalParticleColorCurrentsNucleus) {
            s.addCurrentGenerator(current.getCurrentGenerator(s));
        }

		for(YamlMVModel current : MVModels) {
			s.addCurrentGenerator(current.getCurrentGenerator());
		}

		for(YamlDualMVModel current : dualMVModels) {
			s.addCurrentGenerator(current.getCurrentGenerator());
		}
	}
}
