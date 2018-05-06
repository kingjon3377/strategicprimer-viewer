import strategicprimer.model.map {
    TileFixture
}
"A wrapper around `Boolean(TileFixture)`, used to determine Z-order of fixtures."
shared class FixtureMatcher {
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
    shared Boolean matches(TileFixture fixture);
    shared variable Boolean displayed = true;
    shared String description;
    shared new (Boolean(TileFixture) predicate, String desc) {
        matches = predicate;
        description = desc;
    }
    shared actual String string = "Matcher for ``description``";
}
