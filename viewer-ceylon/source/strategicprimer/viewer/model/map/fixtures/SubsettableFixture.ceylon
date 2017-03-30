import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
    Subsettable
}
import model.map {
    IFixture
}
"An interface to use to make Tile.isSubset() work properly without special-casing every
 Subsettable fixture."
todo("Is this really necessary now we have reified generics?")
shared interface SubsettableFixture satisfies IFixture&Subsettable<IFixture> {}