package org.openpixi.pixi.physics.util;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Settings;

/**
 * Tests the class copier.
 */
public class ClassCopierTest extends TestCase {

	public void testCopy() throws Exception {
		Settings settings = new Settings();
		settings.setGridCellsX(14);

		Settings settings2 = ClassCopier.copy(settings);

		assertEquals(settings.getGridCellsX(), settings2.getGridCellsX());
		assertEquals(settings.getInterpolator(), settings2.getInterpolator());
	}
}
