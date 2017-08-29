"An interface to hold a mutable value, accept modifications to it, and report its current
 value."
shared interface Accumulator {
    "Add to the accumulation."
    shared formal void add(Integer addend);
    "Get the current value of the accumulation."
    shared formal Integer sum;
}
"An implementation of [[Accumulator]]."
shared class IntHolder(variable Integer count) satisfies Accumulator {
    shared actual void add(Integer addend) => count += addend;
    shared actual Integer sum => count;
}