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
            Accumulator counter = IntHolder(1);
            counts[item] = counter;
        }
    }
    "Count the items in a sequence."
    shared void countMany(Type* values) {
        for (item in values) {
            count(item);
        }
    }
    "Get the count for a given value."
    shared Integer getCount(Type item) => counts[item]?.sum else 0;
}