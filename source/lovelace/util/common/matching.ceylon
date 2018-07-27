"Given a function and a predicate, produces another predicate that applies the function
 to each object and returns the result of the given predicate applied to that result. This
 is intended to be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingPredicate<Type, Field>(Boolean(Field) predicate,
		Field(Type) field) given Type satisfies Object => compose(predicate, field);
"Given a function and an expected value, produces a predicate that applies that function
 to each object and returns true iff it produces the expected value. This is intended to
 be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingValue<Type, Field>(Field expected, Field(Type) field)
		given Type satisfies Object => matchingPredicate(curry(anythingEqual)(expected), field);
"Given a predicate, produces a predicate that returns true iff the given predicate returns
 false."
shared Boolean(Type) inverse<Type>(Boolean(Type) predicate) =>
				(Type item) => !predicate(item);
"Given an [[Entry]], return true iff its key and item are the given types.

 Using [[Iterable.narrow]] on a stream of [[tuples|Tuple]] works, but doing so on a stream
 of [[entries|Entry]] results in the empty stream."
shared Boolean matchingEntry<Key, Item>(Entry<Object, Anything> entry) =>
		entry.key is Key && entry.item is Item;

Key->Item entryIdentity<Key, Item>(Object->Anything entry) given Key satisfies Object {
	assert (is Key key = entry.key, is Item item = entry.item);
	return key->item;
}
"Given a stream of Entries, return a version of it narrowed to the given type parameters."
shared {<Key->Item>*} narrowedStream<Key, Item>({<Object->Anything>*} stream)
		given Key satisfies Object =>
		stream.filter(matchingEntry<Key, Item>).map(entryIdentity<Key, Item>);
