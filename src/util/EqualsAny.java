package util;
/**
 * A class to hold a utility method for comparing a value with a number of other values.
 * @author Jonathan Lovelace
 *
 */
public final class EqualsAny {
	/**
	 * Do not instantiate.
	 */
	private EqualsAny() {
		// Do nothing.
	}
	/**
	 * Compare a value with a number of other (generally constant) values.
	 * @param <TYPE> the type of all the values.
	 * @param against the value to compare the others to
	 * @param values the values to compare to it
	 * @return true if any of theme equal it, false otherwise.
	 */
	public static <TYPE> boolean equalsAny(final TYPE against, final TYPE... values) {
		for (TYPE value : values) {
			if (against.equals(value)) {
				return true; // NOPMD
			}
		}
		return false;
	}
}
