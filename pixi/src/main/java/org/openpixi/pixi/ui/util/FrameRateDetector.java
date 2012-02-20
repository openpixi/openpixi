package org.openpixi.pixi.ui.util;

/**
 * Measures the frame rate for real-time display.
 */
public class FrameRateDetector {

	private long detectiondelay;
	private long lasttime;
	private long lastduration;
	private long durationsum;
	private long durationcount;
	private double framerate;

	/**
	 * Measures the frame rate for real-time display.
	 * @param detectiondelay minimum duration in milliseconds during which 
	 * the frame rate is measured
	 */
	public FrameRateDetector(long detectiondelay) {
		this.detectiondelay = detectiondelay;
	}

	/**
	 * Call this method whenever the screen content is updated.
	 */
	public void update() {
		long currenttime = System.currentTimeMillis();
		lastduration = currenttime - lasttime;
		lasttime = currenttime;
		durationsum += lastduration;
		durationcount++;
		if (durationsum > detectiondelay) {
			framerate = durationcount * 1000. / durationsum;
			durationsum = 0;
			durationcount = 0;
		}
	}

	/**
	 * Obtain the calculated frame rate.
	 */
	public double getRate() {
		return framerate;
	}
	
	/**
	 * Obtain the calculated frame rate as string.
	 * If the rate is larger than 10, the integer part only is returned.
	 * Below 10, the result is given to one significant digit.
	 */
	public String getRateString() {
		if (framerate >= 10) {
			return "" + ((int) framerate);
		} else {
			return "" + ((int) (10 * framerate)) / 10.;
		}
	}
}
