import ceylon.whole {
    Whole
}
import ceylon.decimal {
    decimalNumber,
    Decimal
}

"If [[number]] is a [[Decimal]], return it; otherwise, return a Decimal
 representation of the same number. Note that this will throw if it is
 a [[Float]] and either infinity or `NaN`."
shared Decimal decimalize(Number<out Anything> number) {
    assert (is Decimal|Whole|Integer|Float number);
    switch (number)
    case (is Decimal) {
        return number;
    }
    case (is Integer|Float|Whole) {
        return decimalNumber(number);
    }
}
