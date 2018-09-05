import strategicprimer.model.map {
    Subsettable
}

import lovelace.util.common {
    comparingOn,
    numberComparator
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
        if (is Quantity obj) {
            return units == obj.units && numberComparator.compare(number, obj.number) == equal;
        } else {
            return false;
        }
    }
    shared actual Integer hash => units.hash.or(number.hash);
    shared actual Comparison compare(Quantity quantity) =>
            comparing(comparingOn(Quantity.units, increasing<String>),
                comparingOn(Quantity.number, numberComparator.compare))(this, quantity);
}
