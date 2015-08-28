package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.BoundaryFixer;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;

import java.util.HashMap;

/**
 * Yaml wrapper for the BoundaryFixer class.
 */
public class YamlBoundaryFixer {

	/**
	 * String which specifies the boundary plane to be fixed.
	 *
	 * Format: The boundary plane is specified by three parameters: two directions and one coordinate.
	 * Examples:
	 *   "xy0" specifies the xy-plane at z = 0.
	 *   "xy1" specifies the xy-plane at z = Lz, where Lz is the box length.
	 *
	 * For the directions the symbols "x", "y" and "z" can be used.
	 * For the coordinate use either 0 or 1 (either innermost or outermost plane).
	 */
	public String plane;


	/**
	 * Returns an instance of BoundaryFixer according to the parameters in the YAML file.
	 *
	 * @return Instance of BoundaryFixer.
	 */
	public BoundaryFixer getFileGenerator() {
		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		map.put('x', 0);
		map.put('y', 1);
		map.put('z', 2);
		map.put('0', 0);
		map.put('1', 1);

		char[] chars = plane.toCharArray();
		int loopIndex1 = map.get(chars[0]);
		int loopIndex2 = map.get(chars[1]);
		int fixedIndexPosition = map.get(chars[2]);

		// This line maps the two directions to the fixed direction. Example: xy (0,1) plane to z (2).
		int fixedIndex = 3 - (loopIndex1 + loopIndex2);

		BoundaryFixer diagnostic = new BoundaryFixer(loopIndex1, loopIndex2, fixedIndex, fixedIndexPosition);
		return diagnostic;
	}
}
