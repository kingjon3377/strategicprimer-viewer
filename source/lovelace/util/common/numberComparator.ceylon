import ceylon.whole {
    Whole
}
import ceylon.decimal {
    Decimal
}
shared native object numberComparator satisfies Comparator<Number<out Anything>> {
	"Convert an arbitrary Number to a Float."
	native shared Float floatValue(Number<out Anything> number) {
		"We only know how to handle Integers, Floats, and Wholes."
		assert (is Integer|Float|Whole number);
		switch (number)
		case (is Integer) { return number.float; }
		case (is Float) { return number; }
		case (is Whole) { return number.float; }
	}
	native shared actual Comparison compare(Number<out Anything> one,
			Number<out Anything> two) {
		if (is Integer one, is Integer two) {
			return one <=> two;
		} else if (is Float one, is Float two) {
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
native("jvm")
shared object numberComparator satisfies Comparator<Number<out Anything>> {
	"Convert an arbitrary Number to a Float."
	native("jvm") shared Float floatValue(Number<out Anything> number) {
		"We only know how to handle Integers, Floats, Decimals, and Wholes."
		assert (is Integer|Float|Decimal|Whole number);
		switch (number)
		case (is Integer) { return number.float; }
		case (is Float) { return number; }
		case (is Decimal) { return number.float; }
		case (is Whole) { return number.float; }
	}
	native("jvm") shared actual Comparison compare(Number<out Anything> one,
			Number<out Anything> two) {
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
