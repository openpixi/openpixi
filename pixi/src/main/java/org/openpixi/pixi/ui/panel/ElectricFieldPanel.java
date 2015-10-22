package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.Box;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanArrayProperties;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.ColorProperties;
import org.openpixi.pixi.ui.panel.properties.CoordinateProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;

/**
 * This panel shows the one-dimensional electric field along the x-direction.
 * Several field lines for various grid positions along the y-direction are
 * superimposed.
 */
public class ElectricFieldPanel extends AnimationPanel {

	public ColorProperties colorProperties;
	public ScaleProperties scaleProperties;
	public BooleanProperties gaugeProperties;
	public BooleanArrayProperties showFieldProperties;
	public CoordinateProperties showCoordinateProperties;

	public final int INDEX_E = 0;
	public final int INDEX_U = 1;
	public final int INDEX_U_NEXT = 2;
	public final int INDEX_U0 = 3;
	public final int INDEX_U0_NEXT = 4;
	public final int INDEX_J = 5;
	public final int INDEX_RHO = 6;
	public final int INDEX_GAUSS = 7;

	String[] fieldLabel = new String[] {
			"E",
			"U",
			"U next",
			"U0",
			"U0 next",
			"j",
			"rho",
			"Gauss"
	};

	Color[] fieldColors = new Color[] {
			Color.black,
			Color.green,
			Color.gray,
			Color.green,
			Color.gray,
			Color.red,
			Color.blue,
			Color.magenta
	};

	boolean[] fieldInit = new boolean[] {
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false
	};

	/** Constructor */
	public ElectricFieldPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		colorProperties = new ColorProperties(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);
		gaugeProperties = new BooleanProperties(simulationAnimation, "Coulomb gauge", false);
		showFieldProperties = new BooleanArrayProperties(simulationAnimation, fieldLabel, fieldInit);
		showCoordinateProperties = new CoordinateProperties(simulationAnimation, CoordinateProperties.Mode.MODE_1D_LOOP);
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);

		/*
			Note:
			These two commands set the origin of the coordinate system to the lower-left corner of the panel and flip
			the y-axis. The system is now as follows:
			Let (x,y0 denote a point on the panel. (0,0) is the lower-left corner. The x-component increases to the
			right. The y-component increases downwards.
		 */
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);

		colorProperties.checkConsistency();

		Simulation s = getSimulationAnimation().getSimulation();
		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth() / 2.0;

		double panelWidth = getWidth();
		double panelHeight = getHeight();

		boolean useCoulombGauge = gaugeProperties.getValue();
		//Grid drawGrid = s.grid;
		Grid drawGrid = new MirroredGrid(s.grid, 0);
		if (useCoulombGauge) {
			CoulombGauge coulombGauge = new CoulombGauge(s.grid);
			Grid gridCopy = new Grid(s.grid);
			coulombGauge.applyGaugeTransformation(gridCopy);
			drawGrid = gridCopy;

//			// Test random gauge:
//			RandomGauge randomGauge = new RandomGauge(s.grid);
//			randomGauge.setRandomVector(new double[] {0, 1, 0});
//			Grid gridCopy = new Grid(s.grid);
//			randomGauge.applyGaugeTransformation(gridCopy);
//			drawGrid = gridCopy;
		}

		// TODO: display particles according to showCoordinateProperties (see below)

		// Draw particles on a central line:
		for (int i = 0; i < s.particles.size(); i++) {
			IParticle par = s.particles.get(i);
			graph.setColor(par.getColor());
			double radius = par.getRadius();
			int width = (int) (2*sx*radius);
			int height = (int) (2*sx*radius);
			if(width > 2 && height > 2) {
				graph.fillOval((int) (par.getPosition(0)*sx) - width/2, (int) (panelHeight/2 - height/2),  width,  height);
			}
			else {
				graph.drawRect((int) (par.getPosition(0)*sx), (int) panelHeight/2, 0, 0);
			}
		}

		// Scale factor
		double scale = scaleProperties.getScale();

		scaleProperties.resetAutomaticScale();

		for (int i = 0; i < fieldLabel.length; i++) {
			if (showFieldProperties.getValue(i)) {
				drawGraph(graph, s, panelWidth, panelHeight, drawGrid, scale, i);
			}
		}

		scaleProperties.calculateAutomaticScale(0.5);

	}

	private void drawGraph(Graphics2D graph, Simulation s, double panelWidth,
			double panelHeight, Grid drawGrid, double scale, int type) {

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double at = s.getTimeStep();
		double g = s.getCouplingConstant();

		int colorIndex = colorProperties.getColorIndex();
		int directionIndex = colorProperties.getDirectionIndex();

		graph.setColor(fieldColors[type]);

		int kmin = 0;
		int kmax = 1;
		int abscissaIndex = showCoordinateProperties.getXAxisIndex();
		int loopIndex = showCoordinateProperties.getLoopIndex();
		int pos[] = showCoordinateProperties.getPositions();

		if (loopIndex != -1) {
			// Show all lines
			kmin = 0;
			kmax = drawGrid.getNumCells(loopIndex);
		}
		double sx = panelWidth / s.getSimulationBoxSize(abscissaIndex) / 2;
		for(int k = kmin; k < kmax; k++)
		{
			int newPosition = 0;
			int newValue = 0;
			for(int i = 0; i < drawGrid.getNumCells(abscissaIndex); i++)
			{

				int oldPosition = newPosition;
				int oldValue = newValue;
				pos[abscissaIndex] = i;
				if (loopIndex != -1) {
					pos[loopIndex] = k;
				}

				// Electric fields are placed at the lattice points.
				newPosition = (int) (s.grid.getLatticeSpacing() * (i) * sx);

				/*
					Expectation: Positive fields should point upwards.
					In the flipped and translated coordinate system defined above we have to add the fields to the
					center of the panel in order to get the expected result.
				*/
				double value = 0;
				switch(type) {
				case INDEX_E:
					value = drawGrid.getE(s.grid.getCellIndex(pos), directionIndex).get(colorIndex) / (as * g);
					break;
				case INDEX_U:
					value = drawGrid.getU(s.grid.getCellIndex(pos), directionIndex).proj().get(colorIndex) / (as * g);
					break;
				case INDEX_U_NEXT:
					value = drawGrid.getUnext(s.grid.getCellIndex(pos), directionIndex).proj().get(colorIndex) / (as * g);
					break;
				case INDEX_U0:
					value = drawGrid.getU0(s.grid.getCellIndex(pos)).proj().get(colorIndex) / (at * g);
					break;
				case INDEX_U0_NEXT:
					value = drawGrid.getU0next(s.grid.getCellIndex(pos)).proj().get(colorIndex) / (at * g);
					break;
				case INDEX_J:
					newPosition = (int) (s.grid.getLatticeSpacing() * (i + .5) * sx);
					value = drawGrid.getJ(s.grid.getCellIndex(pos), directionIndex).get(colorIndex) / (as * g);
					break;
				case INDEX_RHO:
					newPosition = (int) (s.grid.getLatticeSpacing() * (i + .5) * sx);
					value = drawGrid.getRho(s.grid.getCellIndex(pos)).get(colorIndex) / (as * g);
					break;
				case INDEX_GAUSS:
					newPosition = (int) (s.grid.getLatticeSpacing() * (i + .5) * sx);
					value = drawGrid.getGaussConstraint(s.grid.getCellIndex(pos)).get(colorIndex) / (as * g);
					break;
				}
				scaleProperties.putValue(value);
				newValue = (int) (((0.5 + scale * value) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Electric field panel");
		showFieldProperties.addComponents(box);
		colorProperties.addComponents(box);
		showCoordinateProperties.addComponents(box);
		scaleProperties.addComponents(box);
		gaugeProperties.addComponents(box);
	}

	private class MirroredGrid extends Grid {
		public MirroredGrid(Grid grid, int mirroredDirection) {
			super(grid);
			this.numCells[mirroredDirection] *= 2;
			this.cellIterator = null;
			createGrid();

			// Copy and mirror cells.
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				int[] cellPos = grid.getCellPos(i);
				int newGridIndex = this.getCellIndex(cellPos);

				int[] newMirroredGridPos = cellPos.clone();
				newMirroredGridPos[mirroredDirection] = numCells[mirroredDirection] - cellPos[mirroredDirection] - 1;
				int mirroredIndex = this.getCellIndex(newMirroredGridPos);

				cells[newGridIndex] = grid.getCell(i).copy();
				cells[mirroredIndex] = grid.getCell(i).copy();
				/*
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					AlgebraElement E = grid.getCell(i).getE(j).copy().mult(-1.0);
					cells[mirroredIndex].setE(j, E);
				}
				*/
			}

		}
	}
}
