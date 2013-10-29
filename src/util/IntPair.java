package util;

/**
 * A pair of ints.
 *
 * @author Jonathan Lovelace
 *
 */
public class IntPair {
	/**
	 * The first.
	 */
	public final int first;
	/**
	 * The second.
	 */
	public final int second;

	/**
	 * Constructor.
	 *
	 * @param one the first int
	 * @param two the second int.
	 */
	public IntPair(final int one, final int two) {
		first = one;
		second = two;
	}

	/**
	 * @param one a first int
	 * @param two a second int
	 * @return a pair of those ints
	 */
	public static IntPair of(final int one, final int two) { // NOPMD
		return new IntPair(one, two);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(15);
		builder.append('(');
		builder.append(first);
		builder.append(", ");
		builder.append(second);
		builder.append(')');
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}

}
