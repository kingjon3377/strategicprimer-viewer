import lovelace.util.common {
    todo
}
"Given comparators of the tuple's types, produce a comparator function that compares
 tuples using the first and then the second element."
todo("If this isn't used in this package, move it to the one where it is used and remove
      `shared`")
shared Comparison([T, U], [T, U]) pairComparator<T, U>(Comparison(T, T) first, Comparison(U, U) second) {
    Comparison retval([T, U] one, [T, U] two) {
        Comparison comparison = first(one.first, two.first);
        if (comparison == equal) {
            return second(one.rest.first, two.rest.first);
        } else {
            return comparison;
        }
    }
    return retval;
}
