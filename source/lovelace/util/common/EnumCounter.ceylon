import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    Accumulator
}

"A class to count references to enumerated objects---though it does not do any sort
 of check that its type parameter is an enumerated type. For every object ever passed
 to [[count]] (or [[countMany]]), it keeps a running total of the number of times it
 has been passed that object."
shared class EnumCounter<Type>() given Type satisfies Object {
    MutableMap<Type, Accumulator<Integer>> counts = HashMap<Type, Accumulator<Integer>>();
    void count(Type item) {
        if (exists counter = counts[item]) {
            counter.add(1);
        } else {
            counts[item] = Accumulator(1);
        }
    }
    "Count the items in a sequence."
    shared void countMany(Type* values) => values.each(count);
    "Get the count for a given value."
    shared Integer getCount(Type item) => counts[item]?.sum else 0;
    "Get all values and counts."
    shared {<Type->Integer>*} allCounts =>
            counts.map(entryMap(identity<Type>, Accumulator<Integer>.sum));
}
