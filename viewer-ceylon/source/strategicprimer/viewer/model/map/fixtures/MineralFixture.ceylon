import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
    TileFixture,
    HasKind
}
"""An (at present marker) interface for tile fixtures representing some kind of "mineral"
   (as opposed to organic) resource or thing: bare rock or the ground, stone deposit,
   mineral vein, mine, etc."""
todo("Any members?")
shared interface MineralFixture satisfies TileFixture&HasKind {}