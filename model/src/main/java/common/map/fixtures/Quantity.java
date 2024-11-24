package common.map.fixtures;

import lovelace.util.NumberComparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A number paired with its units. This class is immutable.
 *
 * @param number The numeric quantity.
 * @param units  The units in which that number is measured.
 */
public record Quantity(Number number, String units) implements Comparable<Quantity>, Serializable {
	/**
	 * The numeric quantity.
	 */
	@Override
	public Number number() {
		return number;
	}

	/**
	 * The units in which that number is measured.
	 */
	@Override
	public String units() {
		return units;
	}

	/**
	 * That quantity as a double.
	 *
	 * @deprecated Just use Number::doubleValue
	 */
	@Deprecated
	public double getFloatNumber() {
		return number.doubleValue();
	}

	@Override
	public String toString() {
		return "%s %s".formatted(number, units);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Quantity(final Number objNumber, final String objUnits)) {
			return units.equals(objUnits) &&
					NumberComparator.compareNumbers(number, objNumber) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return units.hashCode() | number.hashCode();
	}

	@Override
	public int compareTo(final Quantity quantity) {
		return Comparator.comparing(Quantity::units)
				.thenComparing(Quantity::number, NumberComparator::compareNumbers)
				.compare(this, quantity);
	}
}
