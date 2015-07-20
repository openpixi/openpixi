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

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.BooleanArrayProperties;

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

	String[] chartLabel = new String[] {
			"Gauss law violation",
			"E squared",
			"B squared",
			"Energy density",
			"px",
			"py",
			"pz"
	};

	Color[] traceColors = new Color[] {
			Color.red,
			Color.green,
			Color.blue,
			Color.black,
			Color.red,
			Color.green,
			Color.blue
	};

	public BooleanProperties logarithmicProperty;
	public BooleanArrayProperties showChartsProperty;

	private boolean oldLogarithmicValue = false;

	private FieldMeasurements fieldMeasurements;

	/** Constructor */
	public Chart2DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);

		traces = new ITrace2D[chartLabel.length];
		for (int i = 0; i < chartLabel.length; i++) {
			// TODO: Set buffer size according to simulation duration:
			traces[i] = new Trace2DLtd(2000);
			traces[i].setColor(traceColors[i]);
			traces[i].setName(chartLabel[i]);
			addTrace(traces[i]);
		}

		this.fieldMeasurements = new FieldMeasurements();

		logarithmicProperty = new BooleanProperties("Logarithmic scale", false);

		showChartsProperty = new BooleanArrayProperties(chartLabel, new boolean[chartLabel.length]);

		// Linear scale
		AAxis<?> axisy = new AxisLinear<AxisScalePolicyAutomaticBestFit>(
				new LabelFormatterSimple(),
				new MyAxisScalePolicyAutomaticBestFit());
		setAxisYLeft(axisy, 0);
	}

	public void update() {
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

		//TODO Make this method d-dimensional!!
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

		double gaussViolation = fieldMeasurements.calculateGaussConstraint(s.grid);

		traces[INDEX_E_SQUARED].addPoint(time, eSquared);
		traces[INDEX_B_SQUARED].addPoint(time, bSquared);
		traces[INDEX_GAUSS_VIOLATION].addPoint(time, gaussViolation);
		traces[INDEX_ENERGY_DENSITY].addPoint(time, energyDensity);
		traces[INDEX_PX].addPoint(time, px);
		traces[INDEX_PX].addPoint(time, py);
		traces[INDEX_PX].addPoint(time, pz);

		for (int i = 0; i < showChartsProperty.getSize(); i++) {
			traces[i].setVisible(showChartsProperty.getValue(i));
		}
	}

	public void clear() {
		for (int i = 0; i < showChartsProperty.getSize(); i++) {
			traces[i].removeAllPoints();
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Chart panel");
		logarithmicProperty.addComponents(box);
		showChartsProperty.addComponents(box);
	}
}