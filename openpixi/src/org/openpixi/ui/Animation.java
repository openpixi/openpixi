//this is an animation with 30 milliseconds between updates

package org.openpixi.ui;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class Animation extends JFrame{
	
	private JButton start, stop, reset;               //the buttons for start, stop & reset
	private JSlider slider;
	private Particle2DPanel particle;
	
	class Slider implements ChangeListener {
		public void stateChanged(ChangeEvent eve)
		{
			JSlider source = (JSlider)eve.getSource();
			if(!source.getValueIsAdjusting())
			{
				int frames = (int)source.getValue();
				if(frames == 0)
				{
					if(!particle.sl)
						particle.startAnimation(0);
				}
				else
				{
					int delay = (int)source.getMaximum() - (int)source.getValue();
				    particle.tim.stop();
				    particle.tim.setDelay(delay);
				    particle.tim.setInitialDelay(10 * delay);
				    particle.tim.start();
				}
			}
		}
	}
		
	
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
		
		this.setTitle("Animation");                           //the title
		this.setVisible(true); 						     		//setting the frame to be visible
		this.setSize(700, 500);                                //setting the size
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        //defines the closing of the frame
		
		start = new JButton("start");                         //constructor for the 3 buttons
		stop = new JButton("stop");
		reset = new JButton("reset");
		
		slider = new JSlider();
		slider.addChangeListener(new Slider());
		slider.setMinimum(0);
		slider.setMaximum(50);
		slider.setValue(30);
		
		start.addActionListener(new Start());                 //giving the buttons their functions
		stop.addActionListener(new Stop());
		reset.addActionListener(new Reset());
		
	    JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());                    //setting the buttons in a separate panel
	    buttons.add(start);
	    buttons.add(stop);
	    buttons.add(reset);
	    buttons.add(slider);
	    
	    this.setLayout(new BorderLayout());
	    this.add(buttons, BorderLayout.SOUTH);                        //putting the buttons into the frame
	    this.add(particle, BorderLayout.CENTER);                      //putting the panel of the particle into the frame

	}
	public static void main(String[] args){
		
		Animation ani = new Animation();                                //starts the animation
	}

}
