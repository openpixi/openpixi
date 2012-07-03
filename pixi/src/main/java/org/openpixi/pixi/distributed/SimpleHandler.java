package org.openpixi.pixi.distributed;


/**
 * Serves to propagate upcalls of ibis from classes wrapping up ibis 
 * communication to classes implementing the logic of the parallel algorithm.
 */
public interface SimpleHandler {

	public void handle(Object arg);
	
}
