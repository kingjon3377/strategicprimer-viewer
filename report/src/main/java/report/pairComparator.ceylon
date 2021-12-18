"Given comparators of the tuple's types, produce a comparator function that compares
 tuples using the first and then the second element."
Comparison([First, Second], [First, Second]) pairComparator<First, Second>(
        Comparison(First, First) first, Comparison(Second, Second) second) {
    Comparison retval([First, Second] one, [First, Second] two) {
        Comparison comparison = first(one.first, two.first);
        if (comparison == equal) {
            return second(one.rest.first, two.rest.first);
        } else {
            return comparison;
        }
    }
    return retval;
}
