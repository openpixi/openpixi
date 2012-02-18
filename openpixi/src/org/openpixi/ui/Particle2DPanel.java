//this is an animation with 30 milliseconds between updates

package org.openpixi.ui;
import org.openpixi.physics.*;
import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.util.ArrayList;

public class Particle2DPanel extends JPanel{
	
	private static final int step = 30;
	
	private int interval = step;                        //Milliseconds between updates
	public Timer tim;                                //Timer that start the animation for one step
	public boolean sl = false;
	
	//defining the initial conditions for the particle
	private static final double x = 0.0;
	private static final double y = 400.0;
	private static final double vx = 30.0;
	private static final double vy = 30.0;
	
	private static final int NUM_PARTICLES = 10;
	
	private Particle2D par = new Particle2D(x, y, vx, vy, 0.0, 0.0, 1, 0);             //new particle
	private Force f = new Force(0.0);                                                  //new force
	
	//Particle2D[] parlist = new Particle2D[NUM_PARTICLES];
	ArrayList<Particle2D> parlist = new ArrayList<Particle2D>();
	
    //this intern class is required for the timer, i.e. "creating" the timer
	public class TimerStarts implements ActionListener{
		
		public void actionPerformed(ActionEvent eve)
		{
			for(int i = 0; i < NUM_PARTICLES; i++)
			{
				Particle2D par = (Particle2D) parlist.get(i);
				par.setBoundaries(getHeight(), getWidth());
				par.algorithm(0.5, f);
			}
			repaint();
		}
	}
	
	public Particle2DPanel()                                           //the constructor
	{
		tim = new Timer(interval, new TimerStarts());               //it creates the timer together with the above class
		
		this.setVisible(true); 						     		//setting the panel to be visible
		this.setSize(700, 500);                                 //setting the size
		
		for(int i = 0; i < NUM_PARTICLES; i++)
		{
		parlist.add(new Particle2D(x, y, i + vx, i + vy, 0.0, 0.0, i + 1, 0));
		}
		
	}
	public void startAnimation(int i)                           //defining when the animation starts/stops
	{
		if(i == 1)
		{
			tim.start();
			sl = false;
		}
		else
			if(i == 0)
			{
				tim.stop();
				sl = true;
			}
			else
				if(i == -1)
				{
					tim.restart();
					tim.stop();
					par.x = x;
					par.y = y;
					par.vx = vx;
					par.vy = vy;
					sl = true;
				}
	}
	
	public void paintComponent(Graphics graph)                  //painting the particle
	{
		setBackground(Color.gray);
		super.paintComponent(graph); 
		for(int i = 0; i < NUM_PARTICLES; i++)
		{
			Particle2D par = (Particle2D) parlist.get(i);
			graph.setColor(Color.blue);
			graph.fillOval((int)par.x, (int)par.y, 15, 15);
		}
	}

}