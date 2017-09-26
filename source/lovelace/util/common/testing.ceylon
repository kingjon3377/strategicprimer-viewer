import ceylon.test {
    assertAll
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.test.engine {
    MultipleFailureException
}
"Verify that at least one of the given assertions passes."
see(`function assertAll`)
shared void assertAny(
        "The group of assertions."
        [Anything()+] assertions,
        "The message describing the problem."
        String? message = null) {
    MutableList<AssertionError> failures = ArrayList<AssertionError>();
    for (assertion in assertions) {
        try {
            assertion();
            return;
        } catch (AssertionError failure) {
            failures.add(failure);
        }
    }
    throw MultipleFailureException(failures.sequence(),
        message else "``failures.size`` assertions failed");
}
