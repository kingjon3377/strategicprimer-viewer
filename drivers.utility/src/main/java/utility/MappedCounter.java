package utility;

import java.util.stream.Stream;

import org.javatuples.Pair;

import java.util.function.Function;

import lovelace.util.Accumulator;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Comparator;

import java.util.Map;
import java.util.HashMap;

/**
 * A class that, like the {@link lovelace.util.EnumCounter} class, keeps
 * a running total for arguments it is given; unlike that class, it groups on
 * the basis of a field (or equivalent mapping) provided to its constructor and
 * increments the total by the value of another field instead of a constant
 * value.
 *
 * TODO: If Key only ever String, drop type parameter
 *
 * TODO: Move to lovelace.util? (If so, leave Key as-is.)
 *
 * N.B. in Ceylon the type bound for Count was that it satisfy
 * {@code Summable<Count>&Comparable<Count>}
 */
class MappedCounter<Base, Key, Count extends Number & Comparable<Count>> implements Iterable<Pair<Key, Count>> {
	/**
	 * @param keyExtractor   An accessor method to get the key to use for each object that is to be counted.
	 * @param countExtractor An accessor method to get the quantity to
	 *                       increment the count by for each object that is to be counted.
	 * @param factory        A constructor for an accumulator for the count type.
	 * @param zero           Zero in the count type.
	 */
	public MappedCounter(final Function<Base, Key> keyExtractor, final Function<Base, Count> countExtractor,
						 final Function<Count, Accumulator<Count>> factory, final Count zero) {
		this.keyExtractor = keyExtractor;
		this.countExtractor = countExtractor;
		this.factory = factory;
		this.zero = zero;
	}

	private final Function<Base, Key> keyExtractor;
	private final Function<Base, Count> countExtractor;
	private final Function<Count, Accumulator<Count>> factory;
	private final Count zero;

	private final Map<Key, Accumulator<Count>> totals = new HashMap<>();

	/**
	 * Increment the count for the given key by the given amount.
	 */
	public void addDirectly(final Key key, final Count addend) {
		if (totals.containsKey(key)) {
			totals.get(key).add(addend);
		} else {
			totals.put(key, factory.apply(addend));
		}
	}

	/**
	 * Increment the count for the key and by the quantity extracted from the given object.
	 */
	public void add(final Base obj) {
		addDirectly(keyExtractor.apply(obj), countExtractor.apply(obj));
	}

	/**
	 * A stream of keys and associated counts seen so far.
	 */
	@Override
	public Iterator<Pair<Key, Count>> iterator() {
		return stream().iterator();
	}

	public Stream<Pair<Key, Count>> stream() {
		return totals.entrySet().stream().map(e -> Pair.with(e.getKey(), e.getValue().getSum()))
				.sorted(Comparator.comparing(Pair::getValue1, Comparator.reverseOrder()));
	}

	@SuppressWarnings("unchecked") // TODO: Take a Class<Count> field to do the cast without triggering the warning
	private Count plus(final Count one, final Count two) {
		return switch (one) {
			case final Integer i -> (Count) Integer.valueOf(i + (Integer) two);
			case final Long l -> (Count) Long.valueOf(l + (Long) two);
			case final Double v -> (Count) Double.valueOf(v + (Double) two);
			case final Float v -> (Count) Float.valueOf(v + (Float) two);
			case final BigInteger bigInteger -> (Count) bigInteger.add((BigInteger) two);
			case final BigDecimal bigDecimal -> (Count) bigDecimal.add((BigDecimal) two);
			default -> throw new IllegalStateException("Unhandled Count class");
		};
	}

	/**
	 * The total counted for all keys taken together.
	 */
	public Count getTotal() {
		return totals.values().stream().map(Accumulator::getSum).reduce(zero, this::plus);
	}
}
