package report;

import org.javatuples.Pair;
import java.util.Comparator;


/**
 * Given comparators of the pair's types, produce a comparator function that
 * compares pairs using the first and then the second element.
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
		int retval = first.compare(one.getValue0(), two.getValue0());
		if (retval == 0) {
			return second.compare(one.getValue1(), two.getValue1());
		} else {
			return retval;
		}
	}
}
