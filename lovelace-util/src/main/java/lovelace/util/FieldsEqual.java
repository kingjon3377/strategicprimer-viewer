package lovelace.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * A class providing a helper method to condense equals() methods.
 *
 * @author Jonathan Lovelace
 */
public final class FieldsEqual {
	private FieldsEqual() {
		// Do not instantiate.
	}

	/**
	 * Test whether two objects are equal in all the specified fields.
	 * @param one the first object
	 * @param two the second object
	 * @param <T> the shared supertype of the two objects
	 * @param getters a series of getters on the class they share
	 * @return whether the objects are equal as far as the specified getters are concerned.
	 */
	public static <T> boolean fieldsEqual(final T one, final T two, final Function<T, ?>... getters) {
		for (final Function<T, ?> getter : getters) {
			if (!Objects.equals(getter.apply(one), getter.apply(two))) {
				return false;
			}
		}
		return true;
	}
}
