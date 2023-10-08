package lovelace.util;

import java.math.BigDecimal;

/**
 * An implementation of {@link Accumulator} for arbitrary-precision decimal numbers.
 */
public class DecimalAccumulator implements Accumulator<BigDecimal> {
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
}
