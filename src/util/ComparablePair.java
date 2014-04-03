package util;

/**
 * A pair of Comparables.
 *
 * @see Pair
 * @author Jonathan Lovelace
 *
 * @param <K> the type of the first item
 * @param <V> the type of the second item
 */
public class ComparablePair<K extends Comparable<K>, V extends Comparable<V>>
		extends Pair<K, V> implements Comparable<ComparablePair<K, V>> {
	/**
	 * Constructor.
	 *
	 * @param firstItem The first item in the pair.
	 * @param secondItem The second item in the pair.
	 */
	protected ComparablePair(final K firstItem, final V secondItem) {
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
	public int compareTo(final ComparablePair<K, V> other) {
		final int cmp = first().compareTo(other.first());
		if (cmp == 0) {
			return second().compareTo(other.second()); // NOPMD
		} else {
			return cmp;
		}
	}

	/**
	 * Create a pair without having to specify the types.
	 *
	 * @param <K> The type of the first element in the pair
	 * @param <V> The type of the second element in the pair
	 * @param first The first element in the pair.
	 * @param second The second element in the pair.
	 * @return a pair containing the two elements
	 */
	public static <K extends Comparable<K>, V extends Comparable<V>>
			ComparablePair<K, V> of(final K first, final V second) {
		return new ComparablePair<>(first, second);
	}

}
