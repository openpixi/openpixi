package org.openpixi.pixi.distributed.util;

/**
 * Simple integer lock which waits for a certain positive value to be reached.
 */
public class CountLock {

	private int requiredCount;
	private int actualCount;

	public CountLock(int requiredCount) {
		this.requiredCount = requiredCount;
	}

	public synchronized void increase() {
		++actualCount;
		notifyAll();
	}

	public synchronized void waitForCount() {
		while (requiredCount != actualCount) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	public synchronized void reset() {
		actualCount = 0;
	}
}
