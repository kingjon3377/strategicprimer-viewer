package lovelace.util;

import java.math.BigDecimal;

/**
 * An implementation of {@link Accumulator} for arbitrary-precision decimal numbers.
 */
public final class DecimalAccumulator implements Accumulator<BigDecimal> {
	private BigDecimal count;

	public DecimalAccumulator(final BigDecimal count) {
		this.count = count;
	}

	@Override
	public void add(final BigDecimal addend) {
		count = count.add(addend);
	}

	@Override
	public BigDecimal getSum() {
		return count;
	}

	@Override
	public String toString() {
		return "DecimalAccumulator{count=" + count + '}';
	}
}
