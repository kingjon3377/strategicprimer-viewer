import ceylon.whole {
    Whole
}
import ceylon.decimal {
    decimalNumber,
    Decimal
}
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
