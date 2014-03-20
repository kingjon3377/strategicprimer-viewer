package util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to remove the "taint" of null from values.
 * @author Jonathan Lovelace
 */
public final class NullCleaner {
	/**
	 * Do not instantiate.
	 */
	private NullCleaner() {
		// Static-only class.
	}
	/**
	 * @param <T> the type of thing we're dealing with
	 * @param val a value
	 * @param def a default value
	 * @return val if it isn't null, def if val is null
	 */
	public static <T> T valueOrDefault(@Nullable final T val, final T def) {
		if (val == null) {
			return def;
		} else {
			return val;
		}
	}
	/**
	 * Assert that a value isn't null.
	 * @param <T> the type of the value
	 * @param val the value
	 * @return it, if it isn't null.
	 */
	public static <T> T assertNotNull(@Nullable final T val) {
		assert val != null;
		return val;
	}
}
