package org.openpixi.pixi.ui.panel.chart;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyAutomaticBestFit;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyTransformation;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterSimple;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.Box;

import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;
import org.openpixi.pixi.diagnostics.methods.PoyntingTheoremBuffer;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.BooleanArrayProperties;
import org.openpixi.pixi.ui.panel.properties.StringProperties;

/**
 * This panel shows various charts.
 */
public class Chart2DPanel extends AnimationChart2DPanel {

	ITrace2D[] traces;

	public final int INDEX_GAUSS_VIOLATION = 0;
	public final int INDEX_E_SQUARED = 1;
	public final int INDEX_B_SQUARED = 2;
	public final int INDEX_ENERGY_DENSITY = 3;
	public final int INDEX_PX = 4;
	public final int INDEX_PY = 5;
	public final int INDEX_PZ = 6;
	public final int INDEX_ENERGY_DENSITY_2 = 7;
	public final int INDEX_TOTAL_CHARGE = 8;
	public final int INDEX_TOTAL_CHARGE_SQUARED = 9;
	public final int INDEX_ENERGY_DENSITY_DERIVATIVE = 10;
	public final int INDEX_DIV_S = 11;
	public final int INDEX_JE = 12;
	public final int INDEX_POYNTING_THEOREM = 13;
	public final int INDEX_INTEGRATED_DIV_S = 14;
	public final int INDEX_INTEGRATED_JE = 15;
	public final int INDEX_INTEGRATED_POYNTING_THEOREM = 16;

	String[] chartLabel = new String[] {
			"Gauss law violation",
			"E squared",
			"B squared",
			"Energy density",
			"px",
			"py",
			"pz",
			"Energy density (occupation numbers)",
			"Total charge",
			"Total charge squared",
			"dE/dt",
			"div S",
			"J*E",
			"dE/dt + div S + J*E",
			"Integrated div S",
			"Integrated J*E",
			"Integrated dE/dt + div S + J*E"
	};

	Color[] traceColors = new Color[] {
			Color.red,
			Color.green,
			Color.blue,
			Color.black,
			Color.red,
			Color.green,
			Color.blue,
			Color.magenta,
			Color.darkGray,
			Color.darkGray,
			Color.orange,
			Color.cyan,
			Color.blue,
			Color.black,
			Color.red,
			Color.green,
			Color.pink
	};

	public BooleanProperties logarithmicProperty;
	public BooleanArrayProperties showChartsProperty;

	private boolean oldLogarithmicValue = false;

	private FieldMeasurements fieldMeasurements;

	private OccupationNumbersInTime occupationNumbers;

	private PoyntingTheoremBuffer poyntingTheorem;

	/** Constructor */
	public Chart2DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);

		traces = new ITrace2D[chartLabel.length];
		for (int i = 0; i < chartLabel.length; i++) {
			// TODO: Set buffer size according to simulation duration:
			traces[i] = new Trace2DLtd(simulationAnimation.getSimulation().getIterations());
			traces[i].setColor(traceColors[i]);
			traces[i].setName(chartLabel[i]);
			addTrace(traces[i]);
		}

		this.fieldMeasurements = new FieldMeasurements();

		logarithmicProperty = new BooleanProperties(simulationAnimation, "Logarithmic scale", false);

		showChartsProperty = new BooleanArrayProperties(simulationAnimation, chartLabel, new boolean[chartLabel.length]);

		occupationNumbers = new OccupationNumbersInTime(1.0, "none", "", false);
		// Linear scale
		AAxis<?> axisy = new AxisLinear<AxisScalePolicyAutomaticBestFit>(
				new LabelFormatterSimple(),
				new MyAxisScalePolicyAutomaticBestFit());
		setAxisYLeft(axisy, 0);
	}

	public void update() {
		this.fieldMeasurements = new FieldMeasurements();

		if (logarithmicProperty.getValue() != oldLogarithmicValue) {
			oldLogarithmicValue = logarithmicProperty.getValue();
			if (oldLogarithmicValue) {
				// Logarithmic scale
				AAxis<?> axisy = new MyAxisLog10<AxisScalePolicyTransformation>(
						new LabelFormatterNumber(new DecimalFormat("0.0E0")),
						new MyAxisScalePolicyTransformation());
				setAxisYLeft(axisy, 0);
			} else {
				// Linear scale
				AAxis<?> axisy = new AxisLinear<AxisScalePolicyAutomaticBestFit>(
						new LabelFormatterSimple(),
						new MyAxisScalePolicyAutomaticBestFit());
				setAxisYLeft(axisy, 0);
			}
		}

		Simulation s = getSimulationAnimation().getSimulation();
		double time = s.totalSimulationTime;
		poyntingTheorem = PoyntingTheoremBuffer.getOrAppendInstance(s);

		//TODO Make this method d-dimensional!!
		// The values computed from fieldMeasurements already come in "physical units", i.e. the factor g*a is accounted for.
		double[] esquares = new double[3];
		double[] bsquares = new double[3];
		for (int i = 0; i < 3; i++) {
			esquares[i] = fieldMeasurements.calculateEsquared(s.grid, i);
			bsquares[i] = fieldMeasurements.calculateBsquared(s.grid, i);
		}

		double eSquared = esquares[0] + esquares[1] + esquares[2];
		double bSquared = bsquares[0] + bsquares[1] + bsquares[2];
		double px = -esquares[0] + esquares[1] + esquares[2] - bsquares[0] + bsquares[1] + bsquares[2];
		double py = +esquares[0] - esquares[1] + esquares[2] + bsquares[0] - bsquares[1] + bsquares[2];
		double pz = +esquares[0] + esquares[1] - esquares[2] + bsquares[0] + bsquares[1] - bsquares[2];
		double energyDensity = (eSquared + bSquared) / 2;

		// The value computed for the Gauss constraint violation and the total charge is given in physical units as well.
		double gaussViolation = fieldMeasurements.calculateGaussConstraint(s.grid);
		double totalCharge = fieldMeasurements.calculateTotalCharge(s.grid);
		double totalChargeSquared = fieldMeasurements.calculateTotalChargeSquared(s.grid);

		double energyDensityDerivative = poyntingTheorem.getTotalEnergyDensityDerivative();
		double divS = poyntingTheorem.getTotalDivS();
		double jS = poyntingTheorem.getTotalJE();
		double poyntingTheoremSum = energyDensityDerivative + divS + jS;
		double integratedDivS = poyntingTheorem.getIntegratedTotalDivS();
		double integratedJS = poyntingTheorem.getIntegratedTotalJE();
		double integratedPoyntingTheorem = poyntingTheorem.getTotalEnergyDensity()
				+ integratedDivS + integratedJS;

		traces[INDEX_E_SQUARED].addPoint(time, eSquared);
		traces[INDEX_B_SQUARED].addPoint(time, bSquared);
		traces[INDEX_GAUSS_VIOLATION].addPoint(time, gaussViolation);
		traces[INDEX_ENERGY_DENSITY].addPoint(time, energyDensity);
		traces[INDEX_PX].addPoint(time, px);
		traces[INDEX_PY].addPoint(time, py);
		traces[INDEX_PZ].addPoint(time, pz);
		traces[INDEX_TOTAL_CHARGE].addPoint(time, totalCharge);
		traces[INDEX_TOTAL_CHARGE_SQUARED].addPoint(time, totalChargeSquared);
		traces[INDEX_ENERGY_DENSITY_DERIVATIVE].addPoint(time, energyDensityDerivative);
		traces[INDEX_DIV_S].addPoint(time, divS);
		traces[INDEX_JE].addPoint(time, jS);
		traces[INDEX_POYNTING_THEOREM].addPoint(time, poyntingTheoremSum);
		traces[INDEX_INTEGRATED_DIV_S].addPoint(time, integratedDivS);
		traces[INDEX_INTEGRATED_JE].addPoint(time, integratedJS);
		traces[INDEX_INTEGRATED_POYNTING_THEOREM].addPoint(time, integratedPoyntingTheorem);

		if (showChartsProperty.getValue(INDEX_ENERGY_DENSITY_2)) {
			occupationNumbers.initialize(s);
			occupationNumbers.calculate(s.grid, s.particles, 0);
			traces[INDEX_ENERGY_DENSITY_2].addPoint(time, occupationNumbers.energyDensity);
		}

		for (int i = 0; i < showChartsProperty.getSize(); i++) {
			traces[i].setVisible(showChartsProperty.getValue(i));
		}


	}

	public void clear() {
		for (int i = 0; i < showChartsProperty.getSize(); i++) {
			traces[i].removeAllPoints();
			((Trace2DLtd) traces[i]).setMaxSize(simulationAnimation.getSimulation().getIterations());
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Chart panel");
		logarithmicProperty.addComponents(box);
		showChartsProperty.addComponents(box);
	}
}