package org.openpixi.pixi.distributed.util;

/**
 *  Boolean lock which waits for true.
 */
public class BooleanLock {

	private boolean value = false;

	public synchronized void setToTrue() {
		this.value = true;
		notifyAll();
	}

	public synchronized void reset() {
		this.value = false;
	}

	public synchronized void waitForTrue() {
		while (value != true) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}
}
