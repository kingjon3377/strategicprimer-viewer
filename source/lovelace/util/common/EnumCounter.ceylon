import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    Accumulator,
    IntHolder
}
"A class to count references to enumerated objects."
shared class EnumCounter<Type>() given Type satisfies Object {
    MutableMap<Type, Accumulator> counts = HashMap<Type, Accumulator>();
    void count(Type item) {
        if (exists counter = counts[item]) {
            counter.add(1);
        } else {
            counts[item] = IntHolder(1);
        }
    }
    "Count the items in a sequence."
    shared void countMany(Type* values) => values.each(count);
    "Get the count for a given value."
    shared Integer getCount(Type item) => counts[item]?.sum else 0;
}