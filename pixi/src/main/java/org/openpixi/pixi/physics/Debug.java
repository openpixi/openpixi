package org.openpixi.pixi.physics;

public class Debug {
	/**
	 * Turns on assert commands.
	 */
	public static final boolean asserts = false;

	/**
	 * Checks that assertions are enabled.
	 * 
	 * <p>To enable asserts do one of the following:</p>
	 * <ul>
	 * <li>Command line: launch java with parameter '-ea'.</li>
	 * <li>Eclipse: menu Run > Run Configurations... > Arguments > VM Arguments: '-ea'.</li>
	 * <li>Eclipse (globally): menu Window > Preferences > Java > Installed JREs > Edit > Default VM Arguments: '-ea'.</li>
	 * </ul>
	 */
	public static void checkAssertsEnabled() {
		if (asserts) {
			boolean assertsEnabled = false;
			assert assertsEnabled = true;
			if (!assertsEnabled) {
				System.out.println("");
				System.out.println("Asserts must be enabled:");
				System.out.println("");
				System.out.println("To enable asserts do one of the following:");
				System.out.println(" * Command line: launch java with parameter '-ea'.");
				System.out.println(" * Eclipse: menu Run > Run Configurations... > Arguments > VM Arguments: '-ea'.");
				System.out.println(" * Eclipse (globally): menu Window > Preferences > Java > Installed JREs > Edit > Default VM Arguments: '-ea'.");
				System.out.println("");
				throw new RuntimeException("Asserts must be enabled! (see comment for org.openpixi.pixi.physics.Debug.checkAssertsEnabled().)");
			}
		}
	}
}
