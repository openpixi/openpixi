//this is an animation with 30 milliseconds between updates

package org.openpixi.ui;
import org.openpixi.physics.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Particle2DPanel extends JPanel{
	
	private static final int step = 30;
	
	private int interval = step;                        //Milliseconds between updates
	private Timer tim;                                //Timer that start the animation for one step
	
	//defining the initial conditions for the particle
	private static final double x = 0.0;
	private static final double y = 400.0;
	private static final double vx = 30.0;
	private static final double vy = 30.0;
	
	private Particle2D par = new Particle2D(x, y, vx, vy, 0.0, 0.0, 1, 0);             //new particle
	private Force f = new Force(0.0);                                                  //new force
	
    //this intern class is required for the timer, i.e. "creating" the timer
	class TimerStarts implements ActionListener{
		
		public void actionPerformed(ActionEvent eve)
		{
			par.setBoundaries(getHeight(), getWidth());
			par.algorithm(0.5, f);
			repaint();
		}
	}
	
	public Particle2DPanel()                                           //the constructor
	{
		tim = new Timer(interval, new TimerStarts());               //it creates the timer together with the above class
		
		this.setVisible(true); 						     		//setting the panel to be visible
		this.setSize(700, 500);                                //setting the size
		
	}
	public void startAnimation(int i)                  //defining when the animation starts/stops
	{
		if(i == 1)
			tim.start();
		else
			if(i == 0)
				tim.stop();
			else
				if(i == -1)
				{
					tim.restart();
					tim.stop();
					par.x = x;
					par.y = y;
					par.vx = vx;
					par.vy = vy;
				}
	}
	
	public void paintComponent(Graphics graph)                  //painting the particle
	{
		setBackground(Color.gray);
		graph.setColor(Color.blue);
		super.paintComponent(graph);  
		graph.fillOval((int)par.x, (int)par.y, 15, 15);
	}

}