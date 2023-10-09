package common.map.fixtures;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Consumer;

import common.map.Subsettable;

import lovelace.util.NumberComparator;

/**
 * A number paired with its units. This class is immutable.
 *
 * @param number The numeric quantity.
 * @param units  The units in which that number is measured.
 */
public record Quantity(Number number, String units) implements Subsettable<Quantity>, Comparable<Quantity>, Serializable {
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
		return String.format("%s %s", number, units);
	}

	/**
	 * A Quantity is a subset iff it has the same units and either the same
	 * or a lesser quantity.
	 */
	@Override
	public boolean isSubset(final Quantity obj, final Consumer<String> report) {
		if (units.equals(obj.units())) {
			if (NumberComparator.compareNumbers(number, obj.number()) < 0) {
				report.accept("Has greater quantity than we do");
				return false;
			} else {
				return true;
			}
		} else {
			report.accept("Units differ");
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof final Quantity q) {
			return units.equals(q.units()) &&
				NumberComparator.compareNumbers(number, q.number()) == 0;
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
