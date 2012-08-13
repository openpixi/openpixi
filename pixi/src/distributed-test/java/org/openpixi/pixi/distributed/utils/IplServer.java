package org.openpixi.pixi.distributed.utils;

/**
 * Handles the creation and destruction of ipl registry server.
 */
public class IplServer {

	private static Thread iplServer;

	public static void start() {
		if (iplServer == null) {
			iplServer = new Thread(new Runnable() {
				public void run() {
					ibis.ipl.server.Server.main(new String[] {"--events"});
				}
			});
			iplServer.start();
		}
	}

	public static void end() {
		iplServer.interrupt();
	}
}
