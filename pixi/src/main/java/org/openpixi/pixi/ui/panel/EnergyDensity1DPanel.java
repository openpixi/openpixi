package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Box;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ColorProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;

/**
 * This panel shows the energy density along the x-direction.
 * Several energy density lines for various grid positions along the y-direction are
 * superimposed.
 */
public class EnergyDensity1DPanel extends AnimationPanel {
	
	ScaleProperties scaleProperties = new ScaleProperties();

	/** Constructor */
	public EnergyDensity1DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
	}

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

		Simulation s = getSimulationAnimation().getSimulation();
		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();

		double panelHeight = getHeight();

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Draw energy density:
		graph.setColor(Color.black);
		// Scale factor for energy density
		double scaleTotal = scaleProperties.getScale();

		// Scale factor for E^2
		double scaleE = scaleProperties.getScale();
		
		// Scale factor for B^2
		double scaleB = scaleProperties.getScale();

		scaleProperties.resetAutomaticScale();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}
		
		for(int k = 0; k < s.grid.getNumCells(1); k++)
		{
			int newPosition = 0;
			int newValue = 0;
			for(int i = 0; i < s.grid.getNumCells(0); i++)
			{

				int oldPosition = newPosition;
				int oldValue = newValue;
				pos[0] = i;
				pos[1] = k;

				// E^2 is placed at the lattice points.
				newPosition = (int) (s.grid.getLatticeSpacing() * (i) * sx);

				/*
					Expectation: Positive fields should point upwards.
					In the flipped and translated coordinate system defined above we have to add the fields to the
					center of the panel in order to get the expected result.
				*/
				double EfieldSquared = 0.0;
				for(int w = 0; w < s.getNumberOfDimensions(); w++) {
					EfieldSquared += s.grid.getEsquaredFromLinks(pos, w) / (as * g * as * g) / 2;
				}
				scaleProperties.putValue(EfieldSquared);
				newValue = (int) (((0.5 + scaleE * EfieldSquared) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}
		
		// Draw B^2:
		graph.setColor(Color.green);
				
		for(int k = 0; k < s.grid.getNumCells(1); k++)
		{
			int newPosition = 0;
			int newValue = 0;
			for(int i = 0; i < s.grid.getNumCells(0); i++)
			{

				int oldPosition = newPosition;
				int oldValue = newValue;
				pos[0] = i;
				pos[1] = k;

				// Gauge fields are placed at the lattice points.
				newPosition = (int) (s.grid.getLatticeSpacing() * i * sx);

				/*
					Expectation: Positive fields should point upwards.
					In the flipped and translated coordinate system defined above we have to add the fields to the
					center of the panel in order to get the expected result.
				*/
				double BfieldSquared = 0.0;
				for(int w = 0; w < s.getNumberOfDimensions(); w++) {
					BfieldSquared += s.grid.getBsquaredFromLinks(pos, w) / (as * g * as * g) / 2;
				}
				scaleProperties.putValue(BfieldSquared);
				newValue = (int) (((0.5 + scaleB * BfieldSquared) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}
		
		// Draw total energy density:
		graph.setColor(Color.red);
				
		for(int k = 0; k < s.grid.getNumCells(1); k++)
		{
			int newPosition = 0;
			int newValue = 0;
			for(int i = 0; i < s.grid.getNumCells(0); i++)
			{

				int oldPosition = newPosition;
				int oldValue = newValue;
				pos[0] = i;
				pos[1] = k;

				// Gauge fields are placed at the lattice points.
				newPosition = (int) (s.grid.getLatticeSpacing() * i * sx);

				/*
					Expectation: Positive fields should point upwards.
					In the flipped and translated coordinate system defined above we have to add the fields to the
					center of the panel in order to get the expected result.
				*/
				double totalEnergy = 0.0;
				for(int w = 0; w < s.getNumberOfDimensions(); w++) {
					totalEnergy += (s.grid.getBsquaredFromLinks(pos, w) + s.grid.getEsquaredFromLinks(pos, w)) / (as * g * as * g) / 2;
				}
				scaleProperties.putValue(totalEnergy);
				newValue = (int) (((0.5 + scaleTotal * totalEnergy) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}
		
		scaleProperties.calculateAutomaticScale(0.5);

	}

	public void addComponents(Box box) {
		addLabel(box, "Energy density panel");
	}
	
	public ScaleProperties getScaleProperties() {
		return scaleProperties;
	}

}