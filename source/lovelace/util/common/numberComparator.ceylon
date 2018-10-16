import ceylon.whole {
    Whole
}
import ceylon.decimal {
    Decimal
}

"An object providing a comparison for [[Numbers|Number]] of unknown or varied types. In the default
 implementation, only [[Integer]], [[Float]], and [[Whole]] numbers are supported; on the JVM,
 [[Decimals|Decimal]] are additionally supported. Passing any unsupported type will raise an
 [[AssertionError]]."
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
    "Compare two numbers. If they are the same type, delegate to their built-in comparison
     function; if not, convert both to [[Float]] and return the result of comparing those."
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
