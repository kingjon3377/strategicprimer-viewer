"Given a function and an expected value, produces a predicate that applies that function
 to each object and returns true iff it produces the expected value. This is intended to
 be used with [[Iterable.filter]] and the like."
shared Boolean(Type) matchingValue<Type, Field>(Field expected, Field(Type) field)
        given Type satisfies Object =>
            compose(curry(anythingEqual)(expected), field);
