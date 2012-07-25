package org.openpixi.pixi.physics.util;

import java.lang.reflect.Field;

/**
 * Creates shallow field copy of source in destination class.
 */
public class ClassCopier {

	public static <T> T copy(T source) {
		T destination = null;
		try {
			destination = (T)source.getClass().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		copy(source, destination);
		return destination;
	}


	public static <T> void copy(T source, T destination) {
		for (Field sourceField: source.getClass().getDeclaredFields()) {
			try {
				Field destinationField = destination.getClass().getDeclaredField(sourceField.getName());

				// Allow access to private fields
				sourceField.setAccessible(true);
				destinationField.setAccessible(true);

				destinationField.set(destination, sourceField.get(source));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}
