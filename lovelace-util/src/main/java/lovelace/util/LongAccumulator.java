package lovelace.util;

/**
 * An implementation of {@link Accumulator} for long integers.
 */
public class LongAccumulator implements Accumulator<Long> {
	private long count;

	public LongAccumulator(long count) {
		this.count = count;
	}

	@Override
	public void add(final Long addend) {
		count += addend;
	}

	@Override
	public Long getSum() {
		return count;
	}
}
