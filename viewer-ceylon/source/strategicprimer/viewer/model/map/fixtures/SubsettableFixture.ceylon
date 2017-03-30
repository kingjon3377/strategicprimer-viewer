import lovelace.util.common {
    todo
}

import model.map {
    IFixture,
    Subsettable
}
"An interface to use to make Tile.isSubset() work properly without special-casing every
 Subsettable fixture."
todo("Is this really necessary now we have reified generics?")
shared interface SubsettableFixture satisfies IFixture&Subsettable<IFixture> {}