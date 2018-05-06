import strategicprimer.model.map {
    TileFixture
}
import lovelace.util.common {
	inverse
}
import ceylon.language.meta.model {
	ClassOrInterface
}
"A wrapper around `Boolean(TileFixture)`, used to determine Z-order of fixtures."
shared class FixtureMatcher {
	shared static FixtureMatcher trivialMatcher(ClassOrInterface<TileFixture> type,
		String description = "``type.declaration.name``s") =>
			FixtureMatcher(type.typeOf, description);
	shared static FixtureMatcher simpleMatcher<T>(Boolean(T) method, String description) {
		Boolean predicate(TileFixture fixture) {
			if (is T fixture, method(fixture)) {
				return true;
			} else {
				return false;
			}
		}
		return FixtureMatcher(predicate, description);
	}
	shared static {FixtureMatcher*} complements<out T>(Boolean(T) method,
		String firstDescription, String secondDescription) given T satisfies TileFixture =>
			[simpleMatcher<T>(method, firstDescription),
				simpleMatcher<T>(inverse(method), secondDescription)];
    shared Boolean matches(TileFixture fixture);
    shared variable Boolean displayed = true;
    shared String description;
    shared new (Boolean(TileFixture) predicate, String desc) {
        matches = predicate;
        description = desc;
    }
    shared actual String string = "Matcher for ``description``";
}
