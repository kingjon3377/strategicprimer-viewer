package common.map.fixtures;

import java.util.Comparator;
import java.util.function.Consumer;

import common.map.Subsettable;

import lovelace.util.NumberComparator;

/**
 * A number paired with its units. This class is immutable.
 */
public final class Quantity implements Subsettable<Quantity>, Comparable<Quantity> {
	/**
	 * The numeric quantity.
	 */
	private final Number number;

	/**
	 * The numeric quantity.
	 */
	public Number getNumber() {
		return number;
	}

	/**
	 * The units in which that number is measured.
	 */
	private final String units;

	/**
	 * The units in which that number is measured.
	 */
	public String getUnits() {
		return units;
	}

	public Quantity(Number number, String units) {
		this.number = number;
		this.units = units;
	}

	/**
	 * That quantity as a double.
	 * @deprecated Just use Number::doubleValue
	 */
	@Deprecated
	public double getFloatNumber() {
		return number.doubleValue();
	}

	@Override
	public String toString() {
		return String.format("%s %s", number.toString(), units);
	}

	/**
	 * A Quantity is a subset iff it has the same units and either the same
	 * or a lesser quantity.
	 */
	@Override
	public boolean isSubset(Quantity obj, Consumer<String> report) {
		if (units.equals(obj.getUnits())) {
			if (new NumberComparator().compare(number, obj.getNumber()) < 0) {
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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Quantity) {
			return units.equals(((Quantity) obj).getUnits()) &&
				new NumberComparator().compare(number, ((Quantity) obj).getNumber()) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return units.hashCode() | number.hashCode();
	}

	@Override
	public int compareTo(Quantity quantity) {
		return Comparator.comparing(Quantity::getUnits)
			.thenComparing(Quantity::getNumber, new NumberComparator())
			.compare(this, quantity);
	}
}
