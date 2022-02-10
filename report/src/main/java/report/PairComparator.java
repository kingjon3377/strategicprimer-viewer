package report;

import org.javatuples.Pair;
import java.util.Comparator;


/**
 * Given comparators of the pair's types, produce a comparator function that
 * compares pairs using the first and then the second element.
 *
 * TODO: Convert to a helper method in AbstractReportGenerator or some such
 */
public final class PairComparator<First, Second> implements Comparator<Pair<First, Second>> {
	private final Comparator<First> first;
	private final Comparator<Second> second;
	public PairComparator(final Comparator<First> first, final Comparator<Second> second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Given comparators of the pair's types, produce a comparator function
	 * that compares pairs using the first and then the second element.
	 */
	@Override
	public int compare(final Pair<First, Second> one, final Pair<First, Second> two) {
		return Comparator.<Pair<First, Second>, First>comparing(Pair::getValue0, first)
				.thenComparing(Pair::getValue1, second).compare(one, two);
	}
}
