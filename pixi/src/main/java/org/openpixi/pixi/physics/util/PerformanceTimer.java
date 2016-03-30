package org.openpixi.pixi.physics.util;

/**
 * Created by David on 20.03.2016.
 */
public class PerformanceTimer {

	private long time = 0;
	public boolean active = false;

	public void reset() {
		time = System.nanoTime();
	}

	public void lap(String s) {
		if(active) {
			long delta = (System.nanoTime() - time) / (1000 * 1000);
			System.out.println(s + " " + delta + "ms");
			reset();
		}
	}

}
