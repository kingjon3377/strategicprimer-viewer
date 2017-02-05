package util;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Formatter;
import model.map.Subsettable;

/**
 * A number paired with its units. This class is immutable.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Quantity implements Subsettable<Quantity>, Comparable<Quantity> {
	/**
	 * A quantity of zero.
	 */
	@SuppressWarnings("ConstantNamingConvention")
	private static final Number ZERO = Integer.valueOf(0);
	/**
	 * The number.
	 */
	private final Number number;
	/**
	 * The units in which the number is measured.
	 */
	private final String units;
	/**
	 * Constructor.
	 * @param num the number
	 * @param unit its units
	 */
	public Quantity(final Number num, final String unit) {
		units = unit;
		number = num;
	}
	/**
	 * Get the number, as a pure scalar.
	 * @return the number
	 */
	public Number getNumber() {
		return number;
	}
	/**
	 * Get the number's units.
	 * @return the number's units
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * Nearly-trivial.
	 * @return a String representation of the quantity
	 */
	@Override
	public String toString() {
		return String.format("%s %s", number.toString(), units);
	}

	/**
	 * A Quantity is a subset iff it has the same units and either the same quantity,
	 * zero quantity, or a lesser quantity by "double value".
	 * @param obj     an object
	 * @param ostream the stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context; it should be passed through and appended to. Whenever
	 *                it is
	 *                put onto ostream, it should probably be followed by a tab.
	 * @return whether it is a strict subset of this object---with no members that aren't
	 * also in this.
	 */
	@Override
	public boolean isSubset(final Quantity obj, final Formatter ostream,
							final String context) {
		if (units.equals(obj.units)) {
			if (number.equals(obj.number) || ZERO.equals(obj.number) ||
						obj.number.doubleValue() < number.doubleValue()) {
				return true;
			} else {
				ostream.format("%s: Has greater quantity than we do%n", context);
				return false;
			}
		} else {
			ostream.format("%s: Units differ%n", context);
			return false;
		}
	}
	/**
	 * Test equality.
	 * @param obj an object
	 * @return whether it's an equal quantity
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Quantity && units.equals(((Quantity) obj).units) &&
					   number.equals(((Quantity) obj).number);
	}
	/**
	 * Hash value.
	 * @return a hash value for the quantity.
	 */
	@Override
	public int hashCode() {
		return units.hashCode() | number.hashCode();
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * @param obj the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compareTo(final Quantity obj) {
		return Comparator.comparing(Quantity::getUnits)
					   .thenComparing(Quantity::getNumber, Quantity::compareNumbers)
					   .compare(this, obj);
	}
	/**
	 * Compare two Numbers. If they're both Integers or BigDecimals, use the native
	 * conversion. If their integer parts are equal, compare using doubleValue(); if
	 * not, compare using those integer parts.
	 *
	 * @param first  the first number
	 * @param second the second number
	 * @return the result of the comparison
	 */
	private static int compareNumbers(final Number first, final Number second) {
		if (first instanceof Integer && second instanceof Integer) {
			return ((Integer) first).compareTo((Integer) second);
		} else if (first instanceof BigDecimal && second instanceof BigDecimal) {
			return ((BigDecimal) first).compareTo((BigDecimal) second);
		} else if (first.intValue() == second.intValue()) {
			return Double.compare(first.doubleValue(), second.doubleValue());
		} else {
			return Integer.compare(first.intValue(), second.intValue());
		}
	}
}
