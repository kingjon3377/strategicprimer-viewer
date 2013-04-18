package util;
/**
 * A pair of Comparables.
 * @see Pair
 * @author Jonathan Lovelace
 *
 * @param <FIRST> the type of the first item
 * @param <SECOND> the type of the second item
 */
public class ComparablePair<FIRST extends Comparable<FIRST>, SECOND extends Comparable<SECOND>>
		extends Pair<FIRST, SECOND> implements
		Comparable<ComparablePair<FIRST, SECOND>> {
	/**
	 * Constructor.
	 *
	 * @param firstItem The first item in the pair.
	 * @param secondItem The second item in the pair.
	 */
	protected ComparablePair(final FIRST firstItem, final SECOND secondItem) {
		super(firstItem, secondItem);
	}

	/**
	 * Compare to another pair.
	 *
	 * @param other the other pair
	 *
	 * @return the result of the comparison.
	 */
	@Override
	public int compareTo(final ComparablePair<FIRST, SECOND> other) {
		final int cmp = first().compareTo(other.first());
		return (cmp == 0) ? second().compareTo(other.second()) : cmp;
	}
	/**
	 * Create a pair without having to specify the types.
	 *
	 * @param <FIRST> The type of the first element in the pair
	 * @param <SECOND> The type of the second element in the pair
	 * @param first The first element in the pair.
	 * @param second The second element in the pair.
	 * @return a pair containing the two elements
	 */
	public static <FIRST extends Comparable<FIRST>, SECOND extends Comparable<SECOND>> ComparablePair<FIRST, SECOND> of(// NOPMD
			final FIRST first, final SECOND second) {
		return new ComparablePair<FIRST, SECOND>(first, second);
	}

}
