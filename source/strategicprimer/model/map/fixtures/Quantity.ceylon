import ceylon.decimal {
    Decimal
}
import ceylon.whole {
    Whole
}

import strategicprimer.model.map {
    Subsettable
}

import lovelace.util.common {
    comparingOn,
	Comparator
}
shared object numberComparator satisfies Comparator<Number<out Anything>> { // TODO: Move to lovelace.util
	"Convert an arbitrary Number to a Float."
	shared Float floatValue(Number<out Anything> number) {
		"We only know how to handle Integers, Floats, Decimals, and Wholes."
		assert (is Integer|Float|Decimal|Whole number);
		switch (number)
		case (is Integer) { return number.float; }
		case (is Float) { return number; }
		case (is Decimal) { return number.float; }
		case (is Whole) { return number.float; }
	}
	shared actual Comparison compare(Number<out Anything> one, Number<out Anything> two) {
		if (is Integer one, is Integer two) {
			return one <=> two;
		} else if (is Float one, is Float two) {
			return one <=> two;
		} else if (is Decimal one, is Decimal two) {
			return one <=> two;
		} else if (is Whole one, is Whole two) {
			return one <=> two;
		} else {
			Float oneValue = floatValue(one);
			Float twoValue = floatValue(two);
			return oneValue <=> twoValue;
		}
	}
}
"A number paired with its units. This class is immutable."
shared class Quantity
        satisfies Subsettable<Quantity>&Comparable<Quantity> {
    "The numeric quantity."
    shared Number<out Anything> number;
    "The units in which that number is measured."
    shared String units;
	shared new (Number<out Anything> number, String units) {
		this.number = number;
		this.units = units;
	}
	"That quantity as a Float"
	shared Float floatNumber => numberComparator.floatValue(number);
    shared actual String string => "``number`` ``units``";
    "A Quantity is a subset iff it has the same units and either the same or a lesser
     quantity."
    shared actual Boolean isSubset(Quantity obj, Anything(String) report) {
        if (units == obj.units) {
            if (numberComparator.compare(number, obj.number) == smaller) {
                report("Has greater quantity than we do");
                return false;
            } else {
                return true;
            }
        } else {
            report("Units differ");
            return false;
        }
    }
    shared actual Boolean equals(Object obj) {
        if (is Quantity obj) { // FIXME: Uses == to compare numbers that could be floats
            return units == obj.units && number == obj.number;
        } else {
            return false;
        }
    }
    shared actual Integer hash => units.hash.or(number.hash);
    shared actual Comparison compare(Quantity quantity) =>
            comparing(comparingOn(Quantity.units, (String x, String y) => x <=> y),
                comparingOn(Quantity.number, numberComparator.compare))(this, quantity);
}
