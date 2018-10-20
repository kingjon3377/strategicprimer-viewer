import strategicprimer.model.common.map {
    TileFixture
}
import ceylon.language.meta.model {
    ClassOrInterface
}
import lovelace.util.common {
    todo
}

"A wrapper around `Boolean(TileFixture)`, used to determine Z-order of fixtures."
shared class FixtureMatcher {
    "Factory method for a matcher that matches every tile fixture of the given type."
    todo("Why not make the type to match a type parameter instead of passing it in?")
    shared static FixtureMatcher trivialMatcher(ClassOrInterface<TileFixture> type,
        String description = "``type.declaration.name``s") =>
            FixtureMatcher(type.typeOf, description);

    "Factory method for a matcher that matches tile fixtures of the given type
     that additionally match the given predicate."
    shared static FixtureMatcher simpleMatcher<FixtureType>(Boolean(FixtureType) method,
            String description) {
        Boolean predicate(TileFixture fixture) {
            if (is FixtureType fixture, method(fixture)) {
                return true;
            } else {
                return false;
            }
        }
        return FixtureMatcher(predicate, description);
    }

    "Factory method for two matchers covering fixtures of the given type that
     match and that do not match the given predicate."
    shared static {FixtureMatcher*} complements<out FixtureType>(Boolean(FixtureType) method,
        String firstDescription, String secondDescription)
            given FixtureType satisfies TileFixture =>
                [simpleMatcher<FixtureType>(method, firstDescription),
                    simpleMatcher<FixtureType>(not(method), secondDescription)];

    "Whether this matcher matches (applies to) the given fixture."
    shared Boolean matches(TileFixture fixture);
    
    "Whether fixtures that this matcher matches should be displayed."
    shared variable Boolean displayed = true;

    "A description of fixtures this matcher matches, to help the user decide
     whether to enable or disable it."
    shared String description;

    shared new (Boolean(TileFixture) predicate, String desc) {
        matches = predicate;
        description = desc;
    }
    shared actual String string = "Matcher for ``description``";
}
