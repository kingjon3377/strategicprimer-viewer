"Given a function and a predicate, produces another predicate that applies the function
 to each object and returns the result of the given predicate applied to that result. This
 is intended to be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingPredicate<Type, Field>(Boolean(Field) predicate, Field(Type) field)
		given Type satisfies Object => (Type other) => predicate(field(other));
"Given a function and an expected value, produces a predicate that applies that function
 to each object and returns true iff it produces the expected value. This is intended to
 be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingValue<Type, Field>(Field expected, Field(Type) field)
		given Type satisfies Object => matchingPredicate((Anything obj) => anythingEqual(obj, expected), field);
"Given a predicate, produces a predicate that returns true iff the given predicate returns false."
shared Boolean(Type) inverse<Type>(Boolean(Type) predicate) => (Type item) => !predicate(item);