package legacy.map.fixtures;

import legacy.map.Subsettable;
import lovelace.util.NumberComparator;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * A number paired with its units. This class is immutable.
 *
 * @param number The numeric quantity.
 * @param units  The units in which that number is measured.
 */
public record LegacyQuantity(Number number, String units)
		implements Subsettable<LegacyQuantity>, Comparable<LegacyQuantity>, Serializable {
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

	/**
	 * A Quantity is a subset iff it has the same units and either the same
	 * or a lesser quantity.
	 */
	@Override
	public boolean isSubset(final LegacyQuantity obj, final @NotNull Consumer<String> report) {
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
		} else if (obj instanceof final LegacyQuantity q) {
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
	public int compareTo(final @NotNull LegacyQuantity quantity) {
		return Comparator.comparing(LegacyQuantity::units)
				.thenComparing(LegacyQuantity::number, NumberComparator::compareNumbers)
				.compare(this, quantity);
	}
}
