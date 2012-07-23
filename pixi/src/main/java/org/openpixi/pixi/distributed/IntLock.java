package org.openpixi.pixi.distributed;

/**
 * Simple integer lock which waits for a certain value.
 */
public class IntLock {

	private int value;

	public IntLock(int value) {
		this.value = value;
	}

	public synchronized void setValue(int value) {
		this.value = value;
		notifyAll();
	}

	public synchronized void waitForValue(int valueToWaitFor) {
		while (value != valueToWaitFor) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}
}
