//this is a basic simulation

package org.openpixi.ui;
import org.openpixi.physics.*;
import java.awt.*;
import javax.swing.*;

public class Simulation extends JFrame {

	public Simulation()
	{
		setVisible(true);
		setSize(700, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void path(Graphics graph)
	{
		final double stepmax = 15;      //how many steps will the simulation last
		final double step = 0.5;     //the step that is used in the algorithm

		Particle2D par = new Particle2D(0.0, 400.0, 30.0, 30.0, 0.0, 0.0, 1, 0);   //The particle
		Force f = new Force(0.0, 0.0, 0.0, 0.0);                           //The Force with 0 dragging and fields
		par.setBoundaries(getHeight(), getWidth());
		graph.setColor(Color.blue);                                              //setting the color of the graphics

		for(double t = 0.0; t < stepmax; t += step)                                      //drawing the path
		{
			graph.fillOval((int)par.x, (int)par.y, 15, 15);
			par.algorithm(step, f);
		}
	}

	public void paint(Graphics graph)
	{
		setBackground(Color.white);
		path(graph);
	}

	public static void main(String[] args){

		Simulation sim = new Simulation();                                //starts the simulation
	}

}