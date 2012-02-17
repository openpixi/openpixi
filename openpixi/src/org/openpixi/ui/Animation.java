//this is an animation with 30 milliseconds between updates

package org.openpixi.ui;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Animation extends JFrame{
	
	private JButton start, stop, reset;               //the buttons for start, stop & reset
	private Particle2DPanel particle;
	
	
	class Start implements ActionListener{                       //this intern class defines the start for the button
		public void actionPerformed(ActionEvent eve)
		{
			particle.startAnimation(1);
		}
	}
	
	class Stop implements ActionListener{                     //this intern class defines the stop for the button
		public void actionPerformed(ActionEvent eve)
		{
			particle.startAnimation(0);
		}
	}
	
	class Reset implements ActionListener{                     //this intern class defines the reset for the button
		public void actionPerformed(ActionEvent eve)
		{
			particle.startAnimation(-1);
		}
	}
	
	public Animation()                                           //the constructor
	{
		particle = new Particle2DPanel();
		
		this.setTitle("Simulation");                           //the title
		this.setVisible(true); 						     		//setting the frame to be visible
		this.setSize(700, 500);                                //setting the size
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        //defines the closing of the frame
		
		start = new JButton("start");                         //constructor for the 3 buttons
		stop = new JButton("stop");
		reset = new JButton("reset");
		
		start.addActionListener(new Start());                 //giving the buttons their functions
		stop.addActionListener(new Stop());
		reset.addActionListener(new Reset());
		
	    JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());                    //setting the buttons in a separate panel
	    buttons.add(start);
	    buttons.add(stop);
	    buttons.add(reset);
	    
	    this.setLayout(new BorderLayout());
	    this.add(buttons, BorderLayout.NORTH);                        //putting the buttons into the frame
	    this.add(particle, BorderLayout.CENTER);                      //putting the panel of the particle into the frame

	}
	public static void main(String[] args){
		
		Animation ani = new Animation();                                //starts the animation
	}

}
