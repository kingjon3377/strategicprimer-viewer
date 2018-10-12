"An interface to hold a mutable value, accept modifications to it, and report its current
 value."
shared interface Accumulator<Type> given Type satisfies Summable<Type> {
    "Add to the accumulation."
    shared formal void add(Type addend);
    "Get the current value of the accumulation."
    shared formal Type sum;
}
"An implementation of [[Accumulator]]."
shared class IntHolder(variable Integer count) satisfies Accumulator<Integer> {
    shared actual void add(Integer addend) => count += addend;
    shared actual Integer sum => count;
}
