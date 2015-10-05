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

	String[] chartLabel = new String[] {
			"Gauss law violation",
			"E squared",
			"B squared",
			"Energy density",
			"px",
			"py",
			"pz",
			"Energy density (occupation numbers)",
			"Total charge"
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
			Color.darkGray
	};

	public BooleanProperties logarithmicProperty;
	public BooleanArrayProperties showChartsProperty;

	private boolean oldLogarithmicValue = false;

	private FieldMeasurements fieldMeasurements;

	private OccupationNumbersInTime occupationNumbers;

	public BooleanProperties useRestrictedRegionProperty;
	public RegionProperty regionPropery;
	private boolean[] oldRestrictedRegion;
	private boolean oldUseRestrictedRegionProperty;

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

		this.oldRestrictedRegion = new boolean[simulationAnimation.getSimulation().grid.getTotalNumberOfCells()];
		this.useRestrictedRegionProperty = new BooleanProperties(simulationAnimation, "Use restricted region", false);
		this.regionPropery = new RegionProperty(simulationAnimation, "Region", "");
		oldUseRestrictedRegionProperty = this.useRestrictedRegionProperty.getValue();
	}

	public void update() {
		// Update restricted region if settings have changed.
		if (useRestrictedRegionProperty.getValue() != oldUseRestrictedRegionProperty || oldRestrictedRegion != regionPropery.restrictedRegion) {
			oldUseRestrictedRegionProperty = useRestrictedRegionProperty.getValue();
			oldRestrictedRegion = regionPropery.restrictedRegion;
			this.fieldMeasurements = new FieldMeasurements(regionPropery.restrictedRegion);

		}

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

		traces[INDEX_E_SQUARED].addPoint(time, eSquared);
		traces[INDEX_B_SQUARED].addPoint(time, bSquared);
		traces[INDEX_GAUSS_VIOLATION].addPoint(time, gaussViolation);
		traces[INDEX_ENERGY_DENSITY].addPoint(time, energyDensity);
		traces[INDEX_PX].addPoint(time, px);
		traces[INDEX_PY].addPoint(time, py);
		traces[INDEX_PZ].addPoint(time, pz);
		traces[INDEX_TOTAL_CHARGE].addPoint(time, totalCharge);

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
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Chart panel");
		logarithmicProperty.addComponents(box);
		showChartsProperty.addComponents(box);
		useRestrictedRegionProperty.addComponents(box);
		regionPropery.addComponents(box);
	}

	public class RegionProperty extends StringProperties {

		private Simulation s;

		public int[] latticeCoordinate0;
		public int[] latticeCoordinate1;

		public boolean[] restrictedRegion;

		public RegionProperty(SimulationAnimation simulationAnimation, String name, String initialValue) {
			super(simulationAnimation, name, initialValue);
			this.s = simulationAnimation.getSimulation();
			this.restrictedRegion = new boolean[s.grid.getTotalNumberOfCells()];
		}

		@Override
		public void update() {
			// Format "[x0,y0,z0]-[x1,y1,z1]"
			String[] splitString = getValue().trim().split("-");    // remove all whitespaces and split at the '-' symbol.
			if (splitString.length == 2) {
				// remove square brackets and split at ','
				String[] stringCoord0 = splitString[0].replace("[", "").replace("]", "").split(",");
				String[] stringCoord1 = splitString[1].replace("[", "").replace("]", "").split(",");

				if (stringCoord0.length == s.getNumberOfDimensions() && stringCoord1.length == s.getNumberOfDimensions()) {
					latticeCoordinate0 = new int[s.getNumberOfDimensions()];
					latticeCoordinate1 = new int[s.getNumberOfDimensions()];

					for (int i = 0; i < stringCoord0.length; i++) {
						latticeCoordinate0[i] = Integer.parseInt(stringCoord0[i]);
					}

					for (int i = 0; i < stringCoord1.length; i++) {
						latticeCoordinate1[i] = Integer.parseInt(stringCoord1[i]);
					}

					// Compute restricted region.
					this.restrictedRegion = new boolean[s.grid.getTotalNumberOfCells()];

					for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
						int[] currentCoordinate = s.grid.getCellPos(i);
						for (int j = 0; j < s.getNumberOfDimensions(); j++) {
							if (latticeCoordinate0[j] > currentCoordinate[j] || currentCoordinate[j] > latticeCoordinate1[j]) {
								this.restrictedRegion[i] = true;
								break;
							}
						}
					}

				} else {
					System.out.println("Chart2DPanel: Coordinate parsing failed. Check size of the vectors.");
				}
			}

		}
	}
}