import lovelace.util.common {
    todo
}

todo("Simply replace with Iterable<whatever> in uses?")
shared interface FixtureIterable<Element> satisfies Iterable<Element>
        given Element satisfies IFixture { }