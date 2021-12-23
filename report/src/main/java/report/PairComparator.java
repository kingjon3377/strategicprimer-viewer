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
	public PairComparator(Comparator<First> first, Comparator<Second> second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Given comparators of the pair's types, produce a comparator function
	 * that compares pairs using the first and then the second element.
	 *
	 * TODO: May need wildcards somewhere in the type signatures here ...
	 */
	@Override
	public int compare(Pair<First, Second> one, Pair<First, Second> two) {
		int retval = first.compare(one.getValue0(), two.getValue0());
		if (retval == 0) {
			return second.compare(one.getValue1(), two.getValue1());
		} else {
			return retval;
		}
	}
}
