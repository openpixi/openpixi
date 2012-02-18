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
	public boolean sl = false;                        //it is needed for the slider
	
	//defining the initial conditions for the particle
	//private static final double x = 0.0;
	//private static final double y = 400.0;
	//private static final double vx = 30.0;
	//private static final double vy = 30.0;
	
	private static final int NUM_PARTICLES = 10;
	
	private Force f = new Force(0.0, 1.1, 1.2, 1.3);              //new force
	
	//making an array for more particles
	ArrayList<Particle2D> parlist = new ArrayList<Particle2D>();
	
    //this intern class is required for the timer, i.e. "creating" the timer
	public class TimerStarts implements ActionListener{
		
		public void actionPerformed(ActionEvent eve)
		{
			for(int i = 0; i < NUM_PARTICLES; i++)         //this cycle is needed because there are more particles
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
		
		for(int i = 0; i < NUM_PARTICLES; i++)                  //constructing the particles
		{
		parlist.add(new Particle2D(Math.random(), Math.random(), 100 * Math.random(), 100 * Math.random(), 0.0, 0.0, Math.random() + 1, 10 * Math.random()));
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
					for(int k = 0; k < NUM_PARTICLES; k++)          //giving the initial conditions again for reset
					{
						Particle2D par = (Particle2D) parlist.get(k);
						par.x = Math.random();
						par.y = Math.random();
						par.vx = 100 * Math.random();
						par.vy = 100 * Math.random();
					}
					sl = true;
				}
	}
	
	public void paintComponent(Graphics graph)                  //painting the particle
	{
		setBackground(Color.gray);
		super.paintComponent(graph); 
		for(int i = 0; i < NUM_PARTICLES; i++)       //this cycle is needed because there are more particles
		{
			Particle2D par = (Particle2D) parlist.get(i);
			graph.setColor(Color.blue);
			graph.fillOval((int)par.x, (int)par.y, 15, 15);
		}
	}

}