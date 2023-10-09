package common.map;

import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * An interface for fixtures representing large features that report their extent in acres.
 *
 * @see HasPopulation
 */
public interface HasExtent<Self extends HasExtent<Self>> extends SubsettableFixture {
    /**
     * Determine whether the given number is positive.
     */
    static boolean isPositive(final Number number) {
        return switch (number) {
            case Integer i -> number.intValue() > 0;
            case Long l -> number.longValue() > 0L;
            case Float v -> number.floatValue() > 0.0f;
            case Double v -> number.doubleValue() > 0.0;
            case BigInteger bigInteger -> bigInteger.signum() > 0;
            case BigDecimal bigDecimal -> bigDecimal.signum() > 0;
            default -> number.doubleValue() > 0.0;
        };
    }

    /**
     * Adds two possibly-arbitrary-type numbers. To suit the needs of
     * implementations of this interface, if one and only one is negative,
     * it is treated as zero.
     */
    static Number sum(final Number one, final Number two) {
        if (isPositive(one) && !isPositive(two)) {
            return one;
        } else if (isPositive(two) && !isPositive(one)) {
            return two;
        }

        if (one instanceof Integer || one instanceof Long) {
            if (two instanceof Integer || two instanceof Long) {
                return one.longValue() + two.longValue();
            } else if (two instanceof BigInteger) {
                return BigInteger.valueOf(one.longValue()).add((BigInteger) two);
            } else if (two instanceof Float || two instanceof Double) {
                return one.doubleValue() + two.doubleValue();
            } else if (two instanceof BigDecimal) {
                return BigDecimal.valueOf(one.longValue()).add((BigDecimal) two);
            } else {
                throw new IllegalStateException("Unhandled Number type for second argument");
            }
        } else if (one instanceof BigInteger) {
            if (two instanceof Integer || two instanceof Long) {
                return ((BigInteger) one).add(BigInteger.valueOf(two.longValue()));
            } else if (two instanceof BigInteger) {
                return ((BigInteger) one).add((BigInteger) two);
            } else if (two instanceof Float || two instanceof Double) {
                return one.doubleValue() + two.doubleValue();
            } else if (two instanceof BigDecimal) {
                return new BigDecimal((BigInteger) one).add((BigDecimal) two);
            } else {
                throw new IllegalStateException("Unhandled Number type for second argument");
            }
        } else if (one instanceof Float || one instanceof Double) {
            if (two instanceof Integer || two instanceof Long || two instanceof BigInteger ||
                    two instanceof Float || two instanceof Double) {
                return one.doubleValue() + two.doubleValue();
            } else if (two instanceof BigDecimal) {
                return BigDecimal.valueOf(one.doubleValue()).add((BigDecimal) two);
            } else {
                throw new IllegalStateException("Unhandled Number type for second argument");
            }
        } else if (one instanceof BigDecimal) {
            if (two instanceof BigDecimal) {
                return ((BigDecimal) one).add((BigDecimal) two);
            } else if (two instanceof Integer || two instanceof Long) {
                return ((BigDecimal) one).add(BigDecimal.valueOf(two.longValue()));
            } else if (two instanceof BigInteger) {
                return ((BigDecimal) one).add(new BigDecimal((BigInteger) two));
            } else if (two instanceof Float || two instanceof Double) {
                return ((BigDecimal) one).add(BigDecimal.valueOf(two.doubleValue()));
            } else {
                throw new IllegalStateException("Unhandled Number type for second argument");
            }
        } else {
            throw new IllegalStateException("Unhandled Number subclass for first argument");
        }
    }

    /**
     * @param num a number
     * @return the additive inverse of that number
     */
    static Number negate(final Number num) {
        if (num instanceof Integer) {
            return -(Integer) num;
        } else if (num instanceof Long) {
            return -(Long) num;
        } else if (num instanceof BigInteger) {
            return ((BigInteger) num).negate();
        } else if (num instanceof Float || num instanceof Double) {
            return 0.0 - num.doubleValue();
        } else if (num instanceof BigDecimal) {
            return ((BigDecimal) num).negate();
        } else {
            throw new IllegalStateException("Unhandled Number subclass");
        }
    }

    /**
     * The number of acres the fixture extends over.
     */
    Number getAcres();

    /**
     * Returns a copy of this object, except with its extent increased by
     * the extent of the addend, which must be of the same type.
     */
    Self combined(Self addend);

    /**
     * Returns a copy of this object, except with its extent reduced by "subtrahend" acres.
     */
    Self reduced(Number subtrahend);
}
