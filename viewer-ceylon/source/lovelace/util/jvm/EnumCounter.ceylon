import ceylon.collection {
    MutableMap,
    HashMap
}

import java.lang {
    Enum
}

import lovelace.util.common {
    Accumulator,
    IntHolder,
    todo
}
"A class to count instances of enums."
todo("Adapt to work with Ceylon case-values") // FIXME
shared class EnumCounter<Type>() given Type satisfies Object {
    MutableMap<Type, Accumulator> counts = HashMap<Type, Accumulator>();
    void count(Type item) {
        if (exists counter = counts.get(item)) {
            counter.add(1);
        } else {
            Accumulator counter = IntHolder(1);
            counts.put(item, counter);
        }
    }
    "Count the items in a sequence."
    shared void countMany(Type* values) {
        for (item in values) {
            count(item);
        }
    }
    "Get the count for a given value."
    shared Integer getCount(Type item) {
        if (exists counter = counts.get(item)) {
            return counter.sum;
        } else {
            return 0;
        }
    }
}