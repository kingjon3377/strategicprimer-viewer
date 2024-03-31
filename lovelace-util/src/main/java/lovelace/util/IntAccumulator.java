package lovelace.util;

/**
 * An implementation of {@link Accumulator} for integers.
 */
public final class IntAccumulator implements Accumulator<Integer> {
	private int count;

	public IntAccumulator(final int count) {
		this.count = count;
	}

	@Override
	public void add(final Integer addend) {
		count += addend;
	}

	@Override
	public Integer getSum() {
		return count;
	}

	@Override
	public String toString() {
		return "IntAccumulator{" + count + '}';
	}
}
