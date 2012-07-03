package org.openpixi.pixi.distributed;

/**
 * Common interface for master and slave application logic.
 */
public interface Node {
	public void problemDistribution();
	public void step();
	public void resultCollection();
}
