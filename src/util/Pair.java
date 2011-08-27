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
public final class Pair<FIRST, SECOND> implements
		Comparable<Pair<FIRST, SECOND>> {
	/**
	 * The first item in the pair.
	 */
	private final FIRST first;
	/**
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
		first = firstItem;
		second = secondItem;
	}

	/**
	 * Create a pair without having to specify the types.
	 * 
	 * @param <FIRST>
	 *            The type of the first element in the pair.
	 * @param <SECOND>
	 *            The type of the second element in the pair.
	 * @param first
	 *            The first element in the pair.
	 * @param second
	 *            The second element in the pair.
	 * @return a pair containing the two elements
	 */
	public static <FIRST, SECOND> Pair<FIRST, SECOND> of(final FIRST first, // NOPMD
			final SECOND second) {
		return new Pair<FIRST, SECOND>(first, second);
	}

	/**
	 * Compare to another pair.
	 * 
	 * @param other
	 *            the other pair
	 * @return the result of the comparison.
	 */
	@Override
	public int compareTo(final Pair<FIRST, SECOND> other) {
		final int cmp = compare(first, other.first);
		return (cmp == 0) ? compare(second, other.second) : cmp;
	}

	/**
	 * TODO: move this to a helper class.
	 * @param one
	 *            one object
	 * @param two
	 *            another
	 * @return a comparison between the two objects.
	 */
	@SuppressWarnings("unchecked")
	private static int compare(final Object one, final Object two) {
		return (one == null) ? (two == null) ? 0 : -1 : (two == null) ? +1
				: ((Comparable<Object>) one).compareTo(two);
	}

	/**
	 * @return a hash code for the pair.
	 */
	@Override
	public int hashCode() {
		return 31 * hashcode(first) + hashcode(second);
	}

	// ESCA-JAVA0244:
	// ESCA-JAVA0064:
	/**
	 * TODO: move this to a helper class.
	 * @param obj
	 *            an object
	 * @return a hash code for it, or 0 if null.
	 */
	private static int hashcode(final Object obj) {
		return (obj == null) ? 0 : obj.hashCode();
	}

	/**
	 * @param obj
	 *            an object
	 * @return whether it's the same as this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) { 
		return (this == obj) ? true : (obj instanceof Pair) ? equal(first,
				((Pair) obj).first) && equal(second, ((Pair) obj).second)
				: false;
	}

	/**
	 * TODO: move this to a helper class.
	 * @param one
	 *            One object
	 * @param two
	 *            Another object
	 * @return whether the objects are equal
	 */
	private static boolean equal(final Object one, final Object two) {
		return (one == null) ? two == null : (one == two || one.equals(two)); // NOPMD // $codepro.audit.disable useEquals
	}

	/**
	 * @return a String representation of the pair.
	 */
	@Override
	public String toString() {
		return '(' + first.toString() + ", " + second + ')';
	}
}
