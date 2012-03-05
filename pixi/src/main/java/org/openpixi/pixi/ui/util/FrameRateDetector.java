/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
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
