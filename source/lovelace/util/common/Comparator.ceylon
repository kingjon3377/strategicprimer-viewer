"An interface to provide a comparison function for objects of a specific type."
shared interface Comparator<Item> {
    "Compare two instances of the type."
    shared formal Comparison compare(Item one, Item two);
}
