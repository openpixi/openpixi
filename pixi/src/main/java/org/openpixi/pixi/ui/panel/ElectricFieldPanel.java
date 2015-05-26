package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;

/**
 * This panel shows the one-dimensional electric field along the x-direction.
 * Several field lines for various grid positions along the y-direction are
 * superimposed.
 */
public class ElectricFieldPanel extends AnimationPanel {

	/** Constructor */
	public ElectricFieldPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);

		Simulation s = getSimulationAnimation().getSimulation();
		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();

		double panelHeight = getHeight();

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

		int colorIndex = getSimulationAnimation().getColorIndex();
		int dirIndex = getSimulationAnimation().getDirectionIndex();


		/*
			Note: The Java 2D API coordinate system is as follows:
			Let (x,y) denote a point. (0,0) is in the upper-left corner of the drawing area.
			The x component increases to the right, the y component increases downward.
		 */

		// Draw electric field:
		graph.setColor(Color.black);
		// Scale factor for electric field
		double scaleE = 1;
		
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

				// Electric fields are placed at the lattice points.
				newPosition = (int) (s.grid.getLatticeSpacing() * (i) * sx);

				// The y-component in the Java 2D API increases downward. Positive field components should (?) point upward.
				double electricField = s.grid.getE(pos, dirIndex).get(colorIndex);
				newValue = (int) (((0.5 + scaleE * electricField) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}
		
		// Draw gauge field:
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

				// The y-component in the Java 2D API increases downward. Positive field components should (?) point upward.
				double gaugeField = s.grid.getU(pos, dirIndex).getLinearizedAlgebraElement().get(colorIndex);
				newValue = (int) (((0.5 + scaleE * gaugeField) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}

	}

}
