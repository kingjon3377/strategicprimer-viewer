package lovelace.util;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import org.javatuples.Pair;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * A class to count references to enumerated objects---though it does not do
 * any sort of check that its type parameter is an enumerated type. For every
 * object ever passed to {@link #count} (or {@link #countMany}), it keeps a
 * running total of the number of times it has been passed that object.
 */
public class EnumCounter<Type> {
	private final Map<Type, Accumulator<Integer>> counts = new HashMap<>();

	private void count(final Type item) {
		if (counts.containsKey(item)) {
			counts.get(item).add(1);
		} else {
			counts.put(item, new IntAccumulator(1));
		}
	}

	/**
	 * Count the provided items.
	 */
	@SafeVarargs
	public final void countMany(final Type... values) {
		for (final Type item : values) {
			count(item);
		}
	}

	/**
	 * Get the count for a given value.
	 */
	public int getCount(final Type item) {
		return Optional.ofNullable(counts.get(item)).map(Accumulator::getSum).orElse(0);
	}

	/**
	 * Get all values and counts.
	 */
	public Iterable<Pair<Type, Integer>> getAllCounts() {
		return streamAllCounts().collect(Collectors.toList());
	}

	/**
	 * Stream all values and counts.
	 */
	public Stream<Pair<Type, Integer>> streamAllCounts() {
		return counts.entrySet().stream().map(e -> Pair.with(e.getKey(), e.getValue().getSum()));
	}
}
