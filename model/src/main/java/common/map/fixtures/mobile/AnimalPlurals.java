package common.map.fixtures.mobile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import lovelace.util.FileSplitter;

/**
 * Plurals for various kinds of animals, loaded from file, so that a population
 * of multiple animals can be stored with a singular kind but be presented to
 * the user with the proper plural. If one is not found, it defaults to the
 * same as the singular.
 */
public final class AnimalPlurals {
	private AnimalPlurals() {
	}

	private static Map<String, String> initPlurals() {
		try {
			return FileSplitter.getFileContents(Paths.get("animal_data", "plurals.txt"), Function.identity());
		} catch (final IOException except) {
			throw new RuntimeException(except);
		}
	}

	private static final Map<String, String> PLURALS = initPlurals();

	public static String get(final String key) {
		return PLURALS.getOrDefault(key, key);
	}

	public static boolean hasKey(final String key) {
		return PLURALS.containsKey(key);
	}
}
