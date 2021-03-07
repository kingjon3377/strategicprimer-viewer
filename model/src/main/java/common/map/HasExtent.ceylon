import ceylon.whole {
    Whole,
    wholeNumber
}
import ceylon.decimal {
    Decimal,
    decimalNumber
}
"An interface for fixtures representing large features that report their extent in acres."
see(`interface HasPopulation`)
shared interface HasExtent<Self> satisfies IFixture&Subsettable<IFixture>
        given Self satisfies HasExtent<Self>&Object {
    "Adds two possibly-arbitrary-type numbers. To suit the needs of implementations of
     this interface, if one and only one is negative, it is treated as zero."
    // TODO: Once static members of interfaces no longer produce warnings, switch from default to static
    shared native default Number<out Anything> sum(Number<out Anything> one,
            Number<out Anything> two) {
        if (one.positive, !two.positive) {
            return one;
        } else if (two.positive, !one.positive) {
            return two;
        }
        assert (is Integer|Whole|Float one, is Integer|Whole|Float two);
        switch (one)
        case (is Integer) {
            switch (two)
            case (is Integer) {
                return one + two;
            }
            case (is Whole) {
                return wholeNumber(one) + two;
            }
            case (is Float) {
                return one.float + two;
            }
        }
        case (is Whole) {
            switch (two)
            case (is Integer) {
                return one + wholeNumber(two);
            }
            case (is Whole) {
                return one + two;
            }
            case (is Float) {
                return one.float + two;
            }
        }
        case (is Float) {
            switch (two)
            case (is Integer) {
                return one + two.float;
            }
            case (is Whole) {
                return one + two.float;
            }
            case (is Float) {
                return one + two;
            }
        }
    }
    "Adds two possibly-arbitrary-type numbers. To suit the needs of implementations of
     this interface, if one and only one is negative, it is treated as zero."
    shared native("jvm") default Number<out Anything> sum(Number<out Anything> one,
            Number<out Anything> two) {
        if (one.positive, !two.positive) {
            return one;
        } else if (two.positive, !one.positive) {
            return two;
        }
        assert (is Integer|Whole|Float|Decimal one, is Integer|Whole|Float|Decimal two);
        switch (one)
        case (is Integer) {
            switch (two)
            case (is Integer) {
                return one + two;
            }
            case (is Whole) {
                return wholeNumber(one) + two;
            }
            case (is Float) {
                return one.float + two;
            }
            case (is Decimal) {
                return decimalNumber(one) + two;
            }
        }
        case (is Whole) {
            switch (two)
            case (is Integer) {
                return one + wholeNumber(two);
            }
            case (is Whole) {
                return one + two;
            }
            case (is Float) {
                return one.float + two;
            }
            case (is Decimal) {
                return decimalNumber(one) + two;
            }
        }
        case (is Float) {
            switch (two)
            case (is Integer) {
                return one + two.float;
            }
            case (is Whole) {
                return one + two.float;
            }
            case (is Float) {
                return one + two;
            }
            case (is Decimal) {
                return decimalNumber(one) + two;
            }
        }
        case (is Decimal) {
            if (is Decimal two) {
                return one + two;
            } else {
                assert (is Integer|Whole|Float two);
                return one + decimalNumber(two);
            }
        }
    }
    "The number of acres the fixture extends over."
    shared formal Number<out Anything> acres;
    "Returns a copy o this object, except with its extent increased by the extent of the
     [[addend]], which must be of the same type."
    shared formal Self combined(Self addend);
    "Returns a copy of this object, except with its extent reduced by [[subtrahend]] acres."
    shared formal Self reduced(Number<out Number<out Anything>> subtrahend);
}
