import lovelace.util.common {
    Accumulator,
    EnumCounter,
    entryMap,
    todo
}

import ceylon.collection {
    MutableMap,
    HashMap
}

"A class that, like the [[EnumCounter|lovelace.util.common::EnumCounter]]
 class, keeps a running total for arguments it is given; unlike that class, it
 groups on the basis of a field (or equivalent mapping) provided to its
 constructor and increments the total by the value of another field instead of
 a constant value."
todo("If Key only ever String, drop type parameter",
    "Move to lovelace.util? (If so, leave Key as-is.)")
class MappedCounter<Base, Key, Count>(
            "An accessor method to get the key to use for each object that is to be
             counted."
            Key(Base) keyExtractor,
            "An accessor method to get the quantity to increment the count by for each
             object that is to be counted."
            Count(Base) countExtractor,
            "A constructor for an accumulator for the count type."
            Accumulator<Count>(Count) factory,
            "Zero in the count type."
            Count zero) satisfies {<Key->Count>*}
        given Base satisfies Object given Key satisfies Object
        given Count satisfies Summable<Count>&Comparable<Count> {
    MutableMap<Key, Accumulator<Count>> totals = HashMap<Key, Accumulator<Count>>();

    "Increment the count for the given key by the given amount."
    shared void addDirectly(Key key, Count addend) {
        if (exists count = totals[key]) {
            count.add(addend);
        } else {
            totals[key] = factory(addend);
        }
    }

    "Increment the count for the key and by the quantity extracted from the given object."
    shared void add(Base obj) => addDirectly(keyExtractor(obj), countExtractor(obj));

    "A stream of keys and associated counts seen so far."
    shared actual Iterator<Key->Count> iterator() =>
            totals.map(entryMap(identity<Key>, Accumulator<Count>.sum))
                .sort(decreasingItem).iterator();

    "The total counted for all keys taken together."
    shared Count total => totals.items.map(Accumulator<Count>.sum).fold(zero)(plus);
}
