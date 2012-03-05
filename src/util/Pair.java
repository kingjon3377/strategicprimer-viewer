package util;

/**
 * From
 * <http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-
 * c-pairl-r-in-java/3646398#3646398>.
 * 
 * @author Peter Lawrey
 * @author adapted by Jonathan Lovelace to pass muster with static analysis,
 *         etc.
 * 
 * @param <FIRST>
 *            The first type in the pair.
 * @param <SECOND>
 *            The second type in the pair.
 */
public final class Pair<FIRST extends Comparable<FIRST>, SECOND extends Comparable<SECOND>> implements
		Comparable<Pair<FIRST, SECOND>> {
	/**
	 * The first item in the pair.
	 */
	private final FIRST first;

	/**
	 * 
	 * @return the first item in the pair
	 */
	public FIRST first() {
		return first;
	}

	/**
	 * The second item in the pair.
	 */
	private final SECOND second;

	/**
	 * 
	 * @return the second item in the pair
	 */
	public SECOND second() {
		return second;
	}

	// ESCA-JAVA0029:
	/**
	 * Constructor.
	 * 
	 * @param firstItem
	 *            The first item in the pair.
	 * @param secondItem
	 *            The second item in the pair.
	 */
	private Pair(final FIRST firstItem, final SECOND secondItem) {
		if (firstItem == null || secondItem == null) {
			throw new IllegalArgumentException("Pair refuses null arguments");
		}
		first = firstItem;
		second = secondItem;
	}

	/**
	 * Create a pair without having to specify the types.
	 * 
	 * @param <FIRST>
	 *            The type of the first element in the pair
	 * @param <SECOND>
	 *            The type of the second element in the pair
	 * @param first
	 *            The first element in the pair.
	 * @param second
	 *            The second element in the pair.
	 * @return a pair containing the two elements
	 */
	public static <FIRST extends Comparable<FIRST>, SECOND extends Comparable<SECOND>> Pair<FIRST, SECOND> of(// NOPMD
			final FIRST first, final SECOND second) {
		return new Pair<FIRST, SECOND>(first, second);
	}

	/**
	 * Compare to another pair.
	 * 
	 * @param other
	 *            the other pair
	 * 
	 * @return the result of the comparison.
	 */
	@Override
	public int compareTo(final Pair<FIRST, SECOND> other) {
		final int cmp = first.compareTo(other.first);
		return (cmp == 0) ? second.compareTo(other.second) : cmp;
	}

	/**
	 * 
	 * @return a hash code for the pair.
	 */
	@Override
	public int hashCode() {
		return 31 * first.hashCode() + second.hashCode();
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's the same as this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) {
		return (this == obj) ? true
				: (obj instanceof Pair) ? ((first == ((Pair) obj).first || first
						.equals(((Pair) obj).first)))
						&& ((second == ((Pair) obj).second || second
								.equals(((Pair) obj).second))) : false;
	}

	/**
	 * @return a String representation of the pair.
	 */
	@Override
	public String toString() {
		return '(' + first.toString() + ", " + second + ')';
	}
}
