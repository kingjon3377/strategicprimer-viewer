"An interface to hold a mutable value, accept modifications to it, and report its current
 value."
shared class Accumulator<Type>(variable Type count) given Type satisfies Summable<Type> {
    "Add to the accumulation."
    shared void add(Type addend) => count += addend;
    "The current value of the accumulation."
    shared Type sum => count;
}
