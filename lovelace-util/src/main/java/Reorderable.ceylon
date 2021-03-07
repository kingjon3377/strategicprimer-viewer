"An interface for list-like things that can be reordered."
shared interface Reorderable {
    "Move a row of a list or table from one position to another."
    shared formal void reorder(
        "The index to remove from"
        Integer fromIndex,
        "The index (*before* removing the item!) to move to"
        Integer toIndex);
}
