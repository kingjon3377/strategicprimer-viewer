"""A [[Correspondence]] that uses something other than [[null]] for "absent" values."""
shared interface NonNullCorrespondence<in Key, out Item=Anything>
        satisfies Correspondence<Key, Item> given Key satisfies Object {
    shared actual formal Item get(Key key);
}
