"Given a function and an expected value, produces a predicate that applies that function
 to each object and returns true iff it produces the expected value. This is intended to
 be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingValue<Type, Field>(Field expected, Field(Type) field)
        given Type satisfies Object =>
            compose(curry(anythingEqual)(expected), field);
"Given an [[Entry]], return true iff its key and item are the given types.

 Using [[Iterable.narrow]] on a stream of [[tuples|Tuple]] works, but doing so on a stream
 of [[entries|Entry]] results in the empty stream: the compiler and runtime set the reified
 type parameters of every tuple to the precise runtime type(s) of the objects, but make no
 such optimization for Entries, and so the type parameters are generally the declared or
 inferred types of the objects put into the Entry."
shared Boolean matchingEntry<Key, Item>(Entry<Object, Anything> entry) =>
        entry.key is Key && entry.item is Item;

Key->Item entryIdentity<Key, Item>(Object->Anything entry) given Key satisfies Object {
    assert (is Key key = entry.key, is Item item = entry.item);
    return key->item;
}
"Given a stream of [[entries|Entry]], return a version of it narrowed to the given type
 parameters.  [[Iterable.narrow]] returns the empty stream, since [[Entry]] does not have
 the special handling to set its type parameters to the objects' precise types that
 [[Tuple]] does."
shared {<Key->Item>*} narrowedStream<Key, Item>({<Object->Anything>*} stream)
        given Key satisfies Object =>
        stream.filter(matchingEntry<Key, Item>).map(entryIdentity<Key, Item>);
