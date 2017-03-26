import lovelace.util.common {
    todo
}

import model.map {
    IFixture
}
todo("Simply replace with Iterable<whatever> in uses?")
shared interface FixtureIterable<Element> satisfies Iterable<Element>
        given Element satisfies IFixture { }